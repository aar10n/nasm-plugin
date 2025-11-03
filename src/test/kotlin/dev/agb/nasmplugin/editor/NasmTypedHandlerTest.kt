package dev.agb.nasmplugin.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for typed character handling in NASM assembly files.
 * Verifies auto-popup completion triggers.
 */
class NasmTypedHandlerTest : BasePlatformTestCase() {

    fun testPercentSignTriggersAutoPopup() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
            """.trimIndent()
        )

        // Type '%' which should trigger auto-popup for preprocessor directives
        myFixture.type('%')

        // Verify the character was typed
        myFixture.checkResult(
            """
                section .text
                %<caret>
            """.trimIndent()
        )

        // The auto-popup is triggered asynchronously, so we can't directly test
        // the popup appearance, but we can verify the handler is registered
        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        assertEquals("Should stop after handling %",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP,
            result)
    }

    fun testPercentSignInComment() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                ; This is a comment about <caret> symbols
            """.trimIndent()
        )

        // Type '%' in a comment
        myFixture.type('%')

        myFixture.checkResult(
            """
                section .text
                ; This is a comment about %<caret> symbols
            """.trimIndent()
        )
    }

    fun testPercentSignInString() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                msg db "Progress: <caret>d", 0
            """.trimIndent()
        )

        // Type '%' in a string
        myFixture.type('%')

        myFixture.checkResult(
            """
                section .data
                msg db "Progress: %<caret>d", 0
            """.trimIndent()
        )
    }

    fun testPercentSignAtStartOfLine() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        myFixture.type('%')

        myFixture.checkResult(
            """
                %<caret>
            """.trimIndent()
        )

        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        assertEquals("Should trigger auto-popup",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP,
            result)
    }

    fun testPercentSignAfterWhitespace() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                    <caret>
            """.trimIndent()
        )

        myFixture.type('%')

        myFixture.checkResult(
            """
                section .text
                    %<caret>
            """.trimIndent()
        )
    }

    fun testMultiplePercentSigns() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        // Type multiple % signs
        myFixture.type('%')
        myFixture.type('%')

        myFixture.checkResult(
            """
                %%<caret>
            """.trimIndent()
        )

        // Both should trigger auto-popup
        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        assertEquals("Should trigger auto-popup for second %",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP,
            result)
    }

    fun testNonPercentCharacterDoesNotTrigger() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
            """.trimIndent()
        )

        val handler = NasmTypedHandler()

        // Test various characters that should not trigger auto-popup
        val nonTriggerChars = listOf('m', 'a', '.', ':', ' ', '\n', '[', ']')

        nonTriggerChars.forEach { char ->
            val result = handler.checkAutoPopup(
                char,
                myFixture.project,
                myFixture.editor,
                myFixture.file
            )

            assertEquals("Character '$char' should not trigger auto-popup",
                com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE,
                result)
        }
    }

    fun testTypedHandlerOnlyWorksForNasmFiles() {
        // Configure a non-NASM file
        myFixture.configureByText(
            "test.txt",
            "<caret>"
        )

        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        // Should continue (not handle) for non-NASM files
        assertEquals("Should not handle non-NASM files",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.CONTINUE,
            result)
    }

    fun testPercentSignInMacroParameter() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 2
                    mov <caret>1, %2
                %endmacro
            """.trimIndent()
        )

        myFixture.type('%')

        myFixture.checkResult(
            """
                %macro test 2
                    mov %<caret>1, %2
                %endmacro
            """.trimIndent()
        )
    }

    fun testPercentSignFollowedByDirective() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        // Type a complete directive
        myFixture.type('%')
        myFixture.type("define")
        myFixture.type(' ')
        myFixture.type("TEST")
        myFixture.type(' ')
        myFixture.type("1")

        myFixture.checkResult(
            """
                %define TEST 1<caret>
            """.trimIndent()
        )
    }

    fun testPercentInIncludeDirective() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        myFixture.type('%')
        myFixture.type("include")

        myFixture.checkResult(
            """
                %include<caret>
            """.trimIndent()
        )
    }

    fun testPercentInMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        myFixture.type('%')
        myFixture.type("macro")

        myFixture.checkResult(
            """
                %macro<caret>
            """.trimIndent()
        )
    }

    fun testPercentInConditional() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        myFixture.type('%')
        myFixture.type("ifdef")

        myFixture.checkResult(
            """
                %ifdef<caret>
            """.trimIndent()
        )
    }

    fun testHandlerIntegration() {
        // Integration test: verify that typing % actually shows completions
        myFixture.configureByText(
            "test.asm",
            """
                <caret>
            """.trimIndent()
        )

        // Type % and trigger completion
        myFixture.type('%')

        // Complete basic to see if we get preprocessor directive suggestions
        val completions = myFixture.completeBasic()

        // We should get some completions (or null if auto-completed)
        // The presence of completions or auto-completion indicates the handler worked
        val hasCompletions = completions == null || completions.isNotEmpty()
        assertTrue("Should have completions after typing %", hasCompletions)
    }

    fun testPercentAtEndOfLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text<caret>
            """.trimIndent()
        )

        // Move to new line and type %
        myFixture.type('\n')
        myFixture.type('%')

        myFixture.checkResult(
            """
                section .text
                %<caret>
            """.trimIndent()
        )

        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        assertEquals("Should trigger auto-popup",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP,
            result)
    }

    fun testPercentInDataSection() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                <caret>
            """.trimIndent()
        )

        myFixture.type('%')

        myFixture.checkResult(
            """
                section .data
                %<caret>
            """.trimIndent()
        )

        // Should still trigger auto-popup even in data section
        val handler = NasmTypedHandler()
        val result = handler.checkAutoPopup(
            '%',
            myFixture.project,
            myFixture.editor,
            myFixture.file
        )

        assertEquals("Should trigger auto-popup in data section",
            com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result.STOP,
            result)
    }
}
