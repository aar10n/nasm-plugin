package dev.agb.nasmplugin.refactoring

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.IncorrectOperationException
import dev.agb.nasmplugin.psi.NasmPreprocessorToken

/**
 * Element manipulator for NasmPreprocessorToken - enables rename refactoring for
 * identifier tokens in preprocessor contexts (e.g., macro definitions).
 */
class NasmPreprocessorTokenManipulator : AbstractElementManipulator<NasmPreprocessorToken>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        element: NasmPreprocessorToken,
        range: TextRange,
        newContent: String
    ): NasmPreprocessorToken {
        // NasmPreprocessorToken contains an IDENTIFIER token
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
            throw IncorrectOperationException("Cannot rename preprocessor token: unexpected PSI structure")
        }

        return element
    }

    override fun getRangeInElement(element: NasmPreprocessorToken): TextRange {
        // The entire element is the preprocessor token
        return TextRange(0, element.textLength)
    }
}
