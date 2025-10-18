package dev.agb.nasmplugin.refactoring

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import dev.agb.nasmplugin.psi.NasmNamedElement

/**
 * Manipulator for NASM named elements to support rename refactoring.
 * This class handles the actual text replacement during rename operations.
 */
class NasmElementManipulator : AbstractElementManipulator<NasmNamedElement>() {

    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(element: NasmNamedElement, range: TextRange, newContent: String): NasmNamedElement {
        // Get the current element text
        val currentText = element.text

        // Build the new text by replacing only the specified range
        val newText = StringBuilder(currentText)
            .replace(range.startOffset, range.endOffset, newContent)
            .toString()

        // Use the setName method with the complete new text
        element.setName(newText)
        return element
    }

    override fun getRangeInElement(element: NasmNamedElement): TextRange {
        val nameIdentifier = element.nameIdentifier
        if (nameIdentifier != null) {
            val elementStart = element.textRange.startOffset
            val nameStart = nameIdentifier.textRange.startOffset
            val nameEnd = nameIdentifier.textRange.endOffset
            return TextRange(nameStart - elementStart, nameEnd - elementStart)
        }
        // If we can't find the name identifier, return the full element range
        return TextRange(0, element.textLength)
    }
}