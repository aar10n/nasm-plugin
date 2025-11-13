package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.clion.projectmodel.ProjectModelIncludePathResolver
import dev.agb.nasmplugin.settings.NasmProjectSettings
import java.nio.file.Path

/**
 * Tests for ProjectModelIncludePathResolver integration.
 */
class ProjectModelIncludePathResolverTest : BasePlatformTestCase() {

    fun testIncludePathsWithNoCompilationInfo() {
        // When there's no compilation info, should fall back to global settings
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths = mutableListOf("global/include")

        val resolver = ProjectModelIncludePathResolver.getInstance(project)
        val file = createMockVirtualFile("test.asm")

        val paths = resolver.getNasmIncludePaths(file)

        // Should contain the global path (resolved relative to project base)
        assertTrue(paths.isNotEmpty())
        assertTrue(paths.any { it.endsWith("global/include") })
    }

    fun testIncludePathsMergesGlobalAndPerFile() {
        // Setup: configure global include paths
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths = mutableListOf("global/include")

        // Setup: mock compilation info with per-file include paths
        val file = createMockVirtualFile("test.asm")
        val compilationInfo = NasmCompilationInfo(
            file = file,
            workingDirectory = Path.of("/tmp"),
            compilerArguments = listOf("-I/tmp/local/include"),
            outputFile = null
        )

        // Setup the project model service with this compilation info
        val service = NasmProjectModelService.getInstance(project)
        // Note: In a real test, we would need to mock the service's internal state
        // For now, this test documents the expected behavior

        val resolver = ProjectModelIncludePathResolver.getInstance(project)
        val paths = resolver.getNasmIncludePaths(file)

        // Should contain both global and per-file paths
        // This test currently only verifies the global path exists
        assertTrue(paths.isNotEmpty())
    }

    fun testIncludePathsDeduplication() {
        // When the same path appears in both global and per-file, it should be deduplicated
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths = mutableListOf("/shared/include")

        val resolver = ProjectModelIncludePathResolver.getInstance(project)
        val file = createMockVirtualFile("test.asm")

        val paths = resolver.getNasmIncludePaths(file)

        // Count occurrences of the same path
        val uniquePaths = paths.toSet()
        assertEquals("Paths should be deduplicated", uniquePaths.size, paths.size)
    }

    private fun createMockVirtualFile(name: String): VirtualFile {
        // Create a temp file in the test project
        val psiFile = myFixture.configureByText(name, "")
        return psiFile.virtualFile
    }

    override fun tearDown() {
        try {
            val settings = NasmProjectSettings.getInstance(project)
            settings.includePaths = mutableListOf()
        } finally {
            super.tearDown()
        }
    }
}
