package dev.agb.nasmplugin.refactoring

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.IncorrectOperationException
import dev.agb.nasmplugin.psi.NasmInstruction

/**
 * Element manipulator for NasmInstruction - enables rename refactoring for macro invocations
 * that look like instructions.
 */
class NasmInstructionManipulator : AbstractElementManipulator<NasmInstruction>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(element: NasmInstruction, range: TextRange, newContent: String): NasmInstruction {
        // NasmInstruction has a mnemonic child which contains the instruction/macro name
        val mnemonic = element.mnemonic
        val identifier = mnemonic.firstChild

        if (identifier is LeafPsiElement) {
            // Build the new text by replacing only the specified range
            val currentText = identifier.text
            val newText = StringBuilder(currentText)
                .replace(range.startOffset, range.endOffset, newContent)
                .toString()

            // Directly update the leaf element's text
            identifier.replaceWithText(newText)
        } else {
            throw IncorrectOperationException("Cannot rename instruction/macro: unexpected PSI structure")
        }

        return element
    }

    override fun getRangeInElement(element: NasmInstruction): TextRange {
        // The mnemonic is the part we want to rename
        val mnemonic = element.mnemonic
        val elementStart = element.textRange.startOffset
        val mnemonicStart = mnemonic.textRange.startOffset
        val mnemonicEnd = mnemonic.textRange.endOffset
        return TextRange(mnemonicStart - elementStart, mnemonicEnd - elementStart)
    }
}
