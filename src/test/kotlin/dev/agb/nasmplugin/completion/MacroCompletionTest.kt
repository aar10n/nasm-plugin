package dev.agb.nasmplugin.completion

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmMultiLineMacro
import dev.agb.nasmplugin.psi.findMultiLineMacros

/**
 * Comprehensive tests for macro completion in various contexts.
 * Consolidated from MacroDebugTest, MacroCompletionDebugTest, and MacroCompletionDebugTest2.
 */
class MacroCompletionTest : BasePlatformTestCase() {

    fun testMacroCompletionAtLineStart() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro

                %macro print_msg 1
                    push %1
                    call printf
                    add rsp, 8
                %endmacro

                section .text
                _start:
                    <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        val hasMy = completions.any { it.lookupString == "my_macro" }
        val hasPrint = completions.any { it.lookupString == "print_msg" }

        assertTrue("my_macro should be in completions", hasMy)
        assertTrue("print_msg should be in completions", hasPrint)
    }

    fun testMacroCompletionWithPrefix() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test_macro 0
                    nop
                %endmacro

                section .text
                test_<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Single completion - auto-completed
            val text = myFixture.editor.document.text
            assertTrue("test_macro should be auto-completed", text.contains("test_macro"))
        } else {
            val hasTestMacro = completions.any { it.lookupString == "test_macro" }
            assertTrue("test_macro should be in completions", hasTestMacro)
        }
    }

    fun testMacroCompletionAfterPartialTyping() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro

                %macro print_msg 1
                    push %1
                    call printf
                    add rsp, 8
                %endmacro

                section .text
                _start:
                    my_<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Auto-completed (single match)
            val text = myFixture.editor.document.text
            assertTrue("my_macro should be auto-inserted", text.contains("my_macro"))
        } else {
            val hasMy = completions.any { it.lookupString == "my_macro" }
            assertTrue("my_macro should be in completions", hasMy)
        }
    }

    fun testMacroDiscoveryInPsiTree() {
        val file = myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro

                %macro print_msg 1
                    push %1
                    call printf
                    add rsp, 8
                %endmacro

                section .text
                _start:
                    my_macro rax, rbx
            """.trimIndent()
        )

        // Verify macros are properly discovered in PSI tree
        val multiLineMacros = PsiTreeUtil.findChildrenOfType(file, NasmMultiLineMacro::class.java)
        assertEquals("Should find 2 multi-line macros", 2, multiLineMacros.size)

        val macros = file.findMultiLineMacros()
        assertEquals("findMultiLineMacros() should return 2 macros", 2, macros.size)

        val macroNames = macros.mapNotNull { (it as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name }
        assertTrue("Should find my_macro", macroNames.contains("my_macro"))
        assertTrue("Should find print_msg", macroNames.contains("print_msg"))
    }

    fun testMacroCompletionInOperandPosition() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_test_macro 0
                    nop
                %endmacro

                mov rax, <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // Macros may or may not appear in operand position depending on context
        // This test verifies completion doesn't crash in this context
        assertNotNull("Completion should work in operand position", completions)
    }

    fun testMultipleMacrosWithSimilarNames() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    nop
                %endmacro

                %macro test_macro 0
                    nop
                %endmacro

                %macro test_macro_long 0
                    nop
                %endmacro

                test<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        // All three macros should appear
        val testMacros = completions.filter { it.lookupString.startsWith("test") }
        assertTrue("Should have at least 3 test-related macros", testMacros.size >= 3)
        assertTrue("Should include 'test'", completions.any { it.lookupString == "test" })
        assertTrue("Should include 'test_macro'", completions.any { it.lookupString == "test_macro" })
        assertTrue("Should include 'test_macro_long'", completions.any { it.lookupString == "test_macro_long" })
    }
}
