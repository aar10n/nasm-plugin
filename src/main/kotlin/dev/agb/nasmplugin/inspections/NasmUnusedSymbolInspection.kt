package dev.agb.nasmplugin.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiModificationTracker
import dev.agb.nasmplugin.psi.*

/**
 * Inspection that detects unused symbols (labels, macros, EQU constants).
 * Excludes entry points, symbols declared with 'global' directive, and symbols
 * in inactive conditional branches.
 */
class NasmUnusedSymbolInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return NasmUnusedSymbolVisitor(holder)
    }

    private class NasmUnusedSymbolVisitor(private val holder: ProblemsHolder) : PsiElementVisitor() {

        // Cache for global symbols in the file - computed on first access
        private var globalSymbols: Set<String>? = null

        override fun visitElement(element: PsiElement) {
            super.visitElement(element)

            // Only check named elements that are definitions
            if (element !is NasmNamedElement) return

            // Skip symbol declarations in global/extern directives - these are not definitions
            if (element is NasmSymbolDecl) return

            // Skip if this is not a definition (e.g., it's a reference)
            if (element.nameIdentifier == null) return

            val name = element.name ?: return

            // Skip common entry points
            if (isEntryPoint(name)) return

            // Skip symbols declared as global
            if (globalSymbols == null) {
                globalSymbols = findGlobalSymbols(element.containingFile)
            }
            if (name in globalSymbols!!) return

            // Skip local labels (start with .) as they have limited scope
            if (name.startsWith(".")) return

            // Skip symbols in inactive conditional branches
            if (element.isInInactiveConditionalBranch()) return

            // Skip %assign statements inside %rep blocks - they're used in subsequent iterations
            if (element is NasmPpAssignStmt && element.isInsideRepBlock()) return

            // Use cached result for usage search
            // Uses element.useScope which includes cross-file references via include chain
            val hasUsages = CachedValuesManager.getCachedValue(element) {
                val searchScope = element.useScope
                val usages = ReferencesSearch.search(element, searchScope).findFirst()
                CachedValueProvider.Result.create(
                    usages != null,
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }

            if (!hasUsages) {
                val elementType = when (element) {
                    is NasmLabelDef -> "Label"
                    is NasmMultiLineMacro -> "Macro"
                    is NasmPpDefineStmt -> "Macro"
                    is NasmEquDefinition -> "Constant"
                    is NasmPpAssignStmt -> "Variable"
                    else -> "Symbol"
                }

                holder.registerProblem(
                    element.nameIdentifier ?: element,
                    "$elementType '$name' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                )
            }
        }

        private fun isEntryPoint(name: String): Boolean {
            return name in setOf("_start", "main", "START", "MAIN", "_main")
        }

        private fun findGlobalSymbols(file: PsiFile): Set<String> {
            val symbols = mutableSetOf<String>()

            // Find all global directives in the file
            file.accept(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    super.visitElement(element)

                    if (element is NasmGlobalDir) {
                        // Extract symbol names from the global directive
                        // Format: global symbol1, symbol2, symbol3
                        element.symbolList.symbolDeclList.forEach { symbolDecl ->
                            symbolDecl.name?.let { symbols.add(it) }
                        }
                    }
                }
            })

            return symbols
        }
    }
}
