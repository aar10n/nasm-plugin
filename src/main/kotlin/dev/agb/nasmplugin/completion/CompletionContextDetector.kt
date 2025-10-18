package dev.agb.nasmplugin.completion

import com.intellij.psi.PsiElement

/**
 * Detects the completion context based on the cursor position and surrounding code.
 * Uses a rule-based system to make context detection extensible and maintainable.
 */
object CompletionContextDetector {

    /**
     * A rule for detecting a specific completion context.
     */
    private data class ContextRule(
        val name: String,
        val context: CompletionContext,
        val precedence: Int,  // Higher precedence rules are checked first
        val matcher: (PsiElement, String, Int) -> Boolean  // position, fileText, offset
    )

    /**
     * List of context detection rules, checked in order of precedence.
     * Add new rules here to extend context detection.
     */
    private val rules = listOf(
        // After percent sign has highest precedence
        ContextRule(
            name = "After Percent Sign",
            context = CompletionContext.AfterPercent,
            precedence = 1000,
            matcher = { _, fileText, offset ->
                offset > 0 && fileText.getOrNull(offset - 1) == '%'
            }
        ),

        // Memory operand (inside square brackets)
        ContextRule(
            name = "Memory Operand",
            context = CompletionContext.MemoryOperand,
            precedence = 900,
            matcher = { _, fileText, offset ->
                isInsideBrackets(fileText, offset)
            }
        ),

        // Jump/Call instructions
        ContextRule(
            name = "Jump Target",
            context = CompletionContext.JumpTarget,
            precedence = 800,
            matcher = { _, fileText, offset ->
                val lineText = getLineTextBeforeCursor(fileText, offset)
                val cleanLine = lineText.substringBefore(';').trim()

                // Match jump instructions: jmp, ja, jne, call, etc.
                cleanLine.matches(Regex("^(?:[.a-zA-Z_][a-zA-Z0-9_]*:)?\\s*(?:jmp|call|j[a-z]+|loop[a-z]*|jecxz|jrcxz)\\s+\\S*$", RegexOption.IGNORE_CASE))
            }
        ),

        // Global directive
        ContextRule(
            name = "Global Directive",
            context = CompletionContext.GlobalDirective,
            precedence = 750,
            matcher = { _, fileText, offset ->
                val lineText = getLineTextBeforeCursor(fileText, offset)
                val cleanLine = lineText.substringBefore(';').trim()
                // Check if line starts with 'global' followed by whitespace
                cleanLine.matches(Regex("^(?:global|GLOBAL)\\s+.*$")) ||
                cleanLine.matches(Regex("^(?:global|GLOBAL)\\s*$"))
            }
        ),

        // Extern directive
        ContextRule(
            name = "Extern Directive",
            context = CompletionContext.ExternDirective,
            precedence = 740,
            matcher = { _, fileText, offset ->
                val lineText = getLineTextBeforeCursor(fileText, offset)
                val cleanLine = lineText.substringBefore(';').trim()
                // Check if line starts with 'extern' followed by whitespace
                cleanLine.matches(Regex("^(?:extern|EXTERN)\\s+.*$")) ||
                cleanLine.matches(Regex("^(?:extern|EXTERN)\\s*$"))
            }
        ),

        // Data instruction operand (after comma or instruction)
        ContextRule(
            name = "Data Operand",
            context = CompletionContext.DataOperand,
            precedence = 700,
            matcher = { _, fileText, offset ->
                val lineText = getLineTextBeforeCursor(fileText, offset)
                val cleanLine = lineText.substringBefore(';').trim()

                // After comma (subsequent operand)
                val hasComma = cleanLine.contains(',')

                // After data instruction (mov, add, sub, etc.)
                val afterDataInstruction = cleanLine.matches(
                    Regex("^(?:[.a-zA-Z_][a-zA-Z0-9_]*:)?\\s*[a-z]+\\s+\\S*$", RegexOption.IGNORE_CASE)
                )

                hasComma || afterDataInstruction
            }
        ),

        // Line start (no text before cursor on line)
        ContextRule(
            name = "Line Start",
            context = CompletionContext.LineStart,
            precedence = 100,
            matcher = { _, fileText, offset ->
                val lineText = getLineTextBeforeCursor(fileText, offset).trim()
                lineText.isEmpty()
            }
        )

        // General context is the default fallback (no rule needed)
    )

    /**
     * Detects the completion context for the given position.
     */
    fun detectContext(position: PsiElement): CompletionContext {
        val file = position.containingFile ?: return CompletionContext.General
        val fileText = file.text
        val offset = position.textOffset

        // Check rules in order of precedence
        return rules
            .sortedByDescending { it.precedence }
            .firstOrNull { it.matcher(position, fileText, offset) }
            ?.context
            ?: CompletionContext.General
    }

    /**
     * Gets the text from the start of the current line to the cursor position.
     */
    private fun getLineTextBeforeCursor(fileText: String, offset: Int): String {
        if (offset <= 0) return ""

        var lineStart = offset
        while (lineStart > 0 && fileText[lineStart - 1] != '\n') {
            lineStart--
        }

        return fileText.substring(lineStart, offset.coerceAtMost(fileText.length))
    }

    /**
     * Checks if the cursor is inside square brackets (memory operand).
     */
    private fun isInsideBrackets(fileText: String, offset: Int): Boolean {
        if (offset <= 0) return false

        // Find the start of the current line
        var lineStart = offset
        while (lineStart > 0 && fileText[lineStart - 1] != '\n') {
            lineStart--
        }

        // Count brackets from line start to cursor
        var bracketDepth = 0
        for (i in lineStart until offset) {
            when (fileText.getOrNull(i)) {
                '[' -> bracketDepth++
                ']' -> bracketDepth--
            }
        }

        return bracketDepth > 0
    }
}
