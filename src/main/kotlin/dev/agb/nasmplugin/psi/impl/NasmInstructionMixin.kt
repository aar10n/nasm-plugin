package dev.agb.nasmplugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.database.InstructionDatabase
import dev.agb.nasmplugin.psi.*

/**
 * Mixin implementation for instructions that provides reference support
 * when the instruction is actually a macro invocation.
 *
 * Zero-parameter macros in NASM are invoked without arguments, making them look like
 * instructions. Since the grammar parses them as instructions (not macro calls),
 * we need to provide go-to-definition support for instruction elements that resolve to macros.
 */
abstract class NasmInstructionMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        // Get the identifier/mnemonic from the instruction
        val identifierNode = when (this) {
            is NasmInstruction -> (this as? NasmInstruction)?.mnemonic
            else -> node.findChildByType(NasmTypes.IDENTIFIER)?.psi
        } ?: return null

        val identifierName = identifierNode.text

        // Only provide a reference if this is NOT a valid instruction
        // (i.e., it's likely a macro invocation)
        val isValidInstruction = InstructionDatabase.getInstruction(identifierName) != null
        if (isValidInstruction) {
            // It's a real instruction, no reference needed
            return null
        }

        // Not a valid instruction - might be a macro invocation, provide reference
        val range = TextRange.from(identifierNode.startOffsetInParent, identifierNode.textLength)

        return MacroInstructionReference(this, range, identifierName)
    }

    /**
     * Reference implementation for instructions that are actually macro invocations.
     */
    private class MacroInstructionReference(
        element: PsiElement,
        range: TextRange,
        private val macroName: String
    ) : PsiReferenceBase<PsiElement>(element, range) {

        override fun resolve(): PsiElement? {
            val containingFile = element.containingFile ?: return null
            // Use utility method to find macro definition (including in included files)
            return containingFile.findMacroDefinitionWithIncludes(macroName)
        }

        override fun getVariants(): Array<Any> {
            // Return empty array - completion is handled by NasmCompletionContributor
            return emptyArray()
        }
    }
}
