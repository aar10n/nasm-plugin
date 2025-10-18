package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SimpleMacroCompletionTest : BasePlatformTestCase() {

    fun testSimplestCase() {
        // Absolutely simplest case - single character prefix
        myFixture.configureByText(
            "test.asm",
            """
                %macro foo 0
                    nop
                %endmacro

                f<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()
        println("Completions when typing 'f':")
        println("  Total: ${completions.size}")

        val hasFoo = completions.any { it.lookupString == "foo" }
        println("  Has foo: $hasFoo")

        assertTrue("foo macro should appear when typing 'f'", hasFoo)
    }

    fun testTwoCharPrefix() {
        // Two character prefix
        myFixture.configureByText(
            "test.asm",
            """
                %macro bar 0
                    nop
                %endmacro

                ba<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        if (completions == null) {
            // Check if auto-completed
            val text = myFixture.editor.document.text
            val caretOffset = myFixture.editor.caretModel.offset
            val textBeforeCaret = if (caretOffset > 0) {
                text.substring(0.coerceAtLeast(caretOffset - 10), caretOffset)
            } else ""

            println("Completions when typing 'ba': auto-inserted")
            println("  Text before caret: '$textBeforeCaret'")

            val autoInserted = textBeforeCaret.contains("bar")
            assertTrue("bar macro should be auto-inserted when typing 'ba'", autoInserted)
        } else {
            println("Completions when typing 'ba':")
            println("  Total: ${completions.size}")

            val hasBar = completions.any { it.lookupString == "bar" }
            println("  Has bar: $hasBar")

            assertTrue("bar macro should appear when typing 'ba'", hasBar)
        }
    }

    fun testEmptyPrefix() {
        // Empty prefix at line start
        myFixture.configureByText(
            "test.asm",
            """
                %macro baz 0
                    nop
                %endmacro

                <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()
        println("Completions at empty line:")
        println("  Total: ${completions.size}")

        // Print all completions that start with 'b'
        val bCompletions = completions.filter { it.lookupString.startsWith("b", ignoreCase = true) }
        println("  Completions starting with 'b': ${bCompletions.size}")
        bCompletions.take(10).forEach {
            println("    - ${it.lookupString}")
        }

        val hasBaz = completions.any { it.lookupString == "baz" }
        println("  Has baz: $hasBaz")

        assertTrue("baz macro should appear at empty line", hasBaz)
    }
}