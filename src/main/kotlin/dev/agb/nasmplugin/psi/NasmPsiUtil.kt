package dev.agb.nasmplugin.psi

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import java.io.File

/**
 * Utility methods for working with NASM PSI elements, provided as extension functions
 */

private val LOG = Logger.getInstance("dev.agb.nasmplugin.psi.NasmPsiUtil")

// ===== Find methods (extension functions on PsiFile) =====

fun PsiFile.findLabels(): List<NasmLabelDef> =
    PsiTreeUtil.findChildrenOfType(this, NasmLabelDef::class.java).toList()

fun PsiFile.findDataDefs(): List<NasmDataDef> =
    PsiTreeUtil.findChildrenOfType(this, NasmDataDef::class.java).toList()

fun PsiFile.findEquDefinitions(): List<NasmEquDefinition> =
    PsiTreeUtil.findChildrenOfType(this, NasmEquDefinition::class.java).toList()

fun PsiFile.findMultiLineMacros(): List<NasmMultiLineMacro> =
    PsiTreeUtil.findChildrenOfType(this, NasmMultiLineMacro::class.java).toList()

fun PsiFile.findPpDefineStmts(): List<NasmPpDefineStmt> =
    PsiTreeUtil.findChildrenOfType(this, NasmPpDefineStmt::class.java).toList()

@Deprecated(
    message = "Use findPpDefineStmts() instead for clarity",
    replaceWith = ReplaceWith("findPpDefineStmts()"),
    level = DeprecationLevel.WARNING
)
fun PsiFile.findSingleLineMacros(): List<NasmPpDefineStmt> = findPpDefineStmts()

fun PsiFile.findPpAssignStmts(): List<NasmPpAssignStmt> =
    PsiTreeUtil.findChildrenOfType(this, NasmPpAssignStmt::class.java).toList()

fun PsiFile.findExternDirs(): List<NasmExternDir> =
    PsiTreeUtil.findChildrenOfType(this, NasmExternDir::class.java).toList()

fun PsiFile.findExternSymbols(): List<NasmSymbolDecl> =
    findExternDirs().flatMap { externDir ->
        externDir.symbolList?.symbolDeclList ?: emptyList()
    }

fun PsiFile.findGlobalDirs(): List<NasmGlobalDir> =
    PsiTreeUtil.findChildrenOfType(this, NasmGlobalDir::class.java).toList()

fun PsiFile.findGlobalSymbols(): List<NasmSymbolDecl> =
    findGlobalDirs().flatMap { globalDir ->
        globalDir.symbolList?.symbolDeclList ?: emptyList()
    }

fun PsiFile.findAllNamedElements(): List<NasmNamedElement> = buildList {
    addAll(findLabels())
    addAll(findEquDefinitions())
    addAll(findMultiLineMacros())
    addAll(findPpDefineStmts())
    // For %assign, only keep the first definition of each symbol to avoid duplicates in structure view
    val assignsByName = findPpAssignStmts().groupBy { it.name }
    assignsByName.values.forEach { assigns ->
        // Only add the first (declaration) assignment for each name
        assigns.firstOrNull()?.let { add(it) }
    }
    // Add extern symbols
    addAll(findExternSymbols())
}

// ===== Find by name =====

fun PsiFile.findLabelDefinition(labelName: String, referenceElement: PsiElement? = null): PsiElement? {
    // Handle macro-local labels (starts with %%)
    if (labelName.startsWith("%%") && referenceElement != null) {
        // Find the parent macro for this reference
        val parentMacro = findParentMacro(referenceElement)
        if (parentMacro != null) {
            // Search for the macro-local label only within the parent macro's scope
            return findMacroLocalLabelInScope(parentMacro, labelName)
        }
    }

    // Handle explicit global.local label references (e.g., "global_label.local")
    if (labelName.contains('.') && !labelName.startsWith('.')) {
        val parts = labelName.split('.', limit = 2)
        if (parts.size == 2) {
            val globalName = parts[0]
            val localName = "." + parts[1]

            // Find the global label
            val globalLabel = findLabels().firstOrNull { it.name == globalName }
            if (globalLabel != null) {
                // Find the local label within the scope of this global label
                return findLocalLabelInScope(globalLabel, localName)
            }
        }
    }

    // If this is a local label reference (starts with .)
    if (labelName.startsWith('.') && referenceElement != null) {
        // Find the parent global label for this reference
        val parentGlobalLabel = findParentGlobalLabel(referenceElement)
        if (parentGlobalLabel != null) {
            // Search for the local label only within the parent global label's scope
            return findLocalLabelInScope(parentGlobalLabel, labelName)
        }
    }

    // Otherwise, do a simple search for the label name
    return findLabels().firstOrNull { it.name == labelName }
}

