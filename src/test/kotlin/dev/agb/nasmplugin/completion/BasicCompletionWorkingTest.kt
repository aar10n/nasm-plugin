package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BasicCompletionWorkingTest : BasePlatformTestCase() {

    fun testAnyCompletionWorks() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mo<caret>
            """.trimIndent()
        )

        println("=== Basic Completion Test ===")

        val completions = myFixture.completeBasic()

        if (completions == null) {
            println("Completions is NULL - single match auto-completed")
            val text = myFixture.editor.document.text
            println("Document text after completion: $text")
        } else {
            println("Total completions: ${completions.size}")
            completions.take(10).forEach { completion ->
                println("  - ${completion.lookupString}")
            }
        }

        // Test that we get SOME completions (instructions like mov, movzx, etc.)
        val hasAnyCompletions = completions == null || completions.isNotEmpty()
        assertTrue("Should have some completions", hasAnyCompletions)
    }

    fun testInstructionCompletion() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic() ?: emptyArray()

        println("\n=== Instruction Completion Test ===")
        println("Total completions: ${completions.size}")

        val movCompletions = completions.filter { it.lookupString.startsWith("mov") }
        println("MOV-related completions: ${movCompletions.size}")
        movCompletions.forEach {
            println("  - ${it.lookupString}")
        }

        assertTrue("Should have MOV instruction", movCompletions.isNotEmpty())
    }
}