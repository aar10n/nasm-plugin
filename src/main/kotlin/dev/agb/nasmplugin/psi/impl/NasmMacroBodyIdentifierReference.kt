package dev.agb.nasmplugin.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.impl.NasmPsiUtils.isInMacroDefinitionBody

/**
 * Reference for identifiers within macro bodies (like %define) that can reference other macros
 */
class NasmMacroBodyIdentifierReference(element: PsiElement) :
    PsiReferenceBase<PsiElement>(element, TextRange.from(0, element.textLength)) {

    override fun resolve(): PsiElement? {
        val name = element.text
        val containingFile = element.containingFile ?: return null

        // Use centralized resolver for all symbol types
        return NasmSymbolResolver.resolve(
            name = name,
            file = containingFile,
            context = null,  // No context needed for macro body references
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
    }

    override fun getVariants(): Array<Any> = emptyArray()
}