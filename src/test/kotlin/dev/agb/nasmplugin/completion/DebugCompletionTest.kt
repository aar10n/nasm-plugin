package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DebugCompletionTest : BasePlatformTestCase() {

    fun testDebugAfterPercentCompletion() {
        myFixture.configureByText("test.asm", "%<caret>")

        // First, test context detection directly
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: myFixture.file.findElementAt(myFixture.caretOffset - 1)
        if (position != null) {
            val context = CompletionContextDetector.detectContext(position)
            println("Detected context: ${context::class.simpleName}")
            println("Position text: '${position.text}'")
            println("Position offset: ${position.textOffset}")
            println("Caret offset: ${myFixture.caretOffset}")
        }

        val completions = myFixture.completeBasic()

        if (completions == null) {
            println("No completions returned")
            return
        }

        println("Total completions: ${completions.size}")

        // Print first 10 completions
        completions.take(10).forEach { item ->
            println("  - ${item.lookupString} (${item.psiElement?.javaClass?.simpleName})")
        }

        // Check for specific items
        val names = completions.map { it.lookupString }
        println("\nLooking for 'macro': ${names.contains("macro")}")
        println("Looking for '%macro': ${names.contains("%macro")}")
        println("Looking for 'define': ${names.contains("define")}")
        println("Looking for '%define': ${names.contains("%define")}")

        // Check for instructions (shouldn't be there after %)
        val instructions = names.filter {
            it in listOf("aaa", "aad", "mov", "add", "sub", "push", "pop")
        }
        println("\nInstructions found (should be empty): $instructions")

        // Also check what completions start with %
        val withPercent = names.filter { it.startsWith("%") }
        println("\nCompletions starting with %: ${withPercent.take(5)}")

        val withoutPercent = names.filter { !it.startsWith("%") &&
            (it == "macro" || it == "define" || it == "include" || it == "assign") }
        println("Expected completions without %: $withoutPercent")
    }
}