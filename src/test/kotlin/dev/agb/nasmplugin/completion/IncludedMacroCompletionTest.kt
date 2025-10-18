package dev.agb.nasmplugin.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.getIncludedFiles

class IncludedMacroCompletionTest : BasePlatformTestCase() {

    fun testIncludedMacroCompletion() {
        // Create an include file with macros
        val incFile = myFixture.addFileToProject(
            "macros.inc",
            """
                %macro PRINT_STRING 1
                    mov rsi, %1
                    call print
                %endmacro

                %macro SETUP_STACK 0
                    mov rbp, rsp
                    sub rsp, 32
                %endmacro

                %macro EXIT_PROGRAM 1
                    mov rdi, %1
                    mov rax, 60
                    syscall
                %endmacro
            """.trimIndent()
        )

        // Create main file that includes the inc file
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
                %include "macros.inc"

                section .text
                global _start

                _start:
                    <caret>
            """.trimIndent()
        )

        // Check if included files are found
        val includedFiles = mainFile.getIncludedFiles()
        println("Found ${includedFiles.size} included files")
        includedFiles.forEach { file ->
            println("  - ${file.name}")
        }

        // Get completions
        val completions = myFixture.completeBasic() ?: emptyArray()

        println("\nTotal completions: ${completions.size}")

        // Look for our macros
        val macroNames = listOf("PRINT_STRING", "SETUP_STACK", "EXIT_PROGRAM")
        macroNames.forEach { macroName ->
            val found = completions.any { it.lookupString == macroName }
            println("  Found $macroName: $found")
            if (!found) {
                // Try case-insensitive search
                val foundCaseInsensitive = completions.any {
                    it.lookupString.equals(macroName, ignoreCase = true)
                }
                println("    Case-insensitive match: $foundCaseInsensitive")
            }
        }

        // Print all macro-like completions
        println("\nMacro-related completions:")
        completions.filter {
            it.lookupString.contains("PRINT", ignoreCase = true) ||
            it.lookupString.contains("SETUP", ignoreCase = true) ||
            it.lookupString.contains("EXIT", ignoreCase = true) ||
            it.lookupString.contains("MACRO", ignoreCase = true)
        }.forEach { completion ->
            println("  - ${completion.lookupString}")
        }

        // Test assertions
        assertTrue("PRINT_STRING should be in completions",
            completions.any { it.lookupString == "PRINT_STRING" })
        assertTrue("SETUP_STACK should be in completions",
            completions.any { it.lookupString == "SETUP_STACK" })
        assertTrue("EXIT_PROGRAM should be in completions",
            completions.any { it.lookupString == "EXIT_PROGRAM" })
    }

    fun testSimpleIncludedMacro() {
        // Even simpler test case
        myFixture.addFileToProject(
            "simple.inc",
            """
                %macro TEST_MACRO 0
                    nop
                %endmacro
            """.trimIndent()
        )

        myFixture.configureByText(
            "test.asm",
            """
                %include "simple.inc"

                TEST<caret>
            """.trimIndent()
        )

        val completions = myFixture.completeBasic()

        if (completions == null) {
            println("Auto-completed (single match)")
            val text = myFixture.file.text
            println("Text after completion: $text")
            assertTrue("TEST_MACRO should have been auto-completed",
                text.contains("TEST_MACRO"))
        } else {
            println("Found ${completions.size} completions")
            val hasTestMacro = completions.any { it.lookupString == "TEST_MACRO" }
            println("Has TEST_MACRO: $hasTestMacro")
            assertTrue("TEST_MACRO should be available", hasTestMacro)
        }
    }
}