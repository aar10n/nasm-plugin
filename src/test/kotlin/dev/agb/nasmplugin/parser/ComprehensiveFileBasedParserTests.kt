package dev.agb.nasmplugin.parser

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.streams.toList

/**
 * Comprehensive file-based parser tests that auto-discover and test all .asm files
 * in the testData/parser directory structure.
 *
 * This replaces the hardcoded parser tests with a data-driven approach where
 * adding a new test case is as simple as adding a new .asm file to the appropriate
 * subdirectory.
 *
 * Directory structure:
 * - testData/parser/instructions/ - Instruction parsing tests
 * - testData/parser/labels/ - Label definition tests
 * - testData/parser/macros/ - Macro and preprocessor tests
 * - testData/parser/directives/ - NASM directive tests
 * - testData/parser/data/ - Data definition tests
 * - testData/parser/expressions/ - Expression parsing tests
 * - testData/parser/addressing/ - Addressing mode tests
 * - testData/parser/preprocessor/ - Preprocessor conditional tests
 * - testData/parser/repetition/ - Repetition directive tests
 * - testData/parser/structures/ - Structure definition tests
 * - testData/parser/special_syntax/ - Special NASM syntax tests
 * - testData/parser/numeric_literals/ - Number literal tests
 * - testData/parser/comments/ - Comment tests
 * - testData/parser/edge_cases/ - Edge cases (empty files, no newline, etc.)
 * - testData/parser/error_recovery/ - Error recovery tests (may have intentional errors)
 */
class ComprehensiveFileBasedParserTests : ParsingTestCase("", "asm", NasmParserDefinition()) {

    override fun getTestDataPath(): String = "src/test/testData/parser"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true

    /**
     * Discovers all .asm files in the test data directory, organized by subdirectory.
     */
    private fun discoverTestFiles(): Map<String, List<Path>> {
        val testDataDir = Path.of(testDataPath)

        if (!Files.exists(testDataDir) || !Files.isDirectory(testDataDir)) {
            return emptyMap()
        }

        val directories = Files.list(testDataDir).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .toList()
        }

