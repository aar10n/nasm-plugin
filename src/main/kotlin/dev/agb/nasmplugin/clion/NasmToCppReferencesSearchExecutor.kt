package dev.agb.nasmplugin.clion

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.jetbrains.cidr.lang.psi.OCDeclarator
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.psi.NasmExternDir
import dev.agb.nasmplugin.psi.NasmFile
import dev.agb.nasmplugin.psi.NasmSymbolDecl

/**
 * Contributes NASM extern symbol references when searching for usages of C/C++ declarations.
 */
class NasmToCppReferencesSearchExecutor : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val target = queryParameters.elementToSearch

        // Only handle C/C++ declarators (functions, variables, etc.)
        if (target !is OCDeclarator) {
            return
        }

        val symbolName = target.name
        val project = target.project

        // Search scope for NASM files - always use project scope for cross-language search
        val searchScope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)
        val nasmFiles = com.intellij.psi.search.FileTypeIndex.getFiles(NasmFileType, searchScope)

        // Search each NASM file for extern declarations matching the symbol name
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)

        for (virtualFile in nasmFiles) {
            val nasmFile = psiManager.findFile(virtualFile) as? NasmFile ?: continue

            // Find all extern directives in the file
            val externDirs = PsiTreeUtil.findChildrenOfType(nasmFile, NasmExternDir::class.java)

            for (externDir in externDirs) {
                // Find symbol declarations within this extern directive
                val symbolDecls = PsiTreeUtil.findChildrenOfType(externDir, NasmSymbolDecl::class.java)

                for (symbolDecl in symbolDecls) {
                    if (symbolDecl.name == symbolName) {
                        // Create a reference from the NASM extern symbol to the C/C++ declaration
                        val reference = NasmToCppReference(symbolDecl, target)
                        consumer.process(reference)
                    }
                }
            }
        }
    }

    /**
     * Lightweight reference object representing a NASM extern symbol that references a C/C++ declaration.
     * Supports cross-language rename refactoring.
     */
    private class NasmToCppReference(
        element: NasmSymbolDecl,
        private val cppTarget: OCDeclarator
    ) : PsiReferenceBase<NasmSymbolDecl>(element, true) {

        override fun resolve(): PsiElement = cppTarget

        override fun handleElementRename(newElementName: String): PsiElement {
            // Support renaming C/C++ symbols from NASM code
            // Use the element manipulator to update the NASM symbol name
            return com.intellij.psi.ElementManipulators.handleContentChange(element, newElementName)
                ?: element
        }

        override fun bindToElement(element: PsiElement): PsiElement {
            // Not needed for our use case
            return this.element
        }

        override fun isReferenceTo(element: PsiElement): Boolean {
            return element == cppTarget
        }
    }
}
