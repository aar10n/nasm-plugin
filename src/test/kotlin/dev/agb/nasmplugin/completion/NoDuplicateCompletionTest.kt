package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NoDuplicateCompletionTest : BasePlatformTestCase() {

    fun testNoDuplicateMacroCompletions() {
        // Create a file with a %define macro
        myFixture.configureByText(
            "test.asm",
            """
                %define MACRO_FUN 123

                mov qword [_start], MAC<caret>
            """.trimIndent()
        )

        // Get completions at the caret position
        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Auto-insertion happened - check that MACRO_FUN was inserted
            val text = myFixture.editor.document.text
            val caretOffset = myFixture.editor.caretModel.offset
            val textBeforeCaret = if (caretOffset > 0) {
                text.substring(0.coerceAtLeast(caretOffset - 10), caretOffset)
            } else ""

            val hasMacroFun = textBeforeCaret.contains("MACRO_FUN")
            assertTrue("MACRO_FUN should be auto-inserted as single match", hasMacroFun)
        } else {
            // Print all completions for debugging
            println("All completions (${completions.size} total):")
            val grouped = completions.groupBy { it.lookupString }
            grouped.forEach { (name, items) ->
                if (items.size > 1) {
                    println("  $name: ${items.size} occurrences")
                    items.forEach { item ->
                        // LookupElement doesn't directly expose presentation
                        println("    - lookup: ${item.lookupString}")
                    }
                }
            }

            // Check that MACRO_FUN appears in completions
            val macroFunCompletions = completions.filter {
                it.lookupString == "MACRO_FUN"
            }

            // Should have exactly one completion for MACRO_FUN, not duplicates
            assertEquals(
                "MACRO_FUN should appear exactly once in completions",
                1,
                macroFunCompletions.size
            )

            // Verify it's marked as the correct type
            if (macroFunCompletions.isNotEmpty()) {
                val completion = macroFunCompletions.first()
                // Get the actual lookup string to verify it was found
                assertEquals("MACRO_FUN", completion.lookupString)
            }
        }
    }

    fun testNoDuplicateMultipleMacros() {
        // Create a file with multiple macros
        myFixture.configureByText(
            "test.asm",
            """
                %define MACRO_ONE 100
                %define MACRO_TWO 200
                %define MACRO_THREE 300

                %macro my_macro 2
                    mov %1, %2
                %endmacro

                mov rax, MACRO<caret>
            """.trimIndent()
        )

        // Get completions at the caret position
        val completions = myFixture.completeBasic() ?: emptyArray()

        // Count occurrences of each macro
        val macroOneCounts = completions.count { it.lookupString == "MACRO_ONE" }
        val macroTwoCounts = completions.count { it.lookupString == "MACRO_TWO" }
        val macroThreeCounts = completions.count { it.lookupString == "MACRO_THREE" }

        // Each should appear exactly once
        assertEquals("MACRO_ONE should appear exactly once", 1, macroOneCounts)
        assertEquals("MACRO_TWO should appear exactly once", 1, macroTwoCounts)
        assertEquals("MACRO_THREE should appear exactly once", 1, macroThreeCounts)
    }

    fun testLabelsNotDuplicated() {
        // Create a file with labels
        myFixture.configureByText(
            "test.asm",
            """
                global my_label

                my_label:
                    mov rax, 1

                another_label:
                    jmp my<caret>
            """.trimIndent()
        )

        // Get completions at the caret position
        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Auto-insertion happened - check text
            val text = myFixture.editor.document.text
            val caretOffset = myFixture.editor.caretModel.offset
            val textBeforeCaret = if (caretOffset > 0) {
                text.substring(0.coerceAtLeast(caretOffset - 10), caretOffset)
            } else ""

            val hasMyLabel = textBeforeCaret.contains("my_label")
            assertTrue("my_label should be auto-inserted as single match", hasMyLabel)
        } else {
            // Count occurrences of my_label
            val myLabelCount = completions.count { it.lookupString == "my_label" }

            // Should appear exactly once
            assertEquals("my_label should appear exactly once", 1, myLabelCount)
        }
    }
}