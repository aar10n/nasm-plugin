package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DebugGlobalTest : BasePlatformTestCase() {

    fun testDebugGlobalDirectiveCompletion() {
        myFixture.configureByText("test.asm", """
            extern printf

            section .text
            my_func:
            .local_label:
                nop

            global <caret>
        """.trimIndent())

        // First, test context detection
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: myFixture.file.findElementAt(myFixture.caretOffset - 1)
        if (position != null) {
            val context = CompletionContextDetector.detectContext(position)
            println("Detected context: ${context::class.simpleName}")
        }

        val completions = myFixture.completeBasic() ?: return

        println("Total completions in global context: ${completions.size}")

        completions.forEach { item ->
            println("  - ${item.lookupString}")
        }

        val names = completions.map { it.lookupString }

        println("\nChecking specific items:")
        println("  printf (extern) present: ${"printf" in names}")
        println("  my_func (global label) present: ${"my_func" in names}")
        println("  .local_label (local label) present: ${".local_label" in names}")
    }
}