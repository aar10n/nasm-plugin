package dev.agb.nasmplugin.references

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for general Find Usages functionality in NASM files.
 * Tests finding usages of labels, macros, and constants within NASM code.
 *
 * Note: Cross-language C++/NASM find usages is tested in CLion integration tests.
 */
class NasmFindUsagesTest : BasePlatformTestCase() {

    fun testFindUsagesOfLabel() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            helper:
                ret

            main:
                call helper
                jmp helper
                ret
            """.trimIndent()
        )

        // Find the helper label definition
        val labels = file.findLabels()
        val helperLabel = labels.firstOrNull { it.name == "helper" }
        assertNotNull("Should find helper label", helperLabel)

        // Find all references to helper
        val references = ReferencesSearch.search(helperLabel!!).toList()

        // Should find 2 usages: call helper, jmp helper
        assertTrue("Should have at least one usage", references.isNotEmpty())
    }

    fun testFindUsagesOfPrivateLabel() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                call .init
                call .init
                ret

            .init:
                nop
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val initLabel = labels.firstOrNull { it.name == ".init" }
        assertNotNull("Should find .init label", initLabel)

        // Private label should have usages within its scope
        val references = ReferencesSearch.search(initLabel!!).toList()
        assertTrue("Should find usages of .init", references.isNotEmpty())
    }

    fun testFindUsagesOfMacro() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro SAVE_REGS 0
                push rax
                push rbx
            %endmacro

            section .text
            func1:
                SAVE_REGS
                ret

            func2:
                SAVE_REGS
                ret
            """.trimIndent()
        )

        val macros = file.findMultiLineMacros()
        val saveRegsMacro = macros.firstOrNull { it.name == "SAVE_REGS" }
        assertNotNull("Should find SAVE_REGS macro", saveRegsMacro)

        // Macro should have 2 invocations
        val references = ReferencesSearch.search(saveRegsMacro!!).toList()
        assertEquals("Should have 2 macro invocations", 2, references.size)
    }

    fun testFindUsagesOfEquConstant() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            BUFFER_SIZE equ 1024

            section .bss
                buffer1 resb BUFFER_SIZE
                buffer2 resb BUFFER_SIZE

            section .text
            main:
                mov rax, BUFFER_SIZE
                ret
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val bufferSize = constants.firstOrNull { it.name == "BUFFER_SIZE" }
        assertNotNull("Should find BUFFER_SIZE constant", bufferSize)

        // Constant should have 3 usages
        val references = ReferencesSearch.search(bufferSize!!).toList()
        assertEquals("Should have 3 usages of BUFFER_SIZE", 3, references.size)
    }

    fun testFindUsagesAcrossFiles() {
        // Create library file with exported label
        val libFile = myFixture.addFileToProject(
            "lib.asm",
            """
            global utility_func

            section .text
            utility_func:
                ret
            """.trimIndent()
        )

        // Create user file
        myFixture.configureByText(
            "main.asm",
            """
            extern utility_func

            section .text
            main:
                call utility_func
                jmp utility_func
                ret
            """.trimIndent()
        )

        val labels = libFile.findLabels()
        val utilityFunc = labels.firstOrNull { it.name == "utility_func" }
        assertNotNull("Should find utility_func", utilityFunc)

        // Should find usages across files
        val references = ReferencesSearch.search(utilityFunc!!).toList()
        assertTrue("Should find usages in other files", references.isNotEmpty())
    }

    fun testFindUsagesProviderCanFindUsagesForNamedElements() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            section .text
            main:
                mov rax, CONSTANT
                ret
            """.trimIndent()
        )

        val provider = dev.agb.nasmplugin.NasmFindUsagesProvider()

        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should have constant", constant)

        assertTrue("Provider should support finding usages for named elements",
            provider.canFindUsagesFor(constant!!))
    }

    fun testFindUsagesProviderGetType() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            %macro TEST 0
                nop
            %endmacro

            section .text
            main:
                ret
            """.trimIndent()
        )

        val provider = dev.agb.nasmplugin.NasmFindUsagesProvider()

        // Test label type
        val labels = file.findLabels()
        val mainLabel = labels.firstOrNull { it.name == "main" }
        assertEquals("Label type should be 'label'", "label", provider.getType(mainLabel!!))

        // Test constant type
        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertEquals("EQU type should be 'equ constant'", "equ constant", provider.getType(constant!!))

        // Test macro type
        val macros = file.findMultiLineMacros()
        val macro = macros.firstOrNull()
        assertEquals("Macro type should be 'macro'", "macro", provider.getType(macro!!))
    }

    fun testFindUsagesProviderGetDescriptiveName() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            my_function:
                ret
            """.trimIndent()
        )

        val provider = dev.agb.nasmplugin.NasmFindUsagesProvider()

        val labels = file.findLabels()
        val myFunction = labels.firstOrNull()
        assertNotNull("Should have label", myFunction)

        assertEquals("Descriptive name should be label name",
            "my_function", provider.getDescriptiveName(myFunction!!))
    }

    fun testNoUsagesForUnreferencedLabel() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            unused:
                ret

            main:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val unusedLabel = labels.firstOrNull { it.name == "unused" }
        assertNotNull("Should find unused label", unusedLabel)

        val references = ReferencesSearch.search(unusedLabel!!).toList()
        assertEquals("Unused label should have 0 references", 0, references.size)
    }

    fun testFindMultipleUsagesInSameInstruction() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            OFFSET equ 16

            section .text
            main:
                lea rax, [rbx + OFFSET]
                mov rcx, OFFSET
                ret
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val offset = constants.firstOrNull()
        assertNotNull("Should find OFFSET constant", offset)

        val references = ReferencesSearch.search(offset!!).toList()
        assertEquals("Should find 2 usages of OFFSET", 2, references.size)
    }

    fun testFindUsagesOfLabelInConditional() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                cmp rax, rbx
                je equal
                jne not_equal
                ret

            equal:
                mov rax, 1
                ret

            not_equal:
                mov rax, 0
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val equalLabel = labels.firstOrNull { it.name == "equal" }
        assertNotNull("Should find equal label", equalLabel)

        val references = ReferencesSearch.search(equalLabel!!).toList()
        assertEquals("Should find 1 usage (je equal)", 1, references.size)
    }

    fun testFindUsagesOfMacroWithParameters() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro PUSH_ALL 0
                push rax
                push rbx
                push rcx
            %endmacro

            section .text
            func:
                PUSH_ALL
                ; function body
                ret
            """.trimIndent()
        )

        val macros = file.findMultiLineMacros()
        val pushAll = macros.firstOrNull()
        assertNotNull("Should find PUSH_ALL macro", pushAll)

        val references = ReferencesSearch.search(pushAll!!).toList()
        assertEquals("Should find 1 macro invocation", 1, references.size)
    }

    fun testFindUsagesShowsCorrectContext() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            helper:
                ret

            main:
                call helper
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val helperLabel = labels.firstOrNull { it.name == "helper" }
        assertNotNull("Should find helper label", helperLabel)

        val references = ReferencesSearch.search(helperLabel!!).toList()

        // Verify reference exists
        assertTrue("Should have at least 1 reference", references.isNotEmpty())

        // Verify reference points to correct location
        val ref = references.first()
        assertNotNull("Reference element should not be null", ref.element)
    }

    fun testFindUsagesOfGlobalSymbol() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            global my_func

            section .text
            my_func:
                call internal_helper
                ret

            internal_helper:
                ret
            """.trimIndent()
        )

        // Find usages of internal_helper (non-global)
        val labels = file.findLabels()
        val internalHelper = labels.firstOrNull { it.name == "internal_helper" }
        assertNotNull("Should find internal_helper", internalHelper)

        val references = ReferencesSearch.search(internalHelper!!).toList()
        assertEquals("Should find 1 usage of internal_helper", 1, references.size)
    }

    fun testFindUsagesInMacroContext() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            %macro USE_CONSTANT 0
                mov rax, CONSTANT
            %endmacro

            section .text
            main:
                USE_CONSTANT
                ret
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should find CONSTANT", constant)

        // The constant is used inside the macro
        val references = ReferencesSearch.search(constant!!).toList()
        assertEquals("Should find 1 usage in macro", 1, references.size)
    }

    fun testFindUsagesWithCaseVariations() {
        // NASM is case-insensitive for instructions but case-sensitive for labels
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            MyFunction:
                ret

            main:
                call MyFunction
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val myFunction = labels.firstOrNull { it.name == "MyFunction" }
        assertNotNull("Should find MyFunction", myFunction)

        val references = ReferencesSearch.search(myFunction!!).toList()
        assertEquals("Should find 1 usage with exact case match", 1, references.size)
    }

    fun testFindUsagesOfEquInDataDeclaration() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            SIZE equ 100

            section .data
                array times SIZE db 0

            section .bss
                buffer resb SIZE
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val size = constants.firstOrNull()
        assertNotNull("Should find SIZE constant", size)

        val references = ReferencesSearch.search(size!!).toList()
        assertEquals("Should find 2 usages in data declarations", 2, references.size)
    }

    fun testFindUsagesRespectsScope() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                call .local1
                ret

            .local1:
                nop
                ret

            other_func:
                call .local2
                ret

            .local2:
                nop
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()

        // .local1 should only be used in main
        val local1 = labels.firstOrNull { it.name == ".local1" }
        assertNotNull("Should find .local1", local1)

        val refs1 = ReferencesSearch.search(local1!!).toList()
        assertEquals("Should find 1 usage of .local1", 1, refs1.size)

        // .local2 should only be used in other_func
        val local2 = labels.firstOrNull { it.name == ".local2" }
        assertNotNull("Should find .local2", local2)

        val refs2 = ReferencesSearch.search(local2!!).toList()
        assertEquals("Should find 1 usage of .local2", 1, refs2.size)
    }
}
