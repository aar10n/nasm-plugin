package dev.agb.nasmplugin.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for comment/uncomment functionality in NASM assembly files.
 * Verifies line comments with semicolons.
 */
class NasmCommenterTest : BasePlatformTestCase() {

    fun testCommentSingleLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>mov rax, rbx
            """.trimIndent()
        )

        // Trigger line comment action (Ctrl+/ or Cmd+/)
        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                ; mov rax, rbx
            """.trimIndent()
        )
    }

    fun testUncommentSingleLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>; mov rax, rbx
            """.trimIndent()
        )

        // Trigger line uncomment action
        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                mov rax, rbx
            """.trimIndent()
        )
    }

    fun testCommentMultipleLines() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <selection>mov rax, rbx
                mov rcx, rdx</selection>
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                ; mov rax, rbx
                ; mov rcx, rdx
            """.trimIndent()
        )
    }

    fun testUncommentMultipleLines() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <selection>; mov rax, rbx
                ; mov rcx, rdx</selection>
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                mov rax, rbx
                mov rcx, rdx
            """.trimIndent()
        )
    }

    fun testCommentEmptyLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
                mov rax, rbx
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        // Empty lines get commented with "; " (semicolon + space + newline)
        myFixture.checkResult(
            "section .text\n; \nmov rax, rbx"
        )
    }

    fun testCommentWithIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                    <caret>mov rax, rbx
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        // Comment is added at start of line, preserving existing indentation
        myFixture.checkResult(
            """
                section .text
                main:
                ;     mov rax, rbx
            """.trimIndent()
        )
    }

    fun testUncommentWithIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                    <caret>; mov rax, rbx
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                main:
                    mov rax, rbx
            """.trimIndent()
        )
    }

    fun testCommentMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>%macro test 0
                    nop
                %endmacro
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                ; %macro test 0
                    nop
                %endmacro
            """.trimIndent()
        )
    }

    fun testCommentLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>my_label:
                    nop
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                ; my_label:
                    nop
            """.trimIndent()
        )
    }

    fun testCommentDataDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                <caret>message db 'Hello', 0
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .data
                ; message db 'Hello', 0
            """.trimIndent()
        )
    }

    fun testToggleCommentTwice() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>mov rax, rbx
            """.trimIndent()
        )

        // Comment
        myFixture.performEditorAction("CommentByLineComment")

        // Uncomment
        myFixture.performEditorAction("CommentByLineComment")

        // Should be back to original
        myFixture.checkResult(
            """
                section .text
                mov rax, rbx
            """.trimIndent()
        )
    }

    fun testCommentMixedSelection() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <selection>mov rax, rbx
                ; mov rcx, rdx
                mov rsi, rdi</selection>
            """.trimIndent()
        )

        // When selection has both commented and uncommented lines,
        // the action typically comments all
        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                ; mov rax, rbx
                ; ; mov rcx, rdx
                ; mov rsi, rdi
            """.trimIndent()
        )
    }

    fun testCommenterConfiguration() {
        val commenter = NasmCommenter()

        // Verify line comment prefix is "; "
        assertEquals("; ", commenter.lineCommentPrefix)

        // Verify no block comments (NASM doesn't have block comments)
        assertNull("Should not have block comment prefix", commenter.blockCommentPrefix)
        assertNull("Should not have block comment suffix", commenter.blockCommentSuffix)
        assertNull("Should not have commented block comment prefix", commenter.commentedBlockCommentPrefix)
        assertNull("Should not have commented block comment suffix", commenter.commentedBlockCommentSuffix)
    }

    fun testCommentPreprocessorDirective() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>%define MY_VALUE 100
                section .text
                mov rax, MY_VALUE
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                ; %define MY_VALUE 100
                section .text
                mov rax, MY_VALUE
            """.trimIndent()
        )
    }

    fun testCommentInstructionWithComment() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>mov rax, rbx  ; copy value
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        // The whole line should get commented
        myFixture.checkResult(
            """
                section .text
                ; mov rax, rbx  ; copy value
            """.trimIndent()
        )
    }

    fun testUncommentPreservesIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                    <caret>; mov rax, rbx
                    ; mov rcx, rdx
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                section .text
                main:
                    mov rax, rbx
                    ; mov rcx, rdx
            """.trimIndent()
        )
    }

    fun testCommentEntireFile() {
        myFixture.configureByText(
            "test.asm",
            """
                <selection>section .text
                main:
                    mov rax, rbx
                    ret</selection>
            """.trimIndent()
        )

        myFixture.performEditorAction("CommentByLineComment")

        myFixture.checkResult(
            """
                ; section .text
                ; main:
                ;     mov rax, rbx
                ;     ret
            """.trimIndent()
        )
    }
}
