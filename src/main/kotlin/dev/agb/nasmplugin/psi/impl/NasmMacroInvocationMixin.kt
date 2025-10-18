package dev.agb.nasmplugin.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.*

/**
 * Mixin implementation for macro invocations that provides go-to-definition support
 */
abstract class NasmMacroInvocationMixin(node: ASTNode) : CachedReferenceProvider(node), NasmMacroReference {

    override fun createReference(): PsiReference? {
        // Get the IDENTIFIER token (macro name)
        val identifierNode = node.findChildByType(NasmTypes.IDENTIFIER)
            ?: return null
        val identifier = identifierNode.psi
        val range = TextRange.from(identifier.startOffsetInParent, identifier.textLength)

        return MacroInvocationReference(this, range, identifier.text)
    }

    private class MacroInvocationReference(
        element: PsiElement,
        range: TextRange,
        private val macroName: String
    ) : PsiReferenceBase<PsiElement>(element, range) {

        override fun resolve(): PsiElement? {
            val containingFile = element.containingFile ?: return null

            // Use centralized resolver for macro resolution
            return NasmSymbolResolver.resolveMacro(
                name = macroName,
                file = containingFile,
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
