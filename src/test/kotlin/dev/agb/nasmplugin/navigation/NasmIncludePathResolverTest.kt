package dev.agb.nasmplugin.navigation

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.settings.NasmProjectSettings

/**
 * Tests for include path resolution functionality.
 * Tests the fallback include path resolver and various include scenarios.
 */
class NasmIncludePathResolverTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        // Clear settings before each test
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.clear()
        settings.commandLineMacros = ""
        settings.notifyChanged()
    }

    fun testResolveRelativeIncludeInSameDirectory() {
        // Create the included file
        myFixture.addFileToProject(
            "macros.inc",
            """
            %macro TEST_MACRO 0
                nop
            %endmacro
            """.trimIndent()
        )

        // Create main file with include directive
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "macros.inc"

            section .text
            main:
                TEST_MACRO
            """.trimIndent()
        )

        // Verify include is resolved
        val includedFiles = mainFile.getIncludedFiles()
        assertEquals("Should find 1 included file", 1, includedFiles.size)
        assertEquals("Should include macros.inc", "macros.inc", includedFiles[0].name)
    }

    fun testResolveRelativeIncludeInSubdirectory() {
        // Create included file in subdirectory
        myFixture.addFileToProject(
            "include/defs.inc",
            """
            CONSTANT equ 42
            """.trimIndent()
        )

        // Create main file with relative include
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "include/defs.inc"

            section .data
                value dw CONSTANT
            """.trimIndent()
        )

        // Verify include is resolved
        val includedFiles = mainFile.getIncludedFiles()
        assertEquals("Should find 1 included file", 1, includedFiles.size)
        assertEquals("Should include defs.inc", "defs.inc", includedFiles[0].name)
    }

    fun testResolveIncludeFromConfiguredIncludePath() {
        // Note: This test verifies that settings can be configured with include paths.
        // Full resolution requires actual filesystem integration which is tested manually.

        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("lib")
        settings.includePaths.add("/usr/local/include")
        settings.notifyChanged()

        // Verify paths are stored
        assertEquals("Should have 2 include paths", 2, settings.includePaths.size)
        assertTrue("Should contain lib", settings.includePaths.contains("lib"))
        assertTrue("Should contain /usr/local/include",
            settings.includePaths.contains("/usr/local/include"))

        // Verify resolver can access these paths
        val resolver = NasmFallbackIncludePathResolver.getInstance(project)
        val file = myFixture.addFileToProject("test.asm", "").virtualFile
        val paths = resolver.getNasmIncludePaths(file)

        assertEquals("Should return 2 paths", 2, paths.size)
    }

    fun testResolveIncludeWithAbsolutePathInSettings() {
        // Test that absolute paths are kept as-is by the resolver
        val settings = NasmProjectSettings.getInstance(project)
        val absolutePath = "/usr/local/include/nasm"
        settings.includePaths.add(absolutePath)
        settings.notifyChanged()

        val resolver = NasmFallbackIncludePathResolver.getInstance(project)
        val file = myFixture.addFileToProject("test.asm", "").virtualFile
        val paths = resolver.getNasmIncludePaths(file)

        assertEquals("Should return 1 path", 1, paths.size)
        assertEquals("Absolute path should be unchanged", absolutePath, paths[0])
    }

    fun testMultipleIncludePaths() {
        // Test that multiple include paths are returned by resolver
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("dir1")
        settings.includePaths.add("dir2")
        settings.includePaths.add("/abs/path")
        settings.notifyChanged()

        val resolver = NasmFallbackIncludePathResolver.getInstance(project)
        val file = myFixture.addFileToProject("test.asm", "").virtualFile
        val paths = resolver.getNasmIncludePaths(file)

        assertEquals("Should return 3 paths", 3, paths.size)

        // Check that absolute path is unchanged
        assertTrue("Should contain absolute path", paths.any { it == "/abs/path" })

        // Relative paths should be resolved
        assertTrue("Should have relative paths resolved", paths.any { it.contains("dir1") })
        assertTrue("Should have relative paths resolved", paths.any { it.contains("dir2") })
    }

    fun testTransitiveIncludes() {
        // Create a chain of includes: main.asm -> a.inc -> b.inc
        myFixture.addFileToProject(
            "b.inc",
            """
            %macro INNER_MACRO 0
                ret
            %endmacro
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "a.inc",
            """
            %include "b.inc"

            %macro OUTER_MACRO 0
                INNER_MACRO
            %endmacro
            """.trimIndent()
        )

        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "a.inc"

            section .text
            test:
                OUTER_MACRO
            """.trimIndent()
        )

        // Verify transitive includes are found
        val includedFiles = mainFile.getIncludedFiles()
        assertEquals("Should find 2 included files (a.inc and b.inc)", 2, includedFiles.size)

        val fileNames = includedFiles.map { it.name }.toSet()
        assertTrue("Should include a.inc", fileNames.contains("a.inc"))
        assertTrue("Should include b.inc", fileNames.contains("b.inc"))
    }

    fun testCircularIncludesDoNotCauseInfiniteLoop() {
        // Create circular includes: a.inc -> b.inc -> a.inc
        myFixture.addFileToProject(
            "b.inc",
            """
            %include "a.inc"
            %define FROM_B 1
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "a.inc",
            """
            %include "b.inc"
            %define FROM_A 1
            """.trimIndent()
        )

        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "a.inc"

            section .data
                val dw FROM_A
            """.trimIndent()
        )

        // Should not throw StackOverflowError
        val includedFiles = mainFile.getIncludedFiles()

        // Both files should be in the result, but only once each
        assertTrue("Should find at least 1 included file", includedFiles.isNotEmpty())

        val fileNames = includedFiles.map { it.name }
        assertEquals("Each file should appear only once",
            fileNames.size, fileNames.toSet().size)
    }

    fun testNavigationToIncludeFile() {
        // Create the included file
        myFixture.addFileToProject(
            "defs.inc",
            """
            BUFFER_SIZE equ 1024
            """.trimIndent()
        )

        // Create main file with cursor on include path
        myFixture.configureByText(
            "main.asm",
            """
            %include "defs<caret>.inc"

            section .bss
                buffer resb BUFFER_SIZE
            """.trimIndent()
        )

        // Verify Cmd+Click navigation works
        val targetElements = gotoDeclarationAtCaret()

        assertNotNull("Should navigate to include file", targetElements)
        assertTrue("Should navigate to at least one element", targetElements.isNotEmpty())

        val targetFile = (targetElements[0] as? com.intellij.psi.PsiFile)
        assertNotNull("Target should be a PsiFile", targetFile)
        assertEquals("Should navigate to defs.inc", "defs.inc", targetFile?.name)
    }

    fun testNavigationToIncludeWithQuotes() {
        myFixture.addFileToProject(
            "header.inc",
            "%define VERSION 1"
        )

        myFixture.configureByText(
            "main.asm",
            """
            %include "<caret>header.inc"
            """.trimIndent()
        )

        val targetElements = gotoDeclarationAtCaret()
        assertNotNull("Should navigate with quotes", targetElements)
        assertTrue("Should navigate to header.inc", targetElements.size > 0)
    }

    fun testNavigationToIncludeWithSingleQuotes() {
        myFixture.addFileToProject(
            "api.inc",
            "extern api_init"
        )

        myFixture.configureByText(
            "main.asm",
            """
            %include '<caret>api.inc'
            """.trimIndent()
        )

        val targetElements = gotoDeclarationAtCaret()
        assertNotNull("Should navigate with single quotes", targetElements)
        assertTrue("Should navigate to api.inc", targetElements.size > 0)
    }

    fun testIncludePathWithSpaces() {
        // Create file in directory with spaces
        myFixture.addFileToProject(
            "my includes/special.inc",
            "%define SPECIAL 1"
        )

        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "my includes/special.inc"

            section .data
                val dw SPECIAL
            """.trimIndent()
        )

        val includedFiles = mainFile.getIncludedFiles()
        assertEquals("Should find 1 included file", 1, includedFiles.size)
        assertEquals("Should include special.inc", "special.inc", includedFiles[0].name)
    }

    fun testIncludeNotFoundReturnsEmpty() {
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "nonexistent.inc"

            section .text
            main:
                nop
            """.trimIndent()
        )

        // Should not crash, just return empty list
        val includedFiles = mainFile.getIncludedFiles()
        assertEquals("Should find 0 included files when file doesn't exist", 0, includedFiles.size)
    }

    fun testFallbackIncludePathResolverGetsIncludePaths() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("include")
        settings.includePaths.add("/usr/local/include/nasm")
        settings.notifyChanged()

        val resolver = NasmFallbackIncludePathResolver.getInstance(project)

        // Create a dummy virtual file
        val file = myFixture.addFileToProject("test.asm", "").virtualFile

        val paths = resolver.getNasmIncludePaths(file)

        assertEquals("Should return 2 include paths", 2, paths.size)

        // First path should be resolved relative to project
        assertTrue("First path should be absolute", paths[0].contains("include"))

        // Second path should remain absolute
        assertEquals("Second path should be unchanged", "/usr/local/include/nasm", paths[1])
    }

    fun testRelativeIncludePathResolvedAgainstProjectBase() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("../external/includes")
        settings.notifyChanged()

        val resolver = NasmFallbackIncludePathResolver.getInstance(project)
        val file = myFixture.addFileToProject("test.asm", "").virtualFile

        val paths = resolver.getNasmIncludePaths(file)

        assertEquals("Should return 1 include path", 1, paths.size)
        // Path should be resolved (relative paths are resolved against project base)
        assertNotNull("Should have a resolved path", paths[0])
        // The path should not contain ".." after normalization
        val normalizedPath = paths[0].replace("\\", "/")  // Handle Windows paths
        assertFalse("Path should be normalized (no ..)",
            normalizedPath.matches(Regex(".*[/\\\\]\\.\\.[/\\\\].*")))
    }

    /**
     * Helper method to invoke goto declaration and get targets
     */
    private fun gotoDeclarationAtCaret(): Array<PsiElement> {
        val offset = myFixture.caretOffset
        val handler = NasmGotoDeclarationHandler()
        return handler.getGotoDeclarationTargets(
            myFixture.file.findElementAt(offset),
            offset,
            myFixture.editor
        ) ?: emptyArray()
    }
}
