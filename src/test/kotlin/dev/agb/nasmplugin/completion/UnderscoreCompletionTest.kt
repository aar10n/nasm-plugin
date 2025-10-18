package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UnderscoreCompletionTest : BasePlatformTestCase() {

    fun testUnderscoreInMacroName() {
        println("=== Testing underscore in macro names ===\n")

        // Test with underscore in macro name
        myFixture.configureByText(
            "test1.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                %macro mymacro 0
                    nop
                %endmacro

                section .text
                my<caret>
            """.trimIndent()
        )

        var completions = myFixture.completeBasic() ?: emptyArray()
        println("1. Prefix 'my' (my<caret>): ${completions.size} completions")
        val macroCompletions1 = completions.filter {
            it.lookupString.startsWith("my", ignoreCase = true)
        }
        macroCompletions1.forEach { println("   - ${it.lookupString}") }

        // Test with underscore in prefix
        myFixture.configureByText(
            "test2.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                %macro mymacro 0
                    nop
                %endmacro

                section .text
                my_<caret>
            """.trimIndent()
        )

        completions = myFixture.completeBasic() ?: emptyArray()
        println("\n2. Prefix 'my_' (my_<caret>): ${completions.size} completions")
        val macroCompletions2 = completions.filter {
            it.lookupString.startsWith("my", ignoreCase = true)
        }
        macroCompletions2.forEach { println("   - ${it.lookupString}") }

        // Test with 'm' only
        myFixture.configureByText(
            "test3.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                section .text
                m<caret>
            """.trimIndent()
        )

        completions = myFixture.completeBasic() ?: emptyArray()
        println("\n3. Prefix 'm' (m<caret>): ${completions.size} completions")
        val myMacros = completions.filter { it.lookupString == "my_macro" }
        println("   Has 'my_macro': ${myMacros.isNotEmpty()}")

        // Test with different separator
        myFixture.configureByText(
            "test4.asm",
            """
                %macro my-macro 0
                    nop
                %endmacro

                section .text
                my-<caret>
            """.trimIndent()
        )

        completions = myFixture.completeBasic() ?: emptyArray()
        println("\n4. Prefix 'my-' (my-<caret>): ${completions.size} completions")

        // Test after whitespace
        myFixture.configureByText(
            "test5.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                section .text
                    my_<caret>
            """.trimIndent()
        )

        completions = myFixture.completeBasic() ?: emptyArray()
        println("\n5. With indent 'my_' (    my_<caret>): ${completions.size} completions")
    }
}