package dev.agb.nasmplugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.*

/**
 * Mixin for NasmMacroArg that provides reference resolution for identifier tokens within macro arguments.
 * This enables Cmd+Click navigation for arguments like: print_msg msg, msg_len
 */
abstract class NasmMacroArgMixin(node: ASTNode) : ASTWrapperPsiElement(node), NasmMacroArg {

    override fun getReferences(): Array<PsiReference> {
        val references = mutableListOf<PsiReference>()

        // Find all IDENTIFIER tokens in this macro argument
        var child: PsiElement? = firstChild
        while (child != null) {
            if (child.node.elementType == NasmTypes.IDENTIFIER) {
                val startOffset = child.startOffsetInParent
                val length = child.textLength
                references.add(MacroArgIdentifierReference(this, TextRange.from(startOffset, length), child.text))
            }
            child = child.nextSibling
        }

        return references.toTypedArray()
    }

    private class MacroArgIdentifierReference(
        element: PsiElement,
        range: TextRange,
        private val identifierName: String
    ) : PsiReferenceBase<PsiElement>(element, range) {

        override fun resolve(): PsiElement? {
            val containingFile = element.containingFile ?: return null

            // Use centralized resolver for all symbol types
            return NasmSymbolResolver.resolve(
                name = identifierName,
                file = containingFile,
                context = element,  // Pass element for local label context
                searchTypes = NasmSymbolResolver.SearchType.ALL,
                includeIncludes = true
            )
        }

        override fun getVariants(): Array<Any> {
            // Completion is handled by NasmCompletionContributor
            // Returning empty array here prevents duplicate completions
            return emptyArray()
        }
    }
}
