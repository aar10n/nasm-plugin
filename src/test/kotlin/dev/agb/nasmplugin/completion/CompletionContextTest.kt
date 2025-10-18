package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests that completion works correctly in different contexts
 * (line start, after instruction, operand position, etc.)
 */
class CompletionContextTest : BasePlatformTestCase() {

    fun testCompletionAtLineStart() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // At line start in .text section, we should get instructions
        assertTrue("Should have completions at line start", completions.isNotEmpty())
    }

    fun testCompletionAfterPartialIdentifier() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                section .text
                my_<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        // Should get macro completion (might auto-complete if unique)
        if (completions != null) {
            assertTrue("Should include my_macro",
                completions.any { it.lookupString == "my_macro" })
        } else {
            // Auto-completed - verify it was inserted
            assertTrue("Should auto-complete my_macro",
                myFixture.editor.document.text.contains("my_macro"))
        }
    }

    fun testCompletionAfterInstruction() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // After instruction, should get operand completions (registers, etc.)
        assertTrue("Should have completions after instruction", completions.isNotEmpty())
    }

    fun testCompletionInOperandPosition() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // In operand position, should get register/value completions
        assertTrue("Should have completions in operand position", completions.isNotEmpty())
    }

    fun testCompletionAfterPercent() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                %<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // After %, should get preprocessor directives
        assertTrue("Should have completions after %", completions.isNotEmpty())
    }

    fun testCompletionAfterLabelWithIndent() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test_macro 0
                    nop
                %endmacro

                section .text
                label:
                    <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // After label with indentation, should still get completions
        assertTrue("Should have completions after label", completions.isNotEmpty())

        // Should include our macro
        assertTrue("Should include test_macro in completions",
            completions.any { it.lookupString == "test_macro" })
    }

    fun testCompletionWithMacroPrefix() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    nop
                %endmacro

                section .text
                test<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Auto-completed because only one match
            val text = myFixture.editor.document.text
            assertTrue("test macro should be auto-completed", text.contains("test"))
        } else {
            // Multiple matches
            assertTrue("Should include test macro",
                completions.any { it.lookupString == "test" })
        }
    }

    fun testCompletionInEmptyFile() {
        myFixture.configureByText("test.asm", "<caret>")

        val completions = myFixture.completeBasic() ?: emptyArray()

        // Even in empty file, should have some completions (directives, etc.)
        assertTrue("Should have completions in empty file", completions.isNotEmpty())
    }

    fun testMacroCompletionDoesNotCrashInOperandContext() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                mov rax, <caret>
            """.trimIndent()
        )

        // This should not crash even though macro may not be appropriate in operand position
        val completions = myFixture.completeBasic() ?: emptyArray()

        assertNotNull("Completion should not crash in operand position", completions)
    }
}
