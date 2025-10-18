package dev.agb.nasmplugin.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*

/**
 * Inspection that detects macro redefinitions.
 *
 * This inspection warns when a macro is defined multiple times within the same file
 * or across included files. The warning appears on the later definition(s) and
 * references the original definition location.
 *
 * Note: This inspection excludes %assign directives, as they are explicitly designed
 * for reassignment and redefinition is their intended purpose.
 *
 * Example:
 * ```nasm
 * %define FOO 1        ; Original definition
 * %define FOO 2        ; Warning: Macro 'FOO' is already defined at line 1
 *
 * %macro test 0        ; Original definition
 * ; ...
 * %endmacro
 * %macro test 0        ; Warning: Macro 'test' is already defined at line 3
 * ; ...
 * %endmacro
 * ```
 */
class NasmMacroRedefinitionInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return NasmMacroRedefinitionVisitor(holder)
    }

    private class NasmMacroRedefinitionVisitor(private val holder: ProblemsHolder) : PsiElementVisitor() {

        // Lazily initialized map of all macro definitions from current file and includes
        private var allMacroDefinitions: Map<String, List<NasmNamedElement>>? = null

        override fun visitElement(element: PsiElement) {
            super.visitElement(element)

            // Only check macro definitions (not %assign, which is for reassignment)
            if (!isMacroDefinition(element)) return

            val namedElement = element as NasmNamedElement
            val name = namedElement.name ?: return

            // Skip if this element is in an inactive conditional branch
            if (element.isInInactiveConditionalBranch()) return

            // Initialize the macro definitions map on first use
            if (allMacroDefinitions == null) {
                allMacroDefinitions = collectAllMacroDefinitions(holder.file)
            }

            // Generate a unique key for this macro
            // Multi-line macros with different parameter counts are different macros
            val key = getMacroKey(namedElement)

            // Check if this macro has already been defined earlier
            val existingDefinitions = allMacroDefinitions!![key] ?: emptyList()

            // Check each existing definition to see if it conflicts
            for (existingDefinition in existingDefinitions) {
                if (existingDefinition == namedElement) continue

                // Check if this definition comes after the existing one
                if (isDefinedAfter(namedElement, existingDefinition)) {
                    // Check if the definitions are in mutually exclusive branches
                    if (areInMutuallyExclusiveBranches(namedElement, existingDefinition)) {
                        continue
                    }

                    // Redefinition detected - register a problem
                    val nameIdentifier = namedElement.nameIdentifier ?: namedElement

                    // Build a message that references the original definition
                    val originalLocation = getLocationDescription(existingDefinition)
                    val message = "Macro '$name' is already defined$originalLocation"

                    holder.registerProblem(
                        nameIdentifier,
                        message,
                        ProblemHighlightType.WARNING,
                        RemoveMacroDefinitionFix(),
                        NavigateToOriginalDefinitionFix(existingDefinition)
                    )
                    break // Only report once per definition
                }
            }
        }

        /**
         * Collects all macro definitions from the current file and its includes.
         * Returns a map of macro key -> list of all definitions with that key.
         */
        private fun collectAllMacroDefinitions(file: PsiFile): Map<String, List<NasmNamedElement>> {
            return CachedValuesManager.getCachedValue(file) {
                val definitions = mutableMapOf<String, MutableList<NasmNamedElement>>()

                // Process included files first (so definitions in the main file override them)
                file.getIncludedFiles().forEach { includedFile ->
                    collectMacrosFromFile(includedFile, definitions)
                }

                // Then process the main file
                collectMacrosFromFile(file, definitions)

                CachedValueProvider.Result.create(
                    definitions as Map<String, List<NasmNamedElement>>,
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        }

        /**
         * Collects macro definitions from a single file and adds them to the map.
         * Records all definitions, including duplicates (which will be checked for conflicts later).
         */
        private fun collectMacrosFromFile(
            file: PsiFile,
            definitions: MutableMap<String, MutableList<NasmNamedElement>>
        ) {
            file.accept(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    // Must call super to ensure children are visited
                    super.visitElement(element)

                    // Recursively visit children
                    element.acceptChildren(this)

                    if (isMacroDefinition(element)) {
                        val namedElement = element as NasmNamedElement
                        val key = getMacroKey(namedElement)

                        // Skip inactive definitions
                        if (!element.isInInactiveConditionalBranch()) {
                            // Record all definitions (not just the first)
                            definitions.getOrPut(key) { mutableListOf() }.add(namedElement)
                        }
                    }
                }
            })
        }

        /**
         * Checks if two definitions are in mutually exclusive branches of the same conditional block.
         * Returns true if they are in different branches (if/elif/else) of the same conditional.
         */
        private fun areInMutuallyExclusiveBranches(def1: NasmNamedElement, def2: NasmNamedElement): Boolean {
            // Must be in the same file
            if (def1.containingFile != def2.containingFile) {
                return false
            }

            // Find the conditional blocks that each definition belongs to
            val block1 = PsiTreeUtil.getParentOfType(def1 as PsiElement, NasmConditionalBlock::class.java)
            val block2 = PsiTreeUtil.getParentOfType(def2 as PsiElement, NasmConditionalBlock::class.java)

            // If they're not in the same conditional block, they're not mutually exclusive
            if (block1 == null || block2 == null || block1 != block2) {
                return false
            }

            // They're in the same conditional block - now check if they're in different branches
            // We need to determine which branch each definition is in by checking the structure
            val branch1 = findBranchForElement(def1, block1)
            val branch2 = findBranchForElement(def2, block2)

            // If both are in branches and they're different branches, they're mutually exclusive
            return branch1 != null && branch2 != null && branch1 != branch2
        }

        /**
         * Determines which branch (IF, ELIF, ELSE) an element belongs to within a conditional block.
         * Returns a unique identifier for the branch (the directive element itself).
         */
        private fun findBranchForElement(element: PsiElement, block: NasmConditionalBlock): PsiElement? {
            val elementOffset = element.textOffset
            val ifDir = block.ifDir
            val elifDirs = block.elifDirList
            val elseDir = block.elseDir
            val endifDir = block.endifDir

            // Check if in IF branch (between %if and first %elif/%else/%endif)
            val ifEndOffset = (elifDirs.firstOrNull() ?: elseDir ?: endifDir)?.textRange?.startOffset
                ?: block.textRange.endOffset
            if (elementOffset >= ifDir.textRange.endOffset && elementOffset < ifEndOffset) {
                return ifDir
            }

            // Check if in any ELIF branch
            for ((index, elifDir) in elifDirs.withIndex()) {
                val elifEndOffset = (elifDirs.getOrNull(index + 1) ?: elseDir ?: endifDir)?.textRange?.startOffset
                    ?: block.textRange.endOffset
                if (elementOffset >= elifDir.textRange.endOffset && elementOffset < elifEndOffset) {
                    return elifDir
                }
            }

            // Check if in ELSE branch
            if (elseDir != null) {
                val elseEndOffset = endifDir?.textRange?.startOffset ?: block.textRange.endOffset
                if (elementOffset >= elseDir.textRange.endOffset && elementOffset < elseEndOffset) {
                    return elseDir
                }
            }

            return null
        }

        /**
         * Checks if `later` is defined after `earlier` in the code.
         * Returns true if `later` should be warned about as a redefinition.
         */
        private fun isDefinedAfter(later: NasmNamedElement, earlier: NasmNamedElement): Boolean {
            val laterFile = later.containingFile
            val earlierFile = earlier.containingFile

            // If in different files, the definition in the current file being inspected
            // is overriding an included file's definition - this should warn
            if (laterFile != earlierFile) {
                // Only warn if `later` is in the file being inspected (not in an include)
                return laterFile == holder.file
            }

            // Same file - check text offsets
            return later.textOffset > earlier.textOffset
        }

        /**
         * Gets a unique key for the macro that includes parameter count for multi-line macros.
         * In NASM, multi-line macros with the same name but different parameter counts are distinct.
         */
        private fun getMacroKey(element: NasmNamedElement): String {
            val name = element.name ?: return ""

            return when (element) {
                is NasmMultiLineMacro -> {
                    // For multi-line macros, include parameter count in the key
                    val paramCountText = element.paramCount?.text ?: "0"
                    "$name:$paramCountText"
                }
                else -> {
                    // For single-line macros (%define, etc.), just use the name
                    name
                }
            }
        }

        /**
         * Checks if the given element is a macro definition (excluding %assign).
         */
        private fun isMacroDefinition(element: PsiElement): Boolean {
            return when (element) {
                is NasmMultiLineMacro -> true    // %macro, %imacro, etc.
                is NasmPpDefineStmt -> true      // %define, %xdefine, etc.
                is NasmPpAssignStmt -> false     // %assign - explicitly excluded
                else -> false
            }
        }

        /**
         * Gets a human-readable description of where the original definition is located.
         */
        private fun getLocationDescription(element: NasmNamedElement): String {
            val containingFile = element.containingFile
            val currentFile = holder.file

            // Get line number (1-based)
            val document = containingFile.viewProvider.document
            val lineNumber = if (document != null) {
                val offset = element.textOffset
                document.getLineNumber(offset) + 1
            } else {
                null
            }

            return when {
                // Same file - just show line number
                containingFile == currentFile && lineNumber != null -> {
                    " at line $lineNumber"
                }
                // Different file - show filename and line number
                containingFile != currentFile && lineNumber != null -> {
                    " in ${containingFile.name} at line $lineNumber"
                }
                // Different file - just show filename
                containingFile != currentFile -> {
                    " in ${containingFile.name}"
                }
                // Fallback
                else -> ""
            }
        }
    }

    override fun getDisplayName(): String = "Macro redefinition"

    override fun getGroupDisplayName(): String = "NASM"

    override fun getShortName(): String = "NasmMacroRedefinition"

    override fun getStaticDescription(): String? {
        return "Reports when a macro is defined multiple times. Does not apply to %assign directives."
    }

    override fun isEnabledByDefault(): Boolean = true
}

