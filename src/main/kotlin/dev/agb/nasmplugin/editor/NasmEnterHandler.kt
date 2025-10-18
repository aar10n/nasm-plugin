package dev.agb.nasmplugin.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.psi.NasmFile

/**
 * Handles Enter key press in NASM files.
 * Automatically inserts %endmacro when pressing Enter after a %macro definition line.
 */
class NasmEnterHandler : EnterHandlerDelegate {

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (file !is NasmFile) {
            return EnterHandlerDelegate.Result.Continue
        }

        val document = editor.document
        val offset = caretOffset.get()

        // Get the current line
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val currentLine = document.getText(TextRange(lineStart, lineEnd))

        // Check if current line is a macro definition: %macro <name> <params>
        if (currentLine.trim().matches(Regex("^%macro\\s+\\w+\\s+.*"))) {
            // Let the original handler execute first
            return EnterHandlerDelegate.Result.Continue
        }

        return EnterHandlerDelegate.Result.Continue
    }

    override fun postProcessEnter(
        file: PsiFile,
        editor: Editor,
        dataContext: DataContext
    ): EnterHandlerDelegate.Result {
        if (file !is NasmFile) {
            return EnterHandlerDelegate.Result.Continue
        }

        val document = editor.document
        val offset = editor.caretModel.offset

        // Get the previous line (the one we just pressed Enter on)
        val currentLineNumber = document.getLineNumber(offset)
        if (currentLineNumber == 0) {
            return EnterHandlerDelegate.Result.Continue
        }

        val prevLineNumber = currentLineNumber - 1
        val prevLineStart = document.getLineStartOffset(prevLineNumber)
        val prevLineEnd = document.getLineEndOffset(prevLineNumber)
        val prevLine = document.getText(TextRange(prevLineStart, prevLineEnd))

        // Check if previous line is a macro definition: %macro <name> <params>
        if (prevLine.trim().matches(Regex("^%macro\\s+\\w+\\s+.*"))) {
            // Check if there's already a matching %endmacro
            if (!hasMatchingEndMacro(document, prevLineEnd)) {
                // Insert empty line + %endmacro
                val currentLineStart = document.getLineStartOffset(currentLineNumber)
                val indent = getIndent(prevLine)

                // Insert: empty line for macro body + %endmacro
                val textToInsert = "$indent\n$indent%endmacro"
                document.insertString(currentLineStart, textToInsert)

                // Move caret to the empty line (for macro body)
                editor.caretModel.moveToOffset(currentLineStart + indent.length)

                // Commit the document
                PsiDocumentManager.getInstance(file.project).commitDocument(document)

                return EnterHandlerDelegate.Result.Stop
            }
        }

        // Check if previous line is an %if directive
        if (prevLine.trim().matches(Regex("^%(if|ifdef|ifndef|ifmacro|ifnmacro|ifctx|ifnctx|ifidn|ifidni|ifnum|ifstr|iftoken)\\b.*"))) {
            // Check if there's already a matching %endif
            if (!hasMatchingEndIf(document, prevLineEnd)) {
                // Insert empty line + %endif
                val currentLineStart = document.getLineStartOffset(currentLineNumber)
                val indent = getIndent(prevLine)

                // Insert: empty line for conditional body + %endif
                val textToInsert = "$indent\n$indent%endif"
                document.insertString(currentLineStart, textToInsert)

                // Move caret to the empty line (for conditional body)
                editor.caretModel.moveToOffset(currentLineStart + indent.length)

                // Commit the document
                PsiDocumentManager.getInstance(file.project).commitDocument(document)

                return EnterHandlerDelegate.Result.Stop
            }
        }

        return EnterHandlerDelegate.Result.Continue
    }

    /**
     * Extract the leading whitespace from a line
     */
    private fun getIndent(line: String): String {
        var i = 0
        while (i < line.length && line[i].isWhitespace()) {
            i++
        }
        return line.take(i)
    }

    /**
     * Check if there's a matching %endmacro for the %macro at the given position
     */
    private fun hasMatchingEndMacro(document: Document, macroLineEnd: Int): Boolean {
        val text = document.text
        val searchStart = macroLineEnd

        // Count nested macros
        var nestLevel = 1
        var pos = searchStart

        while (pos < text.length && nestLevel > 0) {
            // Find next newline to process line by line
            val lineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
            val line = text.substring(pos, lineEnd).trim()

            // Check for nested %macro
            when {
                line.matches(Regex("^%macro\\s+\\w+\\s+.*")) -> nestLevel++
                line.matches(Regex("^%endmacro\\b.*")) -> nestLevel--
            }

            pos = lineEnd + 1
        }

        return nestLevel == 0
    }

    /**
     * Check if there's a matching %endif for the %if at the given position
     */
    private fun hasMatchingEndIf(document: Document, ifLineEnd: Int): Boolean {
        val text = document.text

        // Count nested conditionals
        var nestLevel = 1
        var pos = ifLineEnd

        while (pos < text.length && nestLevel > 0) {
            // Find next newline to process line by line
            val lineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
            val line = text.substring(pos, lineEnd).trim()

            // Check for nested %if variants
            when {
                line.matches(Regex("^%(if|ifdef|ifndef|ifmacro|ifnmacro|ifctx|ifnctx|ifidn|ifidni|ifnum|ifstr|iftoken)\\b.*")) -> nestLevel++
                line.matches(Regex("^%endif\\b.*")) -> nestLevel--
            }

            pos = lineEnd + 1
        }

        return nestLevel == 0
    }
}
