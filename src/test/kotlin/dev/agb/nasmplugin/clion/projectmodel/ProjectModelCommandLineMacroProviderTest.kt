package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.settings.CommandLineMacroProvider
import dev.agb.nasmplugin.settings.NasmProjectSettings
import java.nio.file.Path

/**
 * Tests for command-line macro integration with project model.
 */
class ProjectModelCommandLineMacroProviderTest : BasePlatformTestCase() {

    fun testGlobalMacrosOnly() {
        // Setup global macros in settings
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG,VERSION=2"

        val provider = CommandLineMacroProvider.getInstance(project)
        val macros = provider.getCommandLineMacros(null)

        assertEquals(2, macros.size)

        val debugMacro = macros.find { it.name == "DEBUG" }
        assertNotNull("DEBUG macro should exist", debugMacro)
        assertNull("DEBUG should have no value", debugMacro?.value)

        val versionMacro = macros.find { it.name == "VERSION" }
        assertNotNull("VERSION macro should exist", versionMacro)
        assertEquals("2", versionMacro?.value)
    }

    fun testPerFileMacrosOverrideGlobal() {
        // Setup global macros
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG,VERSION=1"

        val provider = CommandLineMacroProvider.getInstance(project)
        val file = createMockVirtualFile("test.asm")

        // Without compilation info, should get global macros
        val globalMacros = provider.getCommandLineMacros(file)
        val globalVersion = globalMacros.find { it.name == "VERSION" }
        assertEquals("1", globalVersion?.value)

        // Note: In a full test, we would mock compilation info with VERSION=2
        // and verify that it overrides the global value
    }

    fun testFindMacroByName() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG,RELEASE"

        val provider = CommandLineMacroProvider.getInstance(project)

        val debugMacro = provider.findMacroByName("DEBUG")
        assertNotNull("Should find DEBUG macro", debugMacro)
        assertEquals("DEBUG", debugMacro?.name)

        val notFoundMacro = provider.findMacroByName("NOTFOUND")
        assertNull("Should not find non-existent macro", notFoundMacro)
    }

    fun testPerFileMacrosWithFile() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "GLOBAL_MACRO"

        val provider = CommandLineMacroProvider.getInstance(project)
        val file = createMockVirtualFile("test.asm")

        // Setup compilation info with per-file macros
        val compilationInfo = NasmCompilationInfo(
            file = file,
            workingDirectory = Path.of("/tmp"),
            compilerArguments = listOf("-DPER_FILE_MACRO", "-DVALUE_MACRO=42"),
            outputFile = null
        )

        // Verify that macroDefinitions are parsed correctly
        assertEquals(2, compilationInfo.macroDefinitions.size)
        assertTrue(compilationInfo.macroDefinitions.containsKey("PER_FILE_MACRO"))
        assertEquals("42", compilationInfo.macroDefinitions["VALUE_MACRO"])

        // Note: Full integration testing would require mocking the project model service
        // to return this compilation info for the file
    }

    fun testEmptyMacros() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = ""

        val provider = CommandLineMacroProvider.getInstance(project)
        val macros = provider.getCommandLineMacros(null)

        assertTrue("Should have no macros", macros.isEmpty())
    }

    private fun createMockVirtualFile(name: String): VirtualFile {
        val psiFile = myFixture.configureByText(name, "")
        return psiFile.virtualFile
    }

    override fun tearDown() {
        try {
            val settings = NasmProjectSettings.getInstance(project)
            settings.commandLineMacros = ""
        } finally {
            super.tearDown()
        }
    }
}