/**
 * Find the parent macro that contains this element.
 * Returns the macro definition that contains the reference element.
 */
private fun findParentMacro(element: PsiElement): NasmMultiLineMacro? {
    return PsiTreeUtil.getParentOfType(element, NasmMultiLineMacro::class.java)
}

/**
 * Find a macro-local label within the scope of a macro
 */
private fun findMacroLocalLabelInScope(macro: NasmMultiLineMacro, macroLocalLabelName: String): NasmLabelDef? {
    // Find all labels within the macro
    val labels = PsiTreeUtil.findChildrenOfType(macro, NasmLabelDef::class.java)

    // Find the macro-local label with the matching name
    return labels.firstOrNull { label ->
        label.name == macroLocalLabelName
    }
}

/**
 * Find the parent global label (non-local label) that contains this element.
 * Returns the last global label that appears before the reference element in the file.
 */
private fun findParentGlobalLabel(element: PsiElement): NasmLabelDef? {
    val file = element.containingFile ?: return null
    val referenceOffset = element.textOffset

    // Get all labels in the file
    val allLabels = file.findLabels()

    // Find the last global label (non-local) that appears before this reference
    return allLabels
        .asReversed()  // Start from the end
        .firstOrNull { label ->
            val labelName = label.name
            // Must be a global label (not starting with '.' or '%%')
            // Must appear before the reference in the file
            labelName != null &&
            !labelName.startsWith('.') &&
            !labelName.startsWith("%%") &&
            label.textOffset < referenceOffset
        }
}

/**
 * Find a local label within the scope of a global label
 */
private fun findLocalLabelInScope(globalLabel: NasmLabelDef, localLabelName: String): NasmLabelDef? {
    val file = globalLabel.containingFile ?: return null
    val labels = file.findLabels()

    // Find the index of the global label
    val globalIndex = labels.indexOf(globalLabel)
    if (globalIndex == -1) return null

    // Search from the global label until the next global label
    for (i in (globalIndex + 1) until labels.size) {
        val label = labels[i]
        val labelName = label.name

        if (labelName != null) {
            if (!labelName.startsWith('.')) {
                // We've reached the next global label, stop searching
                break
            }
            // Skip macro-local labels (they have their own scope)
            if (labelName.startsWith("%%")) {
                continue
            }
            if (labelName == localLabelName) {
                return label
            }
        }
    }

    return null
}

fun PsiFile.findDataDefinition(labelName: String): PsiElement? =
    findEquDefinitions().firstOrNull { it.name == labelName }

fun PsiFile.findMacroDefinition(macroName: String): PsiElement? =
    findMultiLineMacros().firstOrNull { it.name == macroName }
        ?: findPpDefineStmts().firstOrNull { it.name == macroName }
        ?: findPpAssignStmts().firstOrNull { it.name == macroName }

fun PsiFile.findExternDefinition(symbolName: String): PsiElement? =
    findExternSymbols().firstOrNull { (it as? NasmNamedElement)?.name == symbolName }

/**
 * Finds a global label definition across all NASM files in the project.
 * This searches for a label that is marked as global in any NASM file.
 * @param symbolName The name of the symbol to find
 * @return The label definition if found, or null
 */
