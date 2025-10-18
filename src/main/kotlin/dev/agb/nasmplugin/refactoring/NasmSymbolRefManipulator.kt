package dev.agb.nasmplugin.refactoring

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.IncorrectOperationException
import dev.agb.nasmplugin.psi.NasmSymbolRef

/**
 * Element manipulator for NasmSymbolRef - enables rename refactoring for symbol references.
 */
class NasmSymbolRefManipulator : AbstractElementManipulator<NasmSymbolRef>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(element: NasmSymbolRef, range: TextRange, newContent: String): NasmSymbolRef {
        // NasmSymbolRef usually contains a single IDENTIFIER token
        // Find that token and update its text
        val identifier = element.firstChild

        if (identifier is LeafPsiElement) {
            // Build the new text by replacing only the specified range
            val currentText = identifier.text
            val newText = StringBuilder(currentText)
                .replace(range.startOffset, range.endOffset, newContent)
                .toString()

            // Directly update the leaf element's text
            identifier.replaceWithText(newText)
        } else {
            throw IncorrectOperationException("Cannot rename symbol: unexpected PSI structure")
        }

        return element
    }

    override fun getRangeInElement(element: NasmSymbolRef): TextRange {
        // The entire element is the symbol reference
        return TextRange(0, element.textLength)
    }
}
