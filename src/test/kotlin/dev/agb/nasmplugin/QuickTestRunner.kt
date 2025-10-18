package dev.agb.nasmplugin

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import dev.agb.nasmplugin.parser.NasmParserDefinition

/**
 * Quick test runner for debugging parser issues.
 * This test is useful for rapid iteration when developing grammar rules.
 *
 * Usage:
 * 1. Put your test NASM code in the testCode variable
 * 2. Run this test
 * 3. Check the output for parse errors and PSI tree structure
 *
 * To enable detailed PSI tree output, run with:
 *   ./gradlew test --tests QuickTestRunner -Dprint.parse.tree=true
 */
class QuickTestRunner : ParsingTestCase("", "asm", NasmParserDefinition()) {

    override fun getTestDataPath(): String = "src/test/resources"

    fun testQuickParse() {
        // Put your test code here for quick debugging
        val testCode = """
            add rax, rbx, rcx
        """.trimIndent()

        println("\n" + "=".repeat(80))
        println("QUICK TEST RUNNER")
        println("=".repeat(80))
        println("\nTest Code:")
        println("-".repeat(80))
        println(testCode)
        println("-".repeat(80))

        val psiFile = createPsiFile("test.asm", testCode)

        // Print PSI tree
        if (System.getProperty("print.parse.tree") == "true") {
            println("\nPSI Tree:")
            println("-".repeat(80))
            println(toParseTreeText(psiFile, false, true))
            println("-".repeat(80))
        }

        // Check for errors
        val errors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)

        if (errors.isEmpty()) {
            println("\n✓ SUCCESS: Code parsed without errors!")
        } else {
            println("\n✗ FAILURE: Found ${errors.size} parse error(s):")
            println("-".repeat(80))
            errors.forEach { error ->
                val lineNumber = getLineNumber(psiFile, error)
                println("\nLine $lineNumber: ${error.errorDescription}")
                println("  Offset: ${error.textRange.startOffset}")
                println("  Text: '${error.text.replace("\n", "\\n")}'")
                println("  Context: ${error.parent?.text?.take(50)?.replace("\n", "\\n")}")
            }
            println("-".repeat(80))
        }

        println("\n" + "=".repeat(80) + "\n")

        // Always print summary
        val instructionCount = psiFile.text.lines().count { it.trim().isNotEmpty() && !it.trim().startsWith(";") }
        println("Summary:")
        println("  Total lines: ${psiFile.text.lines().size}")
        println("  Non-empty lines: $instructionCount")
        println("  Parse errors: ${errors.size}")
        println("  PSI elements: ${countPsiElements(psiFile)}")
        println()

        assertTrue("Code should parse without errors but has ${errors.size} error(s)", errors.isEmpty())
    }

    private fun getLineNumber(file: com.intellij.psi.PsiFile, element: PsiErrorElement): Int {
        val text = file.text
        val offset = element.textRange.startOffset
        return text.take(offset).count { it == '\n' } + 1
    }

    private fun countPsiElements(element: com.intellij.psi.PsiElement): Int {
        var count = 1
        element.children.forEach { child ->
            count += countPsiElements(child)
        }
        return count
    }

    /**
     * Use this test to quickly check specific syntax patterns.
     * Useful for verifying grammar rules before writing full tests.
     */
    fun testSpecificPattern() {
        // Example patterns to test individually
        val patterns = listOf(
            "mov rax, rbx",
            "add [rax], 1",
            "vaddps zmm0, zmm1, zmm2",
            "%macro TEST 0\nnop\n%endmacro",
            "label: nop",
            "data db 'test', 0",
            "%if DEBUG\nmov rax, 1\n%endif"
        )

        patterns.forEach { pattern ->
            println("\nTesting pattern: $pattern")
            val psiFile = createPsiFile("test.asm", pattern)
            val errors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)

            if (errors.isEmpty()) {
                println("  ✓ OK")
            } else {
                println("  ✗ ERROR: ${errors.first().errorDescription}")
            }
        }
    }

    /**
     * Use this test to verify error recovery.
     * The parser should handle errors gracefully without crashing.
     */
    fun testErrorRecovery() {
        val invalidCode = """
            mov rax,          ; Missing operand
            invalid syntax    ; Completely invalid
            mov rbx, 2        ; Should still parse
        """.trimIndent()

        println("\nTesting error recovery with invalid code:")
        println(invalidCode)

        val psiFile = createPsiFile("test.asm", invalidCode)
        val errors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)

        println("\nFound ${errors.size} errors (expected):")
        errors.forEach { error ->
            println("  - ${error.errorDescription}")
        }

        // Verify file still parsed (didn't crash)
        assertNotNull("File should still create PSI even with errors", psiFile)
        println("\n✓ Parser recovered from errors successfully")
    }
}
