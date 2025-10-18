package dev.agb.nasmplugin.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.settings.CommandLineMacroProvider

/**
 * Centralized symbol resolution utility for NASM elements.
 * Consolidates duplicate resolution logic from multiple reference implementations.
 */
object NasmSymbolResolver {

    enum class SearchType {
        LABELS,
        DATA_DEFINITIONS,  // EQU constants
        MULTI_LINE_MACROS,
        SINGLE_LINE_MACROS,  // %define, %xdefine, etc.
        ASSIGNS,  // %assign
        COMMAND_LINE_MACROS,  // Macros from project settings (-D flag)
        EXTERNS,
        GLOBAL_LABELS;  // Labels marked as global in other files

        companion object {
            val ALL = entries.toSet()
            val MACROS = setOf(MULTI_LINE_MACROS, SINGLE_LINE_MACROS, ASSIGNS, COMMAND_LINE_MACROS)
        }
    }

    /**
     * Resolves a symbol name to its definition.
     * @param name The symbol name to resolve
     * @param file The file to start searching from
     * @param context Optional context element for local label resolution
     * @param searchTypes The types of elements to search for
     * @param includeIncludes Whether to search in included files
     * @return The resolved element or null if not found
     */
    fun resolve(
        name: String,
        file: PsiFile,
        context: PsiElement? = null,
        searchTypes: Set<SearchType> = SearchType.ALL,
        includeIncludes: Boolean = true
    ): PsiElement? {
        // First, check command-line macros (these have lowest precedence)
        // Command-line macros are checked first but source definitions override them
        val commandLineMacroResult = if (SearchType.COMMAND_LINE_MACROS in searchTypes) {
            resolveCommandLineMacro(name, file)
        } else null

        // Then search in the main file (source definitions override command-line)
        resolveInFile(name, file, context, searchTypes)?.let { return it }

        // Then search in included files if requested
        if (includeIncludes) {
            file.getIncludedFiles().forEach { includedFile ->
                resolveInFile(name, includedFile, context, searchTypes)?.let { return it }
            }
        }

        // Search for global labels in other assembly files
        if (SearchType.GLOBAL_LABELS in searchTypes) {
            file.findGlobalLabelInProject(name)?.let {
                return it
            }
        }

        // Finally, fall back to command-line macro if no source definition found
        return commandLineMacroResult
    }

    /**
     * Resolves a symbol within a single file.
     */
    private fun resolveInFile(
        name: String,
        file: PsiFile,
        context: PsiElement?,
        searchTypes: Set<SearchType>
    ): PsiElement? {
        // Try to find as data definition (EQU constants)
        if (SearchType.DATA_DEFINITIONS in searchTypes) {
            file.findDataDefinition(name)?.let {
                if (!it.isInInactiveConditionalBranch()) return it
            }
        }

        // Try as code label - use the sophisticated label resolution from NasmPsiUtil
        // which properly handles local labels with scope
        if (SearchType.LABELS in searchTypes) {
            file.findLabelDefinition(name, context)?.let {
                if (!it.isInInactiveConditionalBranch()) return it
            }
        }

        // Try as extern declaration early (before checking other files)
        // Externs in the current file should take precedence over global labels in other files
        if (SearchType.EXTERNS in searchTypes) {
            file.findExternDefinition(name)?.let {
                if (!it.isInInactiveConditionalBranch()) return it
            }
        }

        // Try as multi-line macro definition
        if (SearchType.MULTI_LINE_MACROS in searchTypes) {
            file.findMultiLineMacros().firstOrNull {
                (it as? NasmNamedElement)?.name?.equals(name, ignoreCase = true) == true &&
                !it.isInInactiveConditionalBranch()
            }?.let { return it }
        }

        // Try as single-line macro (%define and variants)
        if (SearchType.SINGLE_LINE_MACROS in searchTypes) {
            file.findPpDefineStmts().firstOrNull {
                (it as? NasmNamedElement)?.name?.equals(name, ignoreCase = true) == true &&
                !it.isInInactiveConditionalBranch()
            }?.let { return it }
        }

        // Try as %assign statement
        if (SearchType.ASSIGNS in searchTypes) {
            file.findPpAssignStmts().firstOrNull {
                (it as? NasmNamedElement)?.name?.equals(name, ignoreCase = true) == true &&
                !it.isInInactiveConditionalBranch()
            }?.let { return it }
        }

        return null
    }

    /**
     * Convenience method for resolving macros only.
     */
    fun resolveMacro(name: String, file: PsiFile, includeIncludes: Boolean = true): PsiElement? {
        return resolve(name, file, null, SearchType.MACROS, includeIncludes)
    }

    /**
     * Convenience method for resolving labels with context.
     */
    fun resolveLabel(name: String, file: PsiFile, context: PsiElement?, includeIncludes: Boolean = true): PsiElement? {
        return resolve(name, file, context, setOf(SearchType.LABELS), includeIncludes)
    }

    /**
     * Resolves a command-line macro from project settings.
     */
    private fun resolveCommandLineMacro(name: String, file: PsiFile): PsiElement? {
        val project = file.project
        val provider = CommandLineMacroProvider.getInstance(project)
        val macro = provider.findMacroByName(name) ?: return null
        return NasmCommandLineMacroElement(macro, file)
    }
}
