package dev.agb.nasmplugin.preprocessor

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.settings.CommandLineMacroProvider
import dev.agb.nasmplugin.settings.NasmProjectSettings

/**
 * Represents a macro definition in the preprocessor state.
 */
data class MacroDefinition(
    val name: String,
    val value: String? = null,
    val isCommandLine: Boolean = false
)

/**
 * Types of conditional directives, designed for extensibility.
 * New conditional types can be added as sealed class variants.
 */
sealed class ConditionalType {
    /**
     * %ifdef MACRO - true if MACRO is defined
     */
    data class Ifdef(val macroName: String) : ConditionalType()

    /**
     * %ifndef MACRO - true if MACRO is not defined
     */
    data class Ifndef(val macroName: String) : ConditionalType()

    /**
     * %if expression - true if expression evaluates to non-zero
     * TODO: Implement expression evaluation
     */
    data class If(val condition: NasmCondition?) : ConditionalType()

    /**
     * Future: %ifidn, %ifidni, %ifnum, %ifstr, etc.
     * These can be added as additional sealed class variants
     */
}

/**
 * Represents a single branch within a conditional block with its active state.
 */
data class ConditionalBranch(
    val textRange: TextRange,
    val isActive: Boolean,
    val conditionalBlock: NasmConditionalBlock,
    val branchType: BranchType
) {
    enum class BranchType {
        IF,      // The initial %if/%ifdef/%ifndef branch
        ELIF,    // An %elif branch
        ELSE     // The %else branch
    }
}

/**
 * Service that evaluates preprocessor conditional directives and determines
 * which code branches are active or inactive.
 *
 * This service:
 * - Evaluates %ifdef, %ifndef, and other conditional directives
 * - Tracks macro definitions from command-line settings and source code
 * - Handles nested conditionals correctly
 * - Caches results for performance
 *
 * The evaluation is extensible - new conditional types can be added by:
 * 1. Adding a variant to the ConditionalType sealed class
 * 2. Implementing evaluation logic in evaluateCondition()
 */
@Service(Service.Level.PROJECT)
class PreprocessorStateEvaluator(private val project: Project) {

    /**
     * Evaluates all conditional blocks in a file and returns information
     * about which branches are active/inactive.
     *
     * Results are cached and invalidated when:
     * - The file is modified
     * - Project settings change (command-line macros)
     */
    fun evaluateFile(file: PsiFile): List<ConditionalBranch> {
        return CachedValuesManager.getCachedValue(file) {
            val branches = doEvaluateFile(file)
            CachedValueProvider.Result.create(
                branches,
                // Invalidate cache when:
                PsiModificationTracker.getInstance(project).forLanguage(file.language), // File changes
                NasmProjectSettings.getInstance(project) // Settings change
            )
        }
    }

    /**
     * Performs the actual evaluation of conditional blocks in a file.
     */
    private fun doEvaluateFile(file: PsiFile): List<ConditionalBranch> {
        // Build initial macro state from command-line definitions
        val macroProvider = CommandLineMacroProvider.getInstance(project)
        val macros = mutableMapOf<String, MacroDefinition>()

        macroProvider.getCommandLineMacros().forEach { cmdMacro ->
            macros[cmdMacro.name] = MacroDefinition(
                name = cmdMacro.name,
                value = cmdMacro.value,
                isCommandLine = true
            )
        }

        // Evaluate all conditional blocks in order
        // This maintains proper macro state tracking
        val branches = mutableListOf<ConditionalBranch>()
        evaluateFileRecursively(file, macros, branches, isParentActive = true)

        return branches
    }

    /**
     * Recursively evaluates all elements in a file, tracking macro state
     * and evaluating conditional blocks.
     *
     * @param element Current PSI element being processed
     * @param macros Current macro state
     * @param branches Output list of conditional branches
     * @param isParentActive Whether the parent conditional branch is active
     */
    private fun evaluateFileRecursively(
        element: PsiElement,
        macros: MutableMap<String, MacroDefinition>,
        branches: MutableList<ConditionalBranch>,
        isParentActive: Boolean
    ) {
        when (element) {
            is NasmConditionalBlock -> {
                // Only evaluate conditionals if parent branch is active
                if (isParentActive) {
                    evaluateConditionalBlock(element, macros, branches)
                } else {
                    // Parent is inactive, so all child branches are inactive too
                    markAllBranchesInactive(element, branches)
                }
            }

            is NasmPpDefineStmt -> {
                // Track %define statements (only if in active branch)
                if (isParentActive) {
                    val macroName = element.name
                    if (macroName != null) {
                        // For %define, we don't track the value since it can be complex
                        macros[macroName] = MacroDefinition(macroName)
                    }
                }
            }

            is NasmPpAssignStmt -> {
                // Track %assign statements (only if in active branch)
                if (isParentActive) {
                    val macroName = element.name
                    if (macroName != null) {
                        // For %assign, we could evaluate the constant expression
                        // For now, just mark it as defined
                        macros[macroName] = MacroDefinition(macroName)
                    }
                }
            }

            // TODO: Track %undef to remove macros from state

            else -> {
                // Recursively process children
                element.children.forEach { child ->
                    evaluateFileRecursively(child, macros, branches, isParentActive)
                }
            }
        }
    }