        return directories.associate { dir ->
            val categoryName = dir.name
            val testFiles = Files.walk(dir, 1).use { walkStream ->
                walkStream
                    .filter { it.isRegularFile() && it.extension == "asm" }
                    .sorted()
                    .toList()
            }
            categoryName to testFiles
        }.filterValues { it.isNotEmpty() }
    }

    /**
     * Main test method that discovers and runs all parser tests.
     * Files in the error_recovery directory are allowed to have parse errors.
     */
    fun testAllParserFiles() {
        val testFilesByCategory = discoverTestFiles()

        assertTrue(
            "No test files found in $testDataPath",
            testFilesByCategory.isNotEmpty()
        )

        val totalFiles = testFilesByCategory.values.sumOf { it.size }
        println("\n=== Comprehensive Parser Tests ===")
        println("Found $totalFiles test files across ${testFilesByCategory.size} categories\n")

        val failures = mutableListOf<String>()
        val errorRecoveryResults = mutableListOf<String>()
        var passedCount = 0

        // Test each category
        for ((category, testFiles) in testFilesByCategory.toSortedMap()) {
            println("Category: $category (${testFiles.size} files)")

            for (testPath in testFiles) {
                val fileName = testPath.name
                val testName = "$category/$fileName"
                print("  Testing $fileName... ")

                try {
                    val code = testPath.readText()
                    val psiFile = createPsiFile(fileName, code)
                    val errors = findParseErrors(psiFile)

                    // Error recovery tests are allowed to have errors
                    if (category == "error_recovery") {
                        println("✓ PARSED (${errors.size} error(s) - expected)")
                        errorRecoveryResults.add("$testName: ${errors.size} error(s)")
                        passedCount++
                    } else if (errors.isEmpty()) {
                        println("✓ PASSED")
                        passedCount++
                    } else {
                        println("✗ FAILED")
                        failures.add(buildErrorReport(testName, psiFile, errors))
                    }
                } catch (e: Exception) {
                    println("✗ EXCEPTION")
                    failures.add("$testName: Exception during parsing - ${e.message}\n${e.stackTraceToString()}")
                }
            }
            println()
        }

        // Print summary
        println("=== Test Summary ===")
        println("Total:  $totalFiles")
        println("Passed: $passedCount")
        println("Failed: ${failures.size}")

        if (errorRecoveryResults.isNotEmpty()) {
            println("\n=== Error Recovery Tests ===")
            errorRecoveryResults.forEach { println(it) }
        }

        // Fail test if any non-error-recovery files had errors
        if (failures.isNotEmpty()) {
            val report = buildString {
                appendLine("\n\n=== PARSE FAILURES ===\n")
                failures.forEach { appendLine(it) }
            }
            fail(report)
        }
    }

    /**
     * Test that verifies all instruction test files parse correctly.
     */
    fun testInstructionFiles() {
        testCategory("instructions")
    }

    /**
     * Test that verifies all label test files parse correctly.
     */
    fun testLabelFiles() {
        testCategory("labels")
    }

    /**
     * Test that verifies all macro test files parse correctly.
     */
    fun testMacroFiles() {
        testCategory("macros")
    }

    /**
     * Test that verifies all directive test files parse correctly.
     */
    fun testDirectiveFiles() {
        testCategory("directives")
    }

    /**
     * Test that verifies all data definition test files parse correctly.
     */
    fun testDataFiles() {
        testCategory("data")
    }

    /**
     * Test that verifies all expression test files parse correctly.
     */
    fun testExpressionFiles() {
        testCategory("expressions")
    }

    /**
     * Test that verifies all addressing mode test files parse correctly.
     */
    fun testAddressingFiles() {
        testCategory("addressing")
    }

    /**
     * Test that verifies all preprocessor test files parse correctly.
     */
    fun testPreprocessorFiles() {
        testCategory("preprocessor")
    }

    /**
     * Test that verifies all edge case test files parse correctly.
     */
    fun testEdgeCaseFiles() {
        testCategory("edge_cases")
    }

    /**
     * Helper method to test all files in a specific category.
     */
    private fun testCategory(categoryName: String, allowErrors: Boolean = false) {
        val categoryDir = Path.of(testDataPath, categoryName)

        if (!Files.exists(categoryDir)) {
            println("Skipping category '$categoryName' - directory not found")
            return
        }

        val testFiles = Files.walk(categoryDir, 1)
            .filter { it.isRegularFile() && it.extension == "asm" }
            .sorted()
            .toList()

        assertTrue(
            "No test files found in category '$categoryName'",
            testFiles.isNotEmpty()
        )

        println("\n=== Testing category: $categoryName (${testFiles.size} files) ===")

        val failures = mutableListOf<String>()
        var passedCount = 0

        for (testPath in testFiles) {
            val fileName = testPath.name
            print("Testing $fileName... ")

            try {
                val code = testPath.readText()
                val psiFile = createPsiFile(fileName, code)
                val errors = findParseErrors(psiFile)

                if (allowErrors || errors.isEmpty()) {
                    println("✓ PASSED")
                    passedCount++
                } else {
                    println("✗ FAILED")
                    failures.add(buildErrorReport(fileName, psiFile, errors))
                }
            } catch (e: Exception) {
                println("✗ EXCEPTION")
                failures.add("$fileName: Exception during parsing - ${e.message}")
            }
        }

        println("\nPassed: $passedCount/${testFiles.size}\n")

        if (failures.isNotEmpty()) {
            val report = buildString {
                appendLine("\n=== FAILURES IN CATEGORY: $categoryName ===\n")
                failures.forEach { appendLine(it) }
            }
            fail(report)
        }
    }

    /**
     * Finds all parse errors in a PSI file.
     */
    private fun findParseErrors(file: PsiFile): Collection<PsiErrorElement> =
        PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)

    /**
     * Builds a detailed error report for a file that failed to parse.
     */
    private fun buildErrorReport(
        fileName: String,
        file: PsiFile,
        errors: Collection<PsiErrorElement>
    ): String = buildString {
        appendLine("$fileName has ${errors.size} parse error(s):")
        errors.forEach { error ->
            val lineNumber = getLineNumber(file, error)
            val errorText = error.text.replace("\n", "\\n")
            appendLine("  Line $lineNumber: ${error.errorDescription}")
            appendLine("    at offset ${error.textRange.startOffset}: '$errorText'")
        }
        appendLine()
    }

    /**
     * Calculates the line number of a PSI element for error reporting.
     */
    private fun getLineNumber(file: PsiFile, element: PsiErrorElement): Int {
        val text = file.text
        val offset = element.textRange.startOffset
        return text.take(offset).count { it == '\n' } + 1
    }
}
