package dev.agb.nasmplugin.clion

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.jetbrains.cidr.lang.OCFileType
import com.jetbrains.cidr.lang.psi.OCDeclarator
import com.jetbrains.cidr.lang.psi.OCFile
import dev.agb.nasmplugin.psi.NasmFile
import dev.agb.nasmplugin.psi.NasmGlobalDir
import dev.agb.nasmplugin.psi.NasmGlobalLabel
import dev.agb.nasmplugin.psi.NasmLabelDef
import dev.agb.nasmplugin.psi.NasmSymbolDecl

/**
 * Contributes C/C++ extern references when searching for usages of NASM labels.
 */
class CppToNasmReferencesSearchExecutor : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        // Find the label definition - might be NasmLabelDef, NasmGlobalLabel, or an identifier within
        val labelDef = when (val target = queryParameters.elementToSearch) {
            is NasmLabelDef -> target
            is NasmGlobalLabel -> PsiTreeUtil.getParentOfType(target, NasmLabelDef::class.java)
            else -> PsiTreeUtil.getParentOfType(target, NasmLabelDef::class.java)
        } ?: return

        val symbolName = labelDef.name ?: return
        val project = labelDef.project

        // Check if this label is declared in a 'global' directive
        if (!isLabelDeclaredGlobal(labelDef, symbolName)) {
            return
        }

        // Search scope for C/C++ files
        val searchScope = queryParameters.effectiveSearchScope
        val globalScope = searchScope as? GlobalSearchScope ?: GlobalSearchScope.allScope(project)

        // Find all C/C++ files in scope
        val ocFiles = FileTypeIndex.getFiles(OCFileType.INSTANCE, globalScope)
        val psiManager = PsiManager.getInstance(project)

        // Search each C/C++ file for extern declarations matching the label name
        for (virtualFile in ocFiles) {
            val ocFile = psiManager.findFile(virtualFile) as? OCFile ?: continue

            // Find all declarators in the file
            ocFile.accept(object : com.intellij.psi.PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is OCDeclarator && element.name == symbolName) {
                        // Check if it's an extern declaration
                        if (isExternDeclaration(element)) {
                            // Create a reference from the C/C++ extern to the NASM label
                            val reference = CppToNasmReference(element, labelDef)
                            consumer.process(reference)
                        }
                    }
                    super.visitElement(element)
                }
            })
        }
    }

    /**
     * Checks if a label is declared in a 'global' directive in the same file.
     */
    private fun isLabelDeclaredGlobal(labelDef: NasmLabelDef, symbolName: String): Boolean {
        val nasmFile = labelDef.containingFile as? NasmFile ?: return false

        // Find all global directives in the file
        val globalDirs = PsiTreeUtil.findChildrenOfType(nasmFile, NasmGlobalDir::class.java)

        for (globalDir in globalDirs) {
            val symbolList = globalDir.symbolList
            val symbolDecls = PsiTreeUtil.findChildrenOfType(symbolList, NasmSymbolDecl::class.java)

            for (symbolDecl in symbolDecls) {
                if (symbolDecl.name == symbolName) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Checks if a C/C++ declarator is an extern declaration.
     */
    private fun isExternDeclaration(declarator: OCDeclarator): Boolean {
        val declarationText = declarator.text
        if (declarationText.contains("extern")) {
            return true
        }

        // Additional heuristic: function declarations without bodies
        val parent = declarator.parent
        return parent?.text?.trimEnd()?.endsWith(";") == true
    }

    /**
     * Lightweight reference object representing a C/C++ extern that references a NASM label.
     * Implements PsiReference directly to avoid ElementManipulator issues with C++ elements.
     */
    private class CppToNasmReference(
        private val element: OCDeclarator,
        private val nasmTarget: NasmLabelDef
    ) : PsiReference {

        override fun getElement(): PsiElement = element

        override fun getRangeInElement(): TextRange {
            // Return the range of the identifier name within the declarator
            val nameIdentifier = element.nameIdentifier
            return if (nameIdentifier != null) {
                val startOffset = nameIdentifier.startOffsetInParent
                TextRange(startOffset, startOffset + nameIdentifier.textLength)
            } else {
                TextRange(0, element.textLength)
            }
        }

        override fun resolve(): PsiElement = nasmTarget

        override fun getCanonicalText(): String = element.name ?: ""

        override fun handleElementRename(newElementName: String): PsiElement {
            // We don't support renaming C++ elements from NASM
            // Just return the element unchanged
            return element
        }

        override fun bindToElement(element: PsiElement): PsiElement {
            // Not needed for our use case
            return this.element
        }

        override fun isReferenceTo(element: PsiElement): Boolean {
            return element == nasmTarget
        }

        override fun isSoft(): Boolean = true

        override fun getVariants(): Array<Any> = emptyArray()
    }
}
