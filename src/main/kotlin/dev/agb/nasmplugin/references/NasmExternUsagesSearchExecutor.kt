package dev.agb.nasmplugin.references

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import dev.agb.nasmplugin.psi.NasmExternDir
import dev.agb.nasmplugin.psi.NasmFile
import dev.agb.nasmplugin.psi.NasmSymbolDecl
import dev.agb.nasmplugin.psi.NasmSymbolRef

/**
 * Enhances find usages for extern declarations.
 *
 * This executor handles two scenarios when finding usages:
 *
 * **Case 1 - Direct extern search:**
 * When the user invokes "Find Usages" directly on an extern declaration like "extern my_function",
 * this finds all local usages in the same file (e.g., "call my_function", "je my_function")
 * that resolve to the extern declaration.
 *
 * **Case 2 - Indirect search via target:**
 * When IntelliJ follows the extern's reference to the global label/function definition and
 * searches for that target, this executor:
 * 1. Detects that we're searching for a label/function
 * 2. Finds all extern declarations that reference that target
 * 3. Includes the local usages of those extern declarations in the results
 *
 * This ensures that extern declarations work as both:
 * - A declaration (for local usages in the same file)
 * - A reference (to the external definition)
 *
 * Without this executor, find usages on an extern would only show usages of the final target,
 * not the local usages that reference the extern itself.
 */
class NasmExternUsagesSearchExecutor : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val target = queryParameters.elementToSearch

        // Case 1: Direct extern declaration search
        if (target is NasmSymbolDecl) {
            val externDir = PsiTreeUtil.getParentOfType(target, NasmExternDir::class.java)
            if (externDir != null) {
                findUsagesOfExtern(target, consumer)
                return
            }
        }

        // Case 2: Searching for a label/function - find externs that point to it
        val symbolName = (target as? com.intellij.psi.PsiNamedElement)?.name
        if (symbolName != null) {
            findExternsPointingToTarget(target, symbolName, consumer)
        }
    }

    /**
     * Finds all local usages of an extern declaration in its file.
     */
    private fun findUsagesOfExtern(externDecl: NasmSymbolDecl, consumer: Processor<in PsiReference>) {
        val symbolName = externDecl.name ?: return
        val file = externDecl.containingFile as? NasmFile ?: return

        // Find all symbol references in the same file
        val symbolRefs = PsiTreeUtil.findChildrenOfType(file, NasmSymbolRef::class.java)

        for (symbolRef in symbolRefs) {
            if (symbolRef.text == symbolName) {
                val reference = symbolRef.reference
                if (reference != null && reference.resolve() == externDecl) {
                    consumer.process(reference)
                }
            }
        }
    }

    /**
     * When searching for a global label/function, finds extern declarations that
     * reference it and includes their local usages.
     */
    private fun findExternsPointingToTarget(
        target: PsiElement,
        symbolName: String,
        consumer: Processor<in PsiReference>
    ) {
        val project = target.project

        // Search all NASM files for extern declarations
        val scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)
        val nasmFiles = com.intellij.psi.search.FileTypeIndex.getFiles(dev.agb.nasmplugin.NasmFileType, scope)
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)

        for (virtualFile in nasmFiles) {
            val nasmFile = psiManager.findFile(virtualFile) as? NasmFile ?: continue

            // Find extern directives with matching symbol name
            val externDirs = PsiTreeUtil.findChildrenOfType(nasmFile, NasmExternDir::class.java)
            for (externDir in externDirs) {
                val symbolDecls = PsiTreeUtil.findChildrenOfType(externDir, NasmSymbolDecl::class.java)
                for (symbolDecl in symbolDecls) {
                    if (symbolDecl.name == symbolName) {
                        // Check if this extern resolves to our target
                        val externRef = symbolDecl.reference
                        if (externRef != null && externRef.resolve() == target) {
                            // Include local usages of this extern
                            findUsagesOfExtern(symbolDecl, consumer)
                        }
                    }
                }
            }
        }
    }
}