fun PsiFile.findGlobalLabelInProject(symbolName: String): PsiElement? {
    val scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)
    val psiManager = com.intellij.psi.PsiManager.getInstance(project)

    // Search for all files with .asm, .inc, .s extensions
    val extensions = listOf("asm", "inc", "s")
    for (ext in extensions) {
        val files = com.intellij.psi.search.FilenameIndex.getAllFilesByExt(project, ext, scope)
        for (virtualFile in files) {
            val psiFile = psiManager.findFile(virtualFile) ?: continue

            // Check if this file has a global declaration for this symbol (case-insensitive)
            val globalSymbols = psiFile.findGlobalSymbols()

            val hasGlobalDecl = globalSymbols.any {
                (it as? NasmNamedElement)?.name?.equals(symbolName, ignoreCase = true) == true
            }

            if (hasGlobalDecl) {
                // Found a global declaration, now find the actual label definition
                val labelDef = psiFile.findLabelDefinition(symbolName)
                if (labelDef != null) {
                    return labelDef
                }
            }
        }
    }

    return null
}

// ===== Include file handling =====

fun PsiFile.findIncludeStatements(): List<NasmIncludeDir> =
    PsiTreeUtil.findChildrenOfType(this, NasmIncludeDir::class.java).toList()

fun NasmIncludeDir.getIncludePath(): String? {
    // First try to find a direct STRING token (for older grammar compatibility)
    var stringToken = node.findChildByType(NasmTypes.STRING)?.psi

    // If not found directly, look for string_or_env child which contains STRING
    if (stringToken == null) {
        // Find the string_or_env child
        val stringOrEnv = children.find { it is NasmStringOrEnv }
        if (stringOrEnv != null) {
            // Look for STRING token inside string_or_env
            stringToken = stringOrEnv.node.findChildByType(NasmTypes.STRING)?.psi

            // Also handle <IDENTIFIER> case (environment variable)
            if (stringToken == null) {
                val ltToken = stringOrEnv.node.findChildByType(NasmTypes.LT)
                val gtToken = stringOrEnv.node.findChildByType(NasmTypes.GT)
                val identToken = stringOrEnv.node.findChildByType(NasmTypes.IDENTIFIER)

                if (ltToken != null && gtToken != null && identToken != null) {
                    // Return the identifier as the path (without angle brackets)
                    return identToken.psi.text
                }
            }
        }
    }

    if (stringToken == null) return null

    val text = stringToken.text
    // Remove quotes
    if (text.length >= 2 && (text.startsWith("\"") || text.startsWith("'"))) {
        return text.substring(1, text.length - 1)
    }
    return text
}

fun PsiFile.resolveIncludeFile(includePath: String): PsiFile? {
    // During completion, the PsiFile might be a copy without a virtualFile
    // Try to get the original file's virtualFile
    val currentVirtualFile = virtualFile ?: originalFile?.virtualFile ?: return null
    val parentDir = currentVirtualFile.parent ?: return null
    val psiManager = PsiManager.getInstance(project)

    // First, try to resolve relative to the current file's directory
    val includedFile = parentDir.findFileByRelativePath(includePath)
    if (includedFile != null && includedFile.exists()) {
        return psiManager.findFile(includedFile)
    }

    // If not found, try the include paths from compilation database (if available)
    return try {
        // Check if include path resolver service is available
        val resolver = project.getService(dev.agb.nasmplugin.navigation.NasmIncludePathResolver::class.java)
            ?: return null

        val includePaths = resolver.getNasmIncludePaths(currentVirtualFile)
        if (includePaths.isEmpty()) {
            return null
        }

        // Search each include path for the file
        for (includePathDir in includePaths) {
            val file = File(includePathDir, includePath)
            if (file.exists() && file.isFile) {
                val vFile = LocalFileSystem.getInstance().findFileByPath(file.absolutePath)
                if (vFile != null) {
                    return psiManager.findFile(vFile)
                }
            }
        }

        null
    } catch (e: Exception) {
        // Silently ignore - likely running in non-CLion IDE without compilation database support
        null
    }
}

fun PsiFile.getIncludedFiles(): List<PsiFile> {
    return com.intellij.psi.util.CachedValuesManager.getCachedValue(this) {
        val visited = mutableSetOf<PsiFile>()
        val result = mutableListOf<PsiFile>()
        collectIncludedFiles(visited, result)

        com.intellij.psi.util.CachedValueProvider.Result.create(
            result,
            com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT
        )
    }
}