    /**
     * Evaluates a single conditional block and adds branch information to the output list.
     */
    private fun evaluateConditionalBlock(
        block: NasmConditionalBlock,
        macros: Map<String, MacroDefinition>,
        branches: MutableList<ConditionalBranch>
    ) {
        val ifDir = block.ifDir
        val elifDirs = block.elifDirList
        val elseDir = block.elseDir

        // Extract and evaluate the initial condition
        val conditionType = extractConditionType(ifDir)
        val ifConditionResult = evaluateCondition(conditionType, macros)

        // If the condition cannot be evaluated (contains macro parameters),
        // don't add any branches - treat all as potentially active
        if (ifConditionResult == null) {
            return
        }

        val ifConditionTrue: Boolean = ifConditionResult

        // Find which branch is active
        var activeBranchFound: Boolean = ifConditionTrue

        // Process IF branch
        val ifBranchRange = getIfBranchRange(block, ifDir, elifDirs.firstOrNull() ?: elseDir)
        if (ifBranchRange != null) {
            branches.add(ConditionalBranch(
                textRange = ifBranchRange,
                isActive = ifConditionTrue,
                conditionalBlock = block,
                branchType = ConditionalBranch.BranchType.IF
            ))
        }

        // Process ELIF branches
        for ((index, elifDir) in elifDirs.withIndex()) {
            val elifConditionType = extractConditionType(elifDir)
            val elifConditionResult = evaluateCondition(elifConditionType, macros)

            // If any elif condition is unevaluable, we can't determine which branches are active/inactive
            // In this case, stop adding branches (treat remaining as potentially active)
            if (elifConditionResult == null) {
                // Don't return immediately - we've already added some branches
                // Just stop processing this conditional block
                return
            }

            val elifConditionTrue = !activeBranchFound && elifConditionResult

            if (elifConditionTrue) {
                activeBranchFound = true
            }

            val nextBranch = elifDirs.getOrNull(index + 1) ?: elseDir
            val elifBranchRange = getElifBranchRange(elifDir, nextBranch)
            if (elifBranchRange != null) {
                branches.add(ConditionalBranch(
                    textRange = elifBranchRange,
                    isActive = elifConditionTrue,
                    conditionalBlock = block,
                    branchType = ConditionalBranch.BranchType.ELIF
                ))
            }
        }

        // Process ELSE branch
        if (elseDir != null) {
            val elseBranchRange = getElseBranchRange(block, elseDir)
            if (elseBranchRange != null) {
                branches.add(ConditionalBranch(
                    textRange = elseBranchRange,
                    isActive = !activeBranchFound,
                    conditionalBlock = block,
                    branchType = ConditionalBranch.BranchType.ELSE
                ))
            }
        }
    }

    /**
     * Extracts the condition type from an %if/%ifdef/%ifndef directive.
     */
    private fun extractConditionType(dir: PsiElement): ConditionalType {
        // Get the directive keyword (first child)
        val directiveKeyword = dir.firstChild?.text?.lowercase() ?: ""

        return when {
            directiveKeyword == "%ifdef" || directiveKeyword == "%elifdef" -> {
                // For %ifdef/%elifdef, the macro name is in the macroRef child (if dir)
                // or needs to be extracted from text (elif dir)
                val macroName = when (dir) {
                    is NasmIfDir -> dir.macroRef?.text ?: ""
                    else -> {
                        // For %elifdef, extract macro name from remaining children
                        // Skip the first child (directive keyword) and get the identifier
                        dir.children.drop(1).firstOrNull {
                            it.node.elementType == NasmTypes.IDENTIFIER
                        }?.text ?: ""
                    }
                }
                ConditionalType.Ifdef(macroName)
            }

            directiveKeyword == "%ifndef" || directiveKeyword == "%elifndef" -> {
                val macroName = when (dir) {
                    is NasmIfDir -> dir.macroRef?.text ?: ""
                    else -> {
                        // For %elifndef, extract macro name from remaining children
                        dir.children.drop(1).firstOrNull {
                            it.node.elementType == NasmTypes.IDENTIFIER
                        }?.text ?: ""
                    }
                }
                ConditionalType.Ifndef(macroName)
            }

            directiveKeyword == "%if" || directiveKeyword == "%elif" -> {
                val condition = (dir as? NasmIfDir)?.condition ?: (dir as? NasmElifDir)?.condition
                ConditionalType.If(condition)
            }

            else -> {
                // Unknown directive - default to If with null condition
                // This will evaluate to false
                ConditionalType.If(null)
            }
        }
    }

