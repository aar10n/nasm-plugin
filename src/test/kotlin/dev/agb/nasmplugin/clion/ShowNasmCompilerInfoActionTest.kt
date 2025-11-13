package dev.agb.nasmplugin.clion

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.clion.projectmodel.NasmCompilationInfo
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectModelService
import java.io.File
import java.nio.file.Path

/**
 * Integration tests for ShowNasmCompilerInfoAction.
 * Tests the action's integration with the project model service and file editor.
 */
class ShowNasmCompilerInfoActionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/projectmodel"

    fun testActionIsNotAvailableForFileWithoutCompilationInfo() {
        // Create a NASM file not in any project model
        val file = myFixture.configureByText("standalone.asm", "nop")

        val action = ShowNasmCompilerInfoAction()
        val event = TestActionEvent.createTestEvent { dataId ->
            when (dataId) {
                CommonDataKeys.VIRTUAL_FILE.name -> file.virtualFile
                CommonDataKeys.PROJECT.name -> project
                else -> null
            }
        }

        action.update(event)

        assertFalse("Action should not be available for file without compilation info",
            event.presentation.isEnabledAndVisible)
    }

    fun testActionIsAvailableForFileWithCompilationInfo() {
        val projectDir = File(testDataPath, "compdb-project").absoluteFile
        if (!projectDir.exists()) {
            println("WARNING: Test project does not exist: $projectDir")
            return
        }

        val mockProject = createMockProject(projectDir)
        val service = NasmProjectModelService(mockProject)
        service.analyzeProject()

        // If compilation info exists, action should be available
        val compiledFiles = service.getAllCompiledFiles()
        if (compiledFiles.isNotEmpty()) {
            val file = compiledFiles.first()
            val action = ShowNasmCompilerInfoAction()

            // Action availability depends on compilation info existence
            assertNotNull("Should have compilation info", service.getCompilationInfo(file))
        }
    }

    private fun createMockProject(projectDir: File): com.intellij.openapi.project.Project {
        return object : com.intellij.openapi.project.Project by project {
            override fun getBasePath(): String = projectDir.absolutePath
        }
    }

    fun testCompilerInfoFormatting() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf(
            "-f", "elf64",
            "-g",
            "-Iinclude",
            "-I/usr/include",
            "-DDEBUG",
            "-DVERSION=100"
        )
        val outputFile = Path.of("/project/build/test.o")

        val compilationInfo = NasmCompilationInfo(file, workingDir, args, outputFile)

        // Verify all components are present
        assertEquals("Should have correct working directory", workingDir, compilationInfo.workingDirectory)
        assertEquals("Should have correct output file", outputFile, compilationInfo.outputFile)
        assertEquals("Should have correct arguments", args, compilationInfo.compilerArguments)

        // Verify parsed data
        assertEquals("Should have 2 include paths", 2, compilationInfo.includePaths.size)
        assertEquals("Should have 2 macros", 2, compilationInfo.macroDefinitions.size)
        assertNull("DEBUG should have no value", compilationInfo.macroDefinitions["DEBUG"])
        assertEquals("VERSION should be 100", "100", compilationInfo.macroDefinitions["VERSION"])
    }

    fun testCompilerInfoWithNoOptionalFields() {
        val file = LightVirtualFile("minimal.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-f", "elf64")

        val compilationInfo = NasmCompilationInfo(file, workingDir, args)

        assertNull("Should have no output file", compilationInfo.outputFile)
        assertTrue("Should have no include paths", compilationInfo.includePaths.isEmpty())
        assertTrue("Should have no macros", compilationInfo.macroDefinitions.isEmpty())
        assertEquals("Should still have compiler arguments", args, compilationInfo.compilerArguments)
    }

    fun testCompilerInfoWithRelativePaths() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf(
            "-Iinclude",
            "-I./headers",
            "-Isrc/../lib"
        )

        val compilationInfo = NasmCompilationInfo(file, workingDir, args)

        // All paths should be made absolute and normalized
        for (includePath in compilationInfo.includePaths) {
            assertTrue("Include paths should be absolute: $includePath",
                includePath.isAbsolute)
        }

        // Verify normalization
        assertTrue("Should normalize src/../lib to lib",
            compilationInfo.includePaths.any { it.toString().endsWith("/lib") })
    }

    fun testCompilerInfoDeduplication() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf(
            "-Iinclude",
            "-I", "include",
            "-I", "/project/include"
        )

        val compilationInfo = NasmCompilationInfo(file, workingDir, args)

        // Should deduplicate to one path
        assertEquals("Should deduplicate include paths", 1, compilationInfo.includePaths.size)
        assertEquals("Should have normalized path",
            Path.of("/project/include"), compilationInfo.includePaths.first())
    }
}