/**
 * Quick fix that removes the redefined macro definition.
 */
private class RemoveMacroDefinitionFix : LocalQuickFix {

    override fun getFamilyName(): String = "Remove macro definition"

    override fun getName(): String = "Remove macro definition"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement?.parent as? NasmNamedElement ?: return

        when (element) {
            is NasmMultiLineMacro -> {
                // For multi-line macros, delete the entire macro block including %endmacro
                // Find the parent statement that contains the macro definition
                val statement = element.parent
                if (statement != null) {
                    statement.delete()
                } else {
                    element.delete()
                }
            }
            is NasmPpDefineStmt -> {
                // For %define statements, delete the entire statement line
                val statement = element.parent
                if (statement != null) {
                    statement.delete()
                } else {
                    element.delete()
                }
            }
            else -> {
                // Fallback: just delete the element
                element.delete()
            }
        }
    }
}

/**
 * Quick fix that navigates to the original definition of a redefined macro.
 */
private class NavigateToOriginalDefinitionFix(
    private val originalDefinition: NasmNamedElement
) : LocalQuickFix {

    override fun getFamilyName(): String = "Navigate to original definition"

    override fun getName(): String {
        val file = originalDefinition.containingFile
        val fileName = file?.name ?: "unknown"
        return "Navigate to original definition in $fileName"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        // Navigate to the original definition
        val element = originalDefinition as? PsiElement ?: return
        val containingFile = element.containingFile?.virtualFile ?: return
        val offset = element.textOffset

        val fileDescriptor = OpenFileDescriptor(project, containingFile, offset)
        FileEditorManager.getInstance(project).openTextEditor(fileDescriptor, true)
    }
}
