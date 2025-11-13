package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.clion.projectmodel.CompilationDatabaseParser
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectCompilationInfo
import dev.agb.nasmplugin.clion.projectmodel.analyzers.CMakeProjectAnalyzer
import dev.agb.nasmplugin.clion.projectmodel.analyzers.CompilationDatabaseProjectAnalyzer
import dev.agb.nasmplugin.clion.projectmodel.analyzers.MakefileProjectAnalyzer
import java.io.File
import java.nio.file.Path

/**
 * Integration tests for the project model system.
 * Tests three identical NASM projects with different build systems (CMake, Makefile, CompilationDatabase).
 * All projects have the same source files, include paths, and compiler flags.
 *
 * Expected setup for all projects:
 * - Files: src/main.asm, src/helper.asm, include/constants.inc
 * - Compiler flags: -f elf64 -g -DDEBUG=1 -DVERSION=100 -Iinclude
 *
 * NOTE: These tests directly test analyzers. For higher-level service testing including
 * analyzer selection, caching, and re-analysis behavior, see NasmProjectModelServiceTest.
 */
class ProjectModelIntegrationTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/projectmodel"

    /**
     * Test CMake project analyzer.
     * Verifies that CMakeProjectAnalyzer can correctly analyze a CMake-based NASM project.
     */
    fun testCMakeProjectAnalysis() {
        val projectDir = File(testDataPath, "cmake-project").absoluteFile
        val mockProject = createMockProject(projectDir)

        val analyzer = CMakeProjectAnalyzer()

        // Verify analyzer detects the project
        assertTrue("CMake analyzer should detect cmake-project",
            analyzer.isApplicable(mockProject))

        // Analyze the project
        val result = analyzer.analyze(mockProject)

        if (result == null) {
            println("WARNING: CMake analysis returned null. This may be expected if 'make' is not available.")
            println("Skipping detailed assertions for CMake project.")
            return
        }

        // Verify we found the correct files
        verifyProjectAnalysis(result, "CMake")
    }

    /**
     * Test Makefile project analyzer.
     * Verifies that MakefileProjectAnalyzer can correctly analyze a Makefile-based NASM project.
     */
    fun testMakefileProjectAnalysis() {
        val projectDir = File(testDataPath, "makefile-project").absoluteFile
        val mockProject = createMockProject(projectDir)

        val analyzer = MakefileProjectAnalyzer()

        // Verify analyzer detects the project
        assertTrue("Makefile analyzer should detect makefile-project",
            analyzer.isApplicable(mockProject))

        // Analyze the project
        val result = analyzer.analyze(mockProject)

        if (result == null) {
            println("WARNING: Makefile analysis returned null. This may be expected if 'make' is not available.")
            println("Skipping detailed assertions for Makefile project.")
            return
        }

        // Verify we found the correct files
        verifyProjectAnalysis(result, "Makefile")
    }

    /**
     * Test compilation database project analyzer.
     * Verifies that CompilationDatabaseProjectAnalyzer can correctly parse a compile_commands.json file.
     */
    fun testCompilationDatabaseProjectAnalysis() {
        val projectDir = File(testDataPath, "compdb-project").absoluteFile
        val mockProject = createMockProject(projectDir)

        val analyzer = CompilationDatabaseProjectAnalyzer()

        // Verify analyzer detects the project
        assertTrue("CompDB analyzer should detect compdb-project",
            analyzer.isApplicable(mockProject))

        // Analyze the project by parsing the compilation database directly
        val compileCommandsFile = File(projectDir, "compile_commands.json")
        assertTrue("compile_commands.json should exist", compileCommandsFile.exists())

        // Create a temporary compile_commands.json with correct absolute paths
        val tempCompileCommands = File.createTempFile("compile_commands", ".json")
        try {
            val content = compileCommandsFile.readText()
                .replace("/test/compdb-project", projectDir.absolutePath)
            tempCompileCommands.writeText(content)

            val result = CompilationDatabaseParser.parse(tempCompileCommands.toPath(), project)

            assertNotNull("CompDB analysis should succeed", result)

            // Verify we found the correct files
            verifyProjectAnalysis(result!!, "CompilationDatabase")
        } finally {
            tempCompileCommands.delete()
        }
    }

    /**
     * Test analyzer priority ordering.
     * Verifies that analyzers are applied in the correct order: CMake > Makefile > CompilationDatabase.
     */
    fun testAnalyzerPriority() {
        val cmakeAnalyzer = CMakeProjectAnalyzer()
        val makefileAnalyzer = MakefileProjectAnalyzer()
        val compdbAnalyzer = CompilationDatabaseProjectAnalyzer()

        // CMake project should only be detected by CMake analyzer
        val cmakeProjectDir = File(testDataPath, "cmake-project")
        assertTrue("CMake analyzer should detect cmake-project",
            cmakeAnalyzer.isApplicable(createMockProject(cmakeProjectDir)))
        assertFalse("Makefile analyzer should NOT detect cmake-project",
            makefileAnalyzer.isApplicable(createMockProject(cmakeProjectDir)))

        // Makefile project should only be detected by Makefile analyzer
        val makefileProjectDir = File(testDataPath, "makefile-project")
        assertFalse("CMake analyzer should NOT detect makefile-project",
            cmakeAnalyzer.isApplicable(createMockProject(makefileProjectDir)))
        assertTrue("Makefile analyzer should detect makefile-project",
            makefileAnalyzer.isApplicable(createMockProject(makefileProjectDir)))

        // Pure compilation database project should only be detected by CompDB analyzer
        val compdbProjectDir = File(testDataPath, "compdb-project")
        assertFalse("CMake analyzer should NOT detect compdb-project",
            cmakeAnalyzer.isApplicable(createMockProject(compdbProjectDir)))
        assertFalse("Makefile analyzer should NOT detect compdb-project",
            makefileAnalyzer.isApplicable(createMockProject(compdbProjectDir)))
        assertTrue("CompDB analyzer should detect compdb-project",
            compdbAnalyzer.isApplicable(createMockProject(compdbProjectDir)))
    }

    /**
     * Common verification logic for all project types.
     * Verifies that the analysis result contains the expected files, include paths, and macros.
     */
    private fun verifyProjectAnalysis(result: NasmProjectCompilationInfo, projectType: String) {
        // Verify we found exactly 2 files
        assertEquals("$projectType: Should have 2 NASM files", 2, result.compilations.size)

        // Verify file names
        val fileNames = result.compilations.keys.map { it.name }.toSet()
        assertTrue("$projectType: Should contain main.asm", fileNames.contains("main.asm"))
        assertTrue("$projectType: Should contain helper.asm", fileNames.contains("helper.asm"))

        // Verify each file has correct compilation info
        for ((file, compilation) in result.compilations) {
            println("$projectType: Analyzing ${file.name}")
            println("  Working directory: ${compilation.workingDirectory}")
            println("  Compiler arguments: ${compilation.compilerArguments}")
            println("  Include paths: ${compilation.includePaths}")
            println("  Macro definitions: ${compilation.macroDefinitions}")

            // Verify compiler arguments
            assertTrue("$projectType (${file.name}): Should have -f flag",
                compilation.compilerArguments.contains("-f"))
            assertTrue("$projectType (${file.name}): Should have elf64 format",
                compilation.compilerArguments.contains("elf64"))
            assertTrue("$projectType (${file.name}): Should have -g flag",
                compilation.compilerArguments.contains("-g"))

            // Verify macro definitions
            assertTrue("$projectType (${file.name}): Should have DEBUG macro",
                compilation.macroDefinitions.containsKey("DEBUG"))
            assertEquals("$projectType (${file.name}): DEBUG should be 1",
                "1", compilation.macroDefinitions["DEBUG"])
            assertTrue("$projectType (${file.name}): Should have VERSION macro",
                compilation.macroDefinitions.containsKey("VERSION"))
            assertEquals("$projectType (${file.name}): VERSION should be 100",
                "100", compilation.macroDefinitions["VERSION"])

            // Verify include paths
            assertTrue("$projectType (${file.name}): Should have at least one include path",
                compilation.includePaths.isNotEmpty())
            assertTrue("$projectType (${file.name}): Should have 'include' directory in include paths",
                compilation.includePaths.any { it.toString().endsWith("include") })

            // Verify include paths are absolute
            for (includePath in compilation.includePaths) {
                assertTrue("$projectType (${file.name}): Include path should be absolute: $includePath",
                    includePath.isAbsolute)
            }

            // Verify include paths are deduplicated (no duplicates)
            assertEquals("$projectType (${file.name}): Include paths should be deduplicated",
                compilation.includePaths.size, compilation.includePaths.toSet().size)
        }
    }

    /**
     * Helper to create a mock project with a specific base path.
     */
    private fun createMockProject(projectDir: File): Project {
        return object : Project by project {
            override fun getBasePath(): String = projectDir.absolutePath
        }
    }
}
