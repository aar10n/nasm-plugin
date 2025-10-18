package dev.agb.nasmplugin.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.*

/**
 * Implementation for macro references in conditional directives (%ifdef, %ifndef)
 * Provides go-to-definition support for macros referenced in conditionals.
 */
abstract class NasmMacroReferenceImpl(node: ASTNode) : CachedReferenceProvider(node), NasmMacroReference {

    override fun createReference(): PsiReference = MacroReference(this)

    private class MacroReference(element: PsiElement) :
        PsiReferenceBase<PsiElement>(element, TextRange.from(0, element.textLength)) {

        override fun resolve(): PsiElement? {
            val macroName = element.text
            val containingFile = element.containingFile ?: return null

            // Resolve to any macro type (including command-line macros)
            return NasmSymbolResolver.resolveMacro(
                name = macroName,
                file = containingFile,
                includeIncludes = true
            )
        }

        override fun getVariants(): Array<Any> {
            // Completion is handled by NasmCompletionContributor
            return emptyArray()
        }
    }
}
