package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.LightVirtualFile
import java.nio.file.Path

/**
 * Tests for NasmCompilationInfo flag parsing.
 */
class NasmCompilationInfoTest : BasePlatformTestCase() {

    fun testExtractIncludePathsWithSpaces() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-I", "include", "-I", "src/headers")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 include paths", 2, info.includePaths.size)
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/project/include")))
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/project/src/headers")))
    }

    fun testExtractIncludePathsWithoutSpaces() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-Iinclude", "-Isrc/headers")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 include paths", 2, info.includePaths.size)
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/project/include")))
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/project/src/headers")))
    }

    fun testExtractIncludePathsWithLowercaseI() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-iinclude", "-i", "src")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 include paths", 2, info.includePaths.size)
    }

    fun testExtractIncludePathsAbsolute() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-I/usr/include", "-I/opt/include")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 include paths", 2, info.includePaths.size)
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/usr/include")))
        assertTrue("Should contain absolute path", info.includePaths.contains(Path.of("/opt/include")))
    }

    fun testDeduplicateIncludePaths() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        // Same path specified multiple ways
        val args = listOf("-Iinclude", "-I", "include", "-I", "/project/include")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should deduplicate to 1 path", 1, info.includePaths.size)
        assertTrue("Should contain normalized path", info.includePaths.contains(Path.of("/project/include")))
    }

    fun testNormalizeIncludePaths() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-Isrc/../include", "-I./headers")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 include paths", 2, info.includePaths.size)
        assertTrue("Should normalize path", info.includePaths.contains(Path.of("/project/include")))
        assertTrue("Should normalize path", info.includePaths.contains(Path.of("/project/headers")))
    }

    fun testExtractMacroDefinitionsWithValues() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-DDEBUG=1", "-D", "VERSION=100")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 macros", 2, info.macroDefinitions.size)
        assertEquals("DEBUG should be 1", "1", info.macroDefinitions["DEBUG"])
        assertEquals("VERSION should be 100", "100", info.macroDefinitions["VERSION"])
    }

    fun testExtractMacroDefinitionsWithoutValues() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-DDEBUG", "-D", "RELEASE")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 2 macros", 2, info.macroDefinitions.size)
        assertNull("DEBUG should have no value", info.macroDefinitions["DEBUG"])
        assertNull("RELEASE should have no value", info.macroDefinitions["RELEASE"])
    }

    fun testExtractMacroDefinitionsMixed() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-DDEBUG", "-DVERSION=100", "-DPLATFORM=x86_64")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertEquals("Should have 3 macros", 3, info.macroDefinitions.size)
        assertNull("DEBUG should have no value", info.macroDefinitions["DEBUG"])
        assertEquals("VERSION should be 100", "100", info.macroDefinitions["VERSION"])
        assertEquals("PLATFORM should be x86_64", "x86_64", info.macroDefinitions["PLATFORM"])
    }

    fun testExtractOutputFile() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-f", "elf64", "-o", "test.o")

        val info = NasmCompilationInfo(file, workingDir, args, Path.of("test.o"))

        assertEquals("Should have output file", Path.of("test.o"), info.outputFile)
    }

    fun testNoIncludePaths() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-f", "elf64", "-g")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertTrue("Should have no include paths", info.includePaths.isEmpty())
    }

    fun testNoMacroDefinitions() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf("-f", "elf64", "-g")

        val info = NasmCompilationInfo(file, workingDir, args)

        assertTrue("Should have no macro definitions", info.macroDefinitions.isEmpty())
    }

    fun testComplexCompilerArguments() {
        val file = LightVirtualFile("test.asm")
        val workingDir = Path.of("/project")
        val args = listOf(
            "-f", "elf64",
            "-g",
            "-F", "dwarf",
            "-Iinclude",
            "-I", "/usr/include",
            "-DDEBUG",
            "-DVERSION=100",
            "-o", "build/test.o"
        )

        val info = NasmCompilationInfo(file, workingDir, args)

        // Verify include paths
        assertEquals("Should have 2 include paths", 2, info.includePaths.size)

        // Verify macros
        assertEquals("Should have 2 macros", 2, info.macroDefinitions.size)
        assertNull("DEBUG should have no value", info.macroDefinitions["DEBUG"])
        assertEquals("VERSION should be 100", "100", info.macroDefinitions["VERSION"])

        // Verify all arguments are preserved
        assertEquals("Should preserve all arguments", args, info.compilerArguments)
    }
}
