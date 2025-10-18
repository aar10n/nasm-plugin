package dev.agb.nasmplugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmPsiUtils.isInMacroDefinitionBody

/**
 * Mixin for preprocessor tokens that provides references for IDENTIFIER tokens within macro definitions
 */
abstract class NasmPreprocessorTokenMixin(node: ASTNode) : ASTWrapperPsiElement(node), NasmPreprocessorToken {

    override fun getReferences(): Array<PsiReference> {
        // Check if this token is an IDENTIFIER
        val firstChild = firstChild
        if (firstChild != null && firstChild.node?.elementType == NasmTypes.IDENTIFIER) {
            // Check if we're in a macro definition body
            if (isInMacroDefinitionBody()) {
                // Pass 'this' as the element, not firstChild
                return arrayOf(NasmPreprocessorTokenIdentifierReference(this))
            }
        }
        return PsiReference.EMPTY_ARRAY
    }

    override fun getReference(): PsiReference? {
        val refs = references
        return if (refs.isNotEmpty()) refs[0] else null
    }
}

/**
 * Reference for NasmPreprocessorToken elements that contain identifiers referencing macros
 */
class NasmPreprocessorTokenIdentifierReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength)) {

    override fun resolve(): PsiElement? {
        val name = element.text
        val containingFile = element.containingFile ?: return null

        // Use centralized resolver for all symbol types
        return NasmSymbolResolver.resolve(
            name = name,
            file = containingFile,
            context = null,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
    }

    override fun getVariants(): Array<Any> = emptyArray()
}