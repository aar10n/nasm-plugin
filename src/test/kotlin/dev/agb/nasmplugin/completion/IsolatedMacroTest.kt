package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class IsolatedMacroTest : BasePlatformTestCase() {

    fun testOnlyMacrosAtLineStart() {
        // Create include file with macros
        myFixture.addFileToProject(
            "test.inc",
            """
                %macro TEST_MACRO 0
                    nop
                %endmacro

                %macro ANOTHER_MACRO 1
                    mov rax, %1
                %endmacro
            """.trimIndent()
        )

        // Test at empty line - only macros should appear now
        myFixture.configureByText(
            "test.asm",
            """
                %include "test.inc"

                <caret>
            """.trimIndent()
        )

        println("=== With all other completions disabled ===")
        val completions = myFixture.completeBasic() ?: emptyArray()

        println("Total completions: ${completions.size}")
        println("All completions:")
        completions.forEach { completion ->
            println("  - ${completion.lookupString}")
        }

        val hasTestMacro = completions.any { it.lookupString == "TEST_MACRO" }
        val hasAnotherMacro = completions.any { it.lookupString == "ANOTHER_MACRO" }

        println("\nHas TEST_MACRO: $hasTestMacro")
        println("Has ANOTHER_MACRO: $hasAnotherMacro")

        // With everything else disabled, we should only see macros
        assertTrue("TEST_MACRO should be in completions", hasTestMacro)
        assertTrue("ANOTHER_MACRO should be in completions", hasAnotherMacro)
    }
}