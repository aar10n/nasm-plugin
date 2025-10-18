package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MultiLineMacroCompletionTest : BasePlatformTestCase() {

    fun testMultiLineMacroInInstructionContext() {
        // Create a file with multi-line macros
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

        // Get completions at the caret position
        val completions = myFixture.completeBasic()

        if (completions == null) {
            // If null, it means there was a single match that was auto-inserted
            // Check if the text at the cursor now contains my_macro
            val text = myFixture.editor.document.text
            val caretOffset = myFixture.editor.caretModel.offset

            // Check if my_macro was auto-inserted
            val textBeforeCaret = if (caretOffset > 0) {
                text.substring(0.coerceAtLeast(caretOffset - 10), caretOffset)
            } else ""

            println("completeBasic() returned null (single completion auto-inserted?)")
            println("Text before caret: '$textBeforeCaret'")

            // If my_macro was auto-inserted, the test should pass
            val autoInserted = textBeforeCaret.contains("my_macro")
            assertTrue("my_macro should be auto-inserted as single completion", autoInserted)
        } else {
            println("Total completions found: ${completions.size}")

            // Print all macro-related completions
            completions.forEach { completion ->
                val lookupString = completion.lookupString
                if (lookupString.contains("my_", ignoreCase = true) ||
                    lookupString.contains("print", ignoreCase = true)) {
                    println("  Found: $lookupString")
                }
            }

            // Check if my_macro is in completions
            val hasMacro = completions.any { it.lookupString == "my_macro" }

            println("Has my_macro: $hasMacro")

            // These should be true
            assertTrue("my_macro should appear in completions", hasMacro)
        }
    }

    fun testMultiLineMacroAsStatement() {
        // Test multi-line macro as a complete statement
        myFixture.configureByText(
            "test.asm",
            """
                %macro clear_regs 0
                    xor rax, rax
                    xor rbx, rbx
                    xor rcx, rcx
                %endmacro

                section .text
                _start:
                    <caret>
            """.trimIndent()
        )

        // Get completions at the caret position
        val completions = myFixture.completeBasic() ?: emptyArray()

        // Look for clear_regs
        val hasClearRegs = completions.any { it.lookupString == "clear_regs" }

        println("Has clear_regs in statement position: $hasClearRegs")

        assertTrue("clear_regs should appear in completions at statement position", hasClearRegs)
    }

    fun testMixedMacroTypes() {
        // Test both single-line and multi-line macros
        myFixture.configureByText(
            "test.asm",
            """
                %define CONSTANT 42

                %macro add_const 1
                    add %1, CONSTANT
                %endmacro

                section .text
                _start:
                    add<caret>
            """.trimIndent()
        )

        // Get completions at the caret position
        val completions = myFixture.completeBasic() ?: emptyArray()

        // Look for add_const
        val hasAddConst = completions.any { it.lookupString == "add_const" }

        println("Has add_const when typing 'add': $hasAddConst")

        assertTrue("add_const should appear in completions when typing 'add'", hasAddConst)
    }
}