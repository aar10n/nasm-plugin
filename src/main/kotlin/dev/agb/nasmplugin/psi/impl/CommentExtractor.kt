package dev.agb.nasmplugin.psi.impl

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Utility for extracting documentation comments from NASM elements.
 * Looks for comments on the line above or beside the definition.
 */
object CommentExtractor {

    /**
     * Extracts documentation comments for a named element (macro or EQU constant).
     * Returns a list of comment texts without the leading semicolon and whitespace.
     */
    fun extractComments(element: PsiElement): List<String> {
        val comments = mutableListOf<String>()

        // Extract comment on the same line (to the right)
        extractInlineComment(element)?.let { comments.add(it) }

        // Extract comments from lines above
        comments.addAll(0, extractPrecedingComments(element))

        return comments
    }

    /**
     * Extracts a comment that appears on the same line as the element (after it).
     * Example: "my_macro: %macro 1  ; This is my macro"
     */
    private fun extractInlineComment(element: PsiElement): String? {
        var current = element.nextSibling

        // Look for a comment on the same line (before the newline)
        while (current != null) {
            if (current is PsiComment || current.node?.elementType == NasmTypes.COMMENT) {
                return cleanComment(current.text)
            }
            if (current is PsiWhiteSpace && current.text.contains('\n')) {
                // Hit a newline, stop looking
                return null
            }
            current = current.nextSibling
        }

        return null
    }

    /**
     * Extracts comments from lines above the element.
     * Continues extracting as long as we find consecutive lines with comments.
     */
    private fun extractPrecedingComments(element: PsiElement): List<String> {
        val comments = mutableListOf<String>()
        var current = element.prevSibling

        // Skip backward to find comments before this line
        var foundNewline = false

        while (current != null) {
            if (current is PsiComment || current.node?.elementType == NasmTypes.COMMENT) {
                if (foundNewline) {
                    // This is a comment on a previous line
                    comments.add(0, cleanComment(current.text))
                    foundNewline = false
                    // Continue looking for more comments on even earlier lines
                    current = current.prevSibling
                    continue
                } else {
                    // This is a comment on the same line (before the element)
                    // We'll include it
                    comments.add(0, cleanComment(current.text))
                }
            } else if (current is PsiWhiteSpace) {
                if (current.text.contains('\n')) {
                    if (foundNewline) {
                        // Hit a blank line (two newlines), stop looking
                        break
                    }
                    foundNewline = true
                }
            } else {
                // Hit non-whitespace, non-comment - stop
                if (foundNewline) {
                    break
                }
            }
            current = current.prevSibling
        }

        return comments
    }

    /**
     * Removes the leading semicolon and whitespace from a comment.
     * Example: "; This is a comment" -> "This is a comment"
     */
    private fun cleanComment(commentText: String): String {
        return commentText
            .trimStart()
            .removePrefix(";")
            .trim()
    }

    /**
     * Formats extracted comments as HTML for documentation display.
     */
    fun formatCommentsAsHtml(comments: List<String>, elementName: String, elementType: String): String? {
        if (comments.isEmpty()) return null

        return buildString {
            append("<div>")

            // Title
            append("<h3 style='margin-top: 0;'>")
            append(elementName)
            append("</h3>")

            // Element type
            append("<p><b>$elementType</b></p>")

            // Comments
            comments.forEach { comment ->
                if (comment.isNotEmpty()) {
                    append("<p>")
                    // Escape HTML and convert to HTML-safe text
                    append(comment.replace("&", "&amp;")
                                  .replace("<", "&lt;")
                                  .replace(">", "&gt;"))
                    append("</p>")
                }
            }

            append("</div>")
        }
    }
}