private fun PsiFile.collectIncludedFiles(visited: MutableSet<PsiFile>, result: MutableList<PsiFile>) {
    if (this in visited) return // Prevent infinite recursion
    visited.add(this)

    for (includeStmt in findIncludeStatements()) {
        val includePath = includeStmt.getIncludePath()
        if (includePath != null) {
            val includedFile = resolveIncludeFile(includePath)
            if (includedFile != null && includedFile !in visited) {
                result.add(includedFile)
                // Recursively collect files included by this file
                includedFile.collectIncludedFiles(visited, result)
            }
        }
    }
}

/**
 * Find all files that include this file (directly or transitively).
 * This is the reverse of getIncludedFiles() - it finds files that reference this file.
 *
 * This uses CachedValuesManager to cache results per file, invalidated on PSI changes.
 */
fun PsiFile.getIncludingFiles(): List<PsiFile> {
    return com.intellij.psi.util.CachedValuesManager.getCachedValue(this) {
        val result = mutableListOf<PsiFile>()
        val targetPath = virtualFile?.path ?: return@getCachedValue com.intellij.psi.util.CachedValueProvider.Result.create(
            emptyList(),
            com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT
        )

        // Find all NASM files in the project
        val scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)

        // Search for all files with .asm, .inc, .s extensions
        val extensions = listOf("asm", "inc", "s")
        for (ext in extensions) {
            val files = com.intellij.psi.search.FilenameIndex.getAllFilesByExt(project, ext, scope)
            for (virtualFile in files) {
                val psiFile = psiManager.findFile(virtualFile) ?: continue
                if (psiFile == this) continue // Don't include self

                // Check if this file includes our target file (directly or transitively)
                val includedFiles = psiFile.getIncludedFiles()
                if (this in includedFiles) {
                    result.add(psiFile)
                }
            }
        }

        // Cache the result and invalidate when PSI changes
        com.intellij.psi.util.CachedValueProvider.Result.create(
            result,
            com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT
        )
    }
}

// ===== Find with includes =====

fun PsiFile.findAllNamedElementsWithIncludes(): List<NasmNamedElement> = buildList {
    addAll(findAllNamedElements())
    for (includedFile in getIncludedFiles()) {
        addAll(includedFile.findAllNamedElements())
    }
}

fun PsiFile.findLabelDefinitionWithIncludes(labelName: String, referenceElement: PsiElement? = null): PsiElement? =
    findLabelDefinition(labelName, referenceElement)
        ?: getIncludedFiles().firstNotNullOfOrNull { it.findLabelDefinition(labelName, null) }

fun PsiFile.findDataDefinitionWithIncludes(labelName: String): PsiElement? =
    findDataDefinition(labelName)
        ?: getIncludedFiles().firstNotNullOfOrNull { it.findDataDefinition(labelName) }

fun PsiFile.findMacroDefinitionWithIncludes(macroName: String): PsiElement? =
    findMacroDefinition(macroName)
        ?: getIncludedFiles().firstNotNullOfOrNull { it.findMacroDefinition(macroName) }

fun PsiFile.findExternDefinitionWithIncludes(symbolName: String): PsiElement? =
    findExternDefinition(symbolName)
        ?: getIncludedFiles().firstNotNullOfOrNull { it.findExternDefinition(symbolName) }

// ===== Conditional compilation support =====

/**
 * Checks if this PSI element is inside an inactive conditional branch.
 * Returns true if the element should be treated as disabled/greyed out code.
 *
 * This respects the inspection settings - if the inspection is disabled,
 * all code is considered active.
 */
fun PsiElement.isInInactiveConditionalBranch(): Boolean {
    try {
        // Prevent circular dependency: If we're currently evaluating preprocessor conditionals,
        // don't try to check if we're in an inactive branch (that's what we're trying to determine!)
        if (dev.agb.nasmplugin.eval.NasmConstExprEvaluator.isInsidePreprocessorEvaluation()) {
            return false
        }

        // Check if the inspection is enabled
        val profile = com.intellij.profile.codeInspection.InspectionProjectProfileManager
            .getInstance(project).currentProfile
        profile.getInspectionTool("NasmInactiveConditionalBranch", this) ?: return false

        // If inspection is not found or disabled, treat all code as active

        val file = containingFile ?: return false
        val evaluator = dev.agb.nasmplugin.preprocessor.PreprocessorStateEvaluator.getInstance(file.project)
        val branches = evaluator.evaluateFile(file)

        val elementOffset = textRange.startOffset
        return branches.any { branch ->
            !branch.isActive && branch.textRange.contains(elementOffset)
        }
    } catch (e: Exception) {
        // If anything goes wrong, treat code as active (fail safe)
        return false
    }
}

