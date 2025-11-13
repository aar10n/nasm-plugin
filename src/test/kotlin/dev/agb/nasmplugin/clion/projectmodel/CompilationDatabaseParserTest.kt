package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.clion.projectmodel.CompilationDatabaseParser
import java.nio.file.Files
import kotlin.io.path.writeText

/**
 * Tests for CompilationDatabaseParser.
 */
class CompilationDatabaseParserTest : BasePlatformTestCase() {

    fun testParseEmptyDatabase() {
        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            tempFile.writeText("[]")
            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNull("Empty database should return null", result)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    fun testParseNonExistentFile() {
        val nonExistentPath = Files.createTempFile("test", ".json")
        Files.deleteIfExists(nonExistentPath)
        val result = CompilationDatabaseParser.parse(nonExistentPath, project)
        assertNull("Non-existent file should return null", result)
    }

    fun testParseInvalidJson() {
        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            tempFile.writeText("not valid json")
            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNull("Invalid JSON should return null", result)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    fun testParseDatabaseWithNasmFiles() {
        val tempDir = Files.createTempDirectory("test_project").toRealPath()
        VfsRootAccess.allowRootAccess(testRootDisposable, tempDir.toString())

        val sourceFile = tempDir.resolve("test.asm")
        Files.createFile(sourceFile)

        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            val json = """
                [
                  {
                    "directory": "$tempDir",
                    "command": "nasm -f elf64 -g -DDEBUG=1 -Iinclude test.asm -o test.o",
                    "file": "${sourceFile.toAbsolutePath()}"
                  }
                ]
            """.trimIndent()
            tempFile.writeText(json)

            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNotNull("Should parse database with NASM file", result)
            assertEquals("Should have 1 compilation", 1, result!!.compilations.size)

            val compilation = result.compilations.values.first()
            assertEquals("Working directory should match", tempDir, compilation.workingDirectory)
            assertTrue("Should have -f argument", compilation.compilerArguments.contains("-f"))
            assertTrue("Should have elf64 argument", compilation.compilerArguments.contains("elf64"))
            assertTrue("Should have -g argument", compilation.compilerArguments.contains("-g"))
        } finally {
            Files.deleteIfExists(tempFile)
            Files.deleteIfExists(sourceFile)
            Files.deleteIfExists(tempDir)
        }
    }

    fun testParseDatabaseWithArgumentsArray() {
        val tempDir = Files.createTempDirectory("test_project").toRealPath()
        VfsRootAccess.allowRootAccess(testRootDisposable, tempDir.toString())

        val sourceFile = tempDir.resolve("test.asm")
        Files.createFile(sourceFile)

        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            val json = """
                [
                  {
                    "directory": "$tempDir",
                    "arguments": ["nasm", "-f", "elf64", "-DVERSION=100", "${sourceFile.toAbsolutePath()}", "-o", "test.o"],
                    "file": "${sourceFile.toAbsolutePath()}"
                  }
                ]
            """.trimIndent()
            tempFile.writeText(json)

            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNotNull("Should parse database with arguments array", result)
            assertEquals("Should have 1 compilation", 1, result!!.compilations.size)

            val compilation = result.compilations.values.first()
            assertTrue("Should have -f argument", compilation.compilerArguments.contains("-f"))
            assertTrue("Should have elf64 argument", compilation.compilerArguments.contains("elf64"))
        } finally {
            Files.deleteIfExists(tempFile)
            Files.deleteIfExists(sourceFile)
            Files.deleteIfExists(tempDir)
        }
    }

    fun testParseDatabaseFiltersNonNasmFiles() {
        val tempDir = Files.createTempDirectory("test_project").toRealPath()
        VfsRootAccess.allowRootAccess(testRootDisposable, tempDir.toString())

        val asmFile = tempDir.resolve("test.asm")
        val cFile = tempDir.resolve("test.c")
        Files.createFile(asmFile)
        Files.createFile(cFile)

        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            val json = """
                [
                  {
                    "directory": "$tempDir",
                    "command": "nasm -f elf64 ${asmFile.toAbsolutePath()}",
                    "file": "${asmFile.toAbsolutePath()}"
                  },
                  {
                    "directory": "$tempDir",
                    "command": "gcc -c ${cFile.toAbsolutePath()}",
                    "file": "${cFile.toAbsolutePath()}"
                  }
                ]
            """.trimIndent()
            tempFile.writeText(json)

            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNotNull("Should parse database", result)
            assertEquals("Should only have NASM file", 1, result!!.compilations.size)
        } finally {
            Files.deleteIfExists(tempFile)
            Files.deleteIfExists(asmFile)
            Files.deleteIfExists(cFile)
            Files.deleteIfExists(tempDir)
        }
    }

    fun testParseDatabaseWithMultipleNasmFiles() {
        val tempDir = Files.createTempDirectory("test_project").toRealPath()
        VfsRootAccess.allowRootAccess(testRootDisposable, tempDir.toString())

        val file1 = tempDir.resolve("test1.asm")
        val file2 = tempDir.resolve("test2.asm")
        Files.createFile(file1)
        Files.createFile(file2)

        val tempFile = Files.createTempFile("compile_commands", ".json")
        try {
            val json = """
                [
                  {
                    "directory": "$tempDir",
                    "command": "nasm -f elf64 ${file1.toAbsolutePath()}",
                    "file": "${file1.toAbsolutePath()}"
                  },
                  {
                    "directory": "$tempDir",
                    "command": "nasm -f elf64 ${file2.toAbsolutePath()}",
                    "file": "${file2.toAbsolutePath()}"
                  }
                ]
            """.trimIndent()
            tempFile.writeText(json)

            val result = CompilationDatabaseParser.parse(tempFile, project)
            assertNotNull("Should parse database", result)
            assertEquals("Should have 2 compilations", 2, result!!.compilations.size)
        } finally {
            Files.deleteIfExists(tempFile)
            Files.deleteIfExists(file1)
            Files.deleteIfExists(file2)
            Files.deleteIfExists(tempDir)
        }
    }
}
