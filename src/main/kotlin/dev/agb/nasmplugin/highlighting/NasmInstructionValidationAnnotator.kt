package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.agb.nasmplugin.database.InstructionDatabase
import dev.agb.nasmplugin.psi.*

/**
 * Validates NASM instructions and provides semantic highlighting.
 *
 * This annotator:
 * - Highlights valid instruction names
 * - Validates instruction names against the instruction database
 * - Distinguishes between instructions and macros
 * - Reports errors for unknown instructions
 */
class NasmInstructionValidationAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is NasmInstruction -> validateAndHighlightInstruction(element, holder)
        }
    }

    private fun validateAndHighlightInstruction(instruction: NasmInstruction, holder: AnnotationHolder) {
        // Get the mnemonic element from the instruction
        val mnemonic = instruction.mnemonic
        val mnemonicText = mnemonic.text.trim()

        // Check if it's a valid instruction
        val isValidInstruction = InstructionDatabase.getInstruction(mnemonicText) != null

        if (isValidInstruction) {
            // Valid instruction - apply semantic highlighting to the mnemonic element
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(mnemonic)
                .textAttributes(NasmSyntaxHighlighter.INSTRUCTION)
                .create()
        } else {
            // Check if it might be a macro invocation
            val file = instruction.containingFile
            val isMacro = file?.let { findMacroDefinitionWithIncludes(mnemonicText, it) != null } ?: false

            if (!isMacro) {
                // Not a valid instruction and not a macro - report error
                holder.newAnnotation(
                    HighlightSeverity.ERROR,
                    "Unknown instruction or macro: '$mnemonicText'"
                )
                    .range(mnemonic)
                    .create()
            }
            // If it's a macro invocation - let the macro annotator handle highlighting
        }
    }

    private fun findMacroDefinitionWithIncludes(macroName: String, file: com.intellij.psi.PsiFile): PsiElement? {
        // Check in current file
        file.findMacroDefinition(macroName)?.let { return it }

        // Check in included files
        file.getIncludedFiles().forEach { includedFile ->
            includedFile.findMacroDefinition(macroName)?.let { return it }
        }

        return null
    }

}