/**
 * Find all conditional blocks in a file.
 */
fun PsiFile.findConditionalBlocks(): List<NasmConditionalBlock> =
    PsiTreeUtil.findChildrenOfType(this, NasmConditionalBlock::class.java).toList()

// ===== Rep block support =====

/**
 * Checks if this PSI element is inside a %rep block.
 * Used to determine if %assign statements inside %rep are used in subsequent iterations.
 */
fun PsiElement.isInsideRepBlock(): Boolean {
    return PsiTreeUtil.getParentOfType(this, NasmRepBlock::class.java) != null
}

// ===== Java compatibility layer (static methods for existing Java code) =====

@Suppress("unused")
object NasmPsiUtil {
    @JvmStatic
    fun findLabels(file: PsiFile): List<NasmLabelDef> = file.findLabels()

    @JvmStatic
    fun findDataDefs(file: PsiFile): List<NasmDataDef> = file.findDataDefs()

    @JvmStatic
    fun findEquDefinitions(file: PsiFile): List<NasmEquDefinition> = file.findEquDefinitions()

    @JvmStatic
    fun findMultiLineMacros(file: PsiFile): List<NasmMultiLineMacro> = file.findMultiLineMacros()

    @JvmStatic
    fun findPpDefineStmts(file: PsiFile): List<NasmPpDefineStmt> = file.findPpDefineStmts()

    @JvmStatic
    @Deprecated(
        message = "Use findPpDefineStmts() instead for clarity",
        replaceWith = ReplaceWith("findPpDefineStmts(file)"),
        level = DeprecationLevel.WARNING
    )
    fun findSingleLineMacros(file: PsiFile): List<NasmPpDefineStmt> = file.findPpDefineStmts()

    @JvmStatic
    fun findPpAssignStmts(file: PsiFile): List<NasmPpAssignStmt> = file.findPpAssignStmts()

    @JvmStatic
    fun findExternDirs(file: PsiFile): List<NasmExternDir> = file.findExternDirs()

    @JvmStatic
    fun findAllNamedElements(file: PsiFile): List<NasmNamedElement> = file.findAllNamedElements()

    @JvmStatic
    @JvmOverloads
    fun findLabelDefinition(file: PsiFile, labelName: String, referenceElement: PsiElement? = null): PsiElement? =
        file.findLabelDefinition(labelName, referenceElement)

    @JvmStatic
    fun findDataDefinition(file: PsiFile, labelName: String): PsiElement? = file.findDataDefinition(labelName)

    @JvmStatic
    fun findMacroDefinition(file: PsiFile, macroName: String): PsiElement? = file.findMacroDefinition(macroName)

    @JvmStatic
    fun findIncludeStatements(file: PsiFile): List<NasmIncludeDir> = file.findIncludeStatements()

    @JvmStatic
    fun getIncludePath(includeStmt: NasmIncludeDir): String? = includeStmt.getIncludePath()

    @JvmStatic
    fun resolveIncludeFile(currentFile: PsiFile, includePath: String): PsiFile? =
        currentFile.resolveIncludeFile(includePath)

    @JvmStatic
    fun getIncludedFiles(file: PsiFile): List<PsiFile> = file.getIncludedFiles()

    @JvmStatic
    fun findAllNamedElementsWithIncludes(file: PsiFile): List<NasmNamedElement> =
        file.findAllNamedElementsWithIncludes()

    @JvmStatic
    @JvmOverloads
    fun findLabelDefinitionWithIncludes(file: PsiFile, labelName: String, referenceElement: PsiElement? = null): PsiElement? =
        file.findLabelDefinitionWithIncludes(labelName, referenceElement)

    @JvmStatic
    fun findDataDefinitionWithIncludes(file: PsiFile, labelName: String): PsiElement? =
        file.findDataDefinitionWithIncludes(labelName)

    @JvmStatic
    fun findMacroDefinitionWithIncludes(file: PsiFile, macroName: String): PsiElement? =
        file.findMacroDefinitionWithIncludes(macroName)
}