    /**
     * Checks if a condition contains macro parameters that make it unevaluable.
     * Macro parameters include %1, %2, %+1, %-1, etc.
     */
    private fun containsMacroParameters(condition: NasmCondition?): Boolean {
        if (condition == null) return false

        // Check all descendants recursively for macro parameter tokens
        fun hasNestedMacroParam(element: PsiElement): Boolean {
            val elementType = element.node?.elementType
            if (elementType == NasmTypes.MACRO_PARAM ||
                elementType == NasmTypes.MACRO_PARAM_GREEDY ||
                elementType == NasmTypes.MACRO_PARAM_REVERSE) {
                return true
            }
            return element.children.any { hasNestedMacroParam(it) }
        }

        return hasNestedMacroParam(condition)
    }

    /**
     * Evaluates a condition based on current macro state.
     * This is the main extensibility point - add new condition types here.
     *
     * @return true if condition is true, false if condition is false, null if condition cannot be evaluated
     */
    private fun evaluateCondition(
        conditionType: ConditionalType,
        macros: Map<String, MacroDefinition>
    ): Boolean? {
        return when (conditionType) {
            is ConditionalType.Ifdef -> {
                // True if macro is defined
                macros.containsKey(conditionType.macroName)
            }

            is ConditionalType.Ifndef -> {
                // True if macro is NOT defined
                !macros.containsKey(conditionType.macroName)
            }

            is ConditionalType.If -> {
                // Check if the condition contains macro parameters
                if (containsMacroParameters(conditionType.condition)) {
                    // Cannot evaluate - contains runtime macro parameters
                    return null
                }

                // TODO: Implement expression evaluation
                // For now, we cannot evaluate %if conditions reliably, so return null
                // to avoid incorrectly marking branches as inactive
                null
            }

            // Future condition types can be added here
        }
    }

    /**
     * Gets the text range for the IF branch (from after %if to before %elif/%else/%endif).
     */
    private fun getIfBranchRange(
        block: NasmConditionalBlock,
        ifDir: NasmIfDir,
        nextBranch: PsiElement?
    ): TextRange? {
        val startOffset = ifDir.textRange.endOffset
        val endOffset = when (nextBranch) {
            null -> block.endifDir?.textRange?.startOffset ?: block.textRange.endOffset
            else -> nextBranch.textRange.startOffset
        }

        return if (endOffset > startOffset) {
            TextRange(startOffset, endOffset)
        } else null
    }

    /**
     * Gets the text range for an ELIF branch.
     */
    private fun getElifBranchRange(elifDir: NasmElifDir, nextBranch: PsiElement?): TextRange? {
        val startOffset = elifDir.textRange.endOffset
        val endOffset = nextBranch?.textRange?.startOffset
            ?: elifDir.parent?.let { (it as? NasmConditionalBlock)?.endifDir?.textRange?.startOffset }
            ?: return null

        return if (endOffset > startOffset) {
            TextRange(startOffset, endOffset)
        } else null
    }

    /**
     * Gets the text range for the ELSE branch.
     */
    private fun getElseBranchRange(block: NasmConditionalBlock, elseDir: NasmElseDir): TextRange? {
        val startOffset = elseDir.textRange.endOffset
        val endOffset = block.endifDir?.textRange?.startOffset ?: block.textRange.endOffset

        return if (endOffset > startOffset) {
            TextRange(startOffset, endOffset)
        } else null
    }

    /**
     * Marks all branches in a conditional block as inactive (used when parent is inactive).
     */
    private fun markAllBranchesInactive(
        block: NasmConditionalBlock,
        branches: MutableList<ConditionalBranch>
    ) {
        val ifDir = block.ifDir
        val elifDirs = block.elifDirList
        val elseDir = block.elseDir

        // Mark IF branch as inactive
        val ifBranchRange = getIfBranchRange(block, ifDir, elifDirs.firstOrNull() ?: elseDir)
        if (ifBranchRange != null) {
            branches.add(ConditionalBranch(
                textRange = ifBranchRange,
                isActive = false,
                conditionalBlock = block,
                branchType = ConditionalBranch.BranchType.IF
            ))
        }

        // Mark all ELIF branches as inactive
        for ((index, elifDir) in elifDirs.withIndex()) {
            val nextBranch = elifDirs.getOrNull(index + 1) ?: elseDir
            val elifBranchRange = getElifBranchRange(elifDir, nextBranch)
            if (elifBranchRange != null) {
                branches.add(ConditionalBranch(
                    textRange = elifBranchRange,
                    isActive = false,
                    conditionalBlock = block,
                    branchType = ConditionalBranch.BranchType.ELIF
                ))
            }
        }

        // Mark ELSE branch as inactive
        if (elseDir != null) {
            val elseBranchRange = getElseBranchRange(block, elseDir)
            if (elseBranchRange != null) {
                branches.add(ConditionalBranch(
                    textRange = elseBranchRange,
                    isActive = false,
                    conditionalBlock = block,
                    branchType = ConditionalBranch.BranchType.ELSE
                ))
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PreprocessorStateEvaluator {
            return project.getService(PreprocessorStateEvaluator::class.java)
        }
    }
}
