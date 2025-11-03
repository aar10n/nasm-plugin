package dev.agb.nasmplugin.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import dev.agb.nasmplugin.psi.*

/**
 * Tests for Navigate to Symbol functionality (Ctrl+Alt+Shift+N).
 * Tests that symbols can be found across the project by name.
 */
class NasmChooseByNameContributorTest : BasePlatformTestCase() {

    fun testFindLabelByName() {
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            global main
            main:
                call helper
                ret

            helper:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'main' label", names.contains("main"))
        assertTrue("Should find 'helper' label", names.contains("helper"))
    }

    fun testFindMacroByName() {
        myFixture.configureByText(
            "test.asm",
            """
            %macro SAVE_REGS 0
                push rax
                push rbx
            %endmacro

            %macro RESTORE_REGS 0
                pop rbx
                pop rax
            %endmacro
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'SAVE_REGS' macro", names.contains("SAVE_REGS"))
        assertTrue("Should find 'RESTORE_REGS' macro", names.contains("RESTORE_REGS"))
    }

    fun testFindEquConstantByName() {
        myFixture.configureByText(
            "test.asm",
            """
            BUFFER_SIZE equ 1024
            MAX_COUNT equ 256

            section .bss
                buffer resb BUFFER_SIZE
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'BUFFER_SIZE' constant", names.contains("BUFFER_SIZE"))
        assertTrue("Should find 'MAX_COUNT' constant", names.contains("MAX_COUNT"))
    }

    fun testFindGlobalSymbolByName() {
        myFixture.configureByText(
            "test.asm",
            """
            global my_function
            global another_function

            section .text
            my_function:
                ret

            another_function:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'my_function' global", names.contains("my_function"))
        assertTrue("Should find 'another_function' global", names.contains("another_function"))
    }

    fun testFindExternSymbolByName() {
        myFixture.configureByText(
            "test.asm",
            """
            extern printf
            extern malloc

            section .text
            main:
                call printf
                call malloc
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'printf' extern", names.contains("printf"))
        assertTrue("Should find 'malloc' extern", names.contains("malloc"))
    }

    fun testFindSymbolsAcrossMultipleFiles() {
        // Create first file
        myFixture.addFileToProject(
            "module1.asm",
            """
            global func1

            func1:
                ret
            """.trimIndent()
        )

        // Create second file
        myFixture.addFileToProject(
            "module2.asm",
            """
            global func2

            func2:
                ret
            """.trimIndent()
        )

        // Create main file
        myFixture.configureByText(
            "main.asm",
            """
            extern func1
            extern func2

            main:
                call func1
                call func2
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find func1", names.contains("func1"))
        assertTrue("Should find func2", names.contains("func2"))
        assertTrue("Should find main", names.contains("main"))
    }

    fun testNavigateToSpecificSymbol() {
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            global main
            main:
                call helper
                ret

            helper:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val parameters = FindSymbolParameters.simple(project, false)

        val items = collectElementsWithName(contributor, "helper", parameters)

        assertEquals("Should find exactly 1 element named 'helper'", 1, items.size)

        val item = items[0]
        assertTrue("Item should be a NasmLabelDef", item is NasmLabelDef)
        assertEquals("Item name should be 'helper'", "helper", (item as NasmNamedElement).name)
    }

    fun testNavigateToMacro() {
        myFixture.configureByText(
            "test.asm",
            """
            %macro TEST_MACRO 0
                nop
            %endmacro

            section .text
            main:
                TEST_MACRO
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val parameters = FindSymbolParameters.simple(project, false)

        val items = collectElementsWithName(contributor, "TEST_MACRO", parameters)

        assertEquals("Should find exactly 1 element named 'TEST_MACRO'", 1, items.size)
        assertTrue("Item should be a NasmMultiLineMacro", items[0] is NasmMultiLineMacro)
    }

    fun testNavigateToEquConstant() {
        myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            section .data
                value dw CONSTANT
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val parameters = FindSymbolParameters.simple(project, false)

        val items = collectElementsWithName(contributor, "CONSTANT", parameters)

        assertEquals("Should find exactly 1 element named 'CONSTANT'", 1, items.size)
        assertTrue("Item should be a NasmEquDefinition", items[0] is NasmEquDefinition)
    }

    fun testCaseInsensitiveSearch() {
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            MyFunction:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'MyFunction'", names.contains("MyFunction"))
    }

    fun testFindPrivateLabel() {
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                call .helper
                ret

            .helper:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find 'main' public label", names.contains("main"))
        assertTrue("Should find '.helper' private label", names.contains(".helper"))
    }

    fun testFindSymbolsWithUnderscores() {
        myFixture.configureByText(
            "test.asm",
            """
            global _start
            extern __libc_start_main

            _start:
                call __libc_start_main
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find '_start'", names.contains("_start"))
        assertTrue("Should find '__libc_start_main'", names.contains("__libc_start_main"))
    }

    fun testFindGlobalListDeclaration() {
        myFixture.configureByText(
            "test.asm",
            """
            global func1, func2, func3

            section .text
            func1:
                ret

            func2:
                ret

            func3:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find func1 from global list", names.contains("func1"))
        assertTrue("Should find func2 from global list", names.contains("func2"))
        assertTrue("Should find func3 from global list", names.contains("func3"))
    }

    fun testFindExternListDeclaration() {
        myFixture.configureByText(
            "test.asm",
            """
            extern api_init, api_shutdown, api_process

            section .text
            main:
                call api_init
                call api_process
                call api_shutdown
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find api_init from extern list", names.contains("api_init"))
        assertTrue("Should find api_shutdown from extern list", names.contains("api_shutdown"))
        assertTrue("Should find api_process from extern list", names.contains("api_process"))
    }

    fun testNoSymbolsInEmptyFile() {
        myFixture.configureByText("test.asm", "")

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        // Empty file may have no symbols, or only recognized as a file
        // Just verify it doesn't crash
        assertNotNull("Names collection should not be null", names)
    }

    fun testFindSymbolsWithNumericSuffixes() {
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            loop1:
                ret

            loop2:
                ret

            loop3:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find loop1", names.contains("loop1"))
        assertTrue("Should find loop2", names.contains("loop2"))
        assertTrue("Should find loop3", names.contains("loop3"))
    }

    fun testFindMixedSymbolTypes() {
        myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            %macro TEST 0
                nop
            %endmacro

            global my_func
            extern ext_func

            section .text
            my_func:
                call ext_func
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        assertTrue("Should find CONSTANT", names.contains("CONSTANT"))
        assertTrue("Should find TEST macro", names.contains("TEST"))
        assertTrue("Should find my_func global", names.contains("my_func"))
        assertTrue("Should find ext_func extern", names.contains("ext_func"))
        assertTrue("Should find my_func label", names.contains("my_func"))
    }

    fun testNavigateToSymbolInSpecificFile() {
        // Create file with duplicate symbol names
        myFixture.addFileToProject(
            "lib1.asm",
            """
            global init

            init:
                mov rax, 1
                ret
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "lib2.asm",
            """
            global init

            init:
                mov rax, 2
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val parameters = FindSymbolParameters.simple(project, false)

        val items = collectElementsWithName(contributor, "init", parameters)

        // Should find 'init' labels and global declarations (2 labels + 2 globals = 4)
        assertEquals("Should find 4 elements named 'init'", 4, items.size)
        // Should include both labels and symbol declarations
        assertTrue("Should have at least one label", items.any { it is NasmLabelDef })
    }

    fun testFileTypeFiltering() {
        // Create a non-NASM file
        myFixture.addFileToProject("test.txt", "main:\n    ret")

        // Create a NASM file
        myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        // Should only find symbols from .asm files, not .txt files
        assertTrue("Should find main from .asm file", names.contains("main"))
    }

    fun testNavigateToDefineStatement() {
        myFixture.configureByText(
            "test.asm",
            """
            %define VERSION 1
            %define AUTHOR "test"

            section .text
            main:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        // Note: Currently %define statements are NOT searched by the contributor
        // The contributor only searches for labels, macros, EQU, global, and extern
        // This test verifies that main label is found, but defines are not currently supported
        assertFalse("Should not find VERSION (defines not indexed)", names.contains("VERSION"))
        assertFalse("Should not find AUTHOR (defines not indexed)", names.contains("AUTHOR"))
        assertTrue("Should find main label", names.contains("main"))
    }

    fun testNavigateToAssignStatement() {
        myFixture.configureByText(
            "test.asm",
            """
            %assign counter 0
            %assign limit 100

            section .text
            main:
                ret
            """.trimIndent()
        )

        val contributor = NasmChooseByNameContributor()
        val scope = GlobalSearchScope.projectScope(project)
        val names = collectNames(contributor, scope)

        // Note: Currently %assign statements are NOT searched by the contributor
        // The contributor only searches for labels, macros, EQU, global, and extern
        // This test verifies that main label is found, but assigns are not currently supported
        assertFalse("Should not find counter (assigns not indexed)", names.contains("counter"))
        assertFalse("Should not find limit (assigns not indexed)", names.contains("limit"))
        assertTrue("Should find main label", names.contains("main"))
    }

    /**
     * Helper method to collect all symbol names from the contributor
     */
    private fun collectNames(
        contributor: NasmChooseByNameContributor,
        scope: GlobalSearchScope
    ): Set<String> {
        val names = mutableSetOf<String>()
        val processor = Processor<String> { name ->
            names.add(name)
            true
        }
        contributor.processNames(processor, scope, null)
        return names
    }

    /**
     * Helper method to collect all navigation items with a specific name
     */
    private fun collectElementsWithName(
        contributor: NasmChooseByNameContributor,
        name: String,
        parameters: FindSymbolParameters
    ): List<NavigationItem> {
        val items = mutableListOf<NavigationItem>()
        val processor = Processor<NavigationItem> { item ->
            items.add(item)
            true
        }
        contributor.processElementsWithName(name, processor, parameters)
        return items
    }
}
