package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmInstruction
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Tests for NasmAnnotator - instruction validation, semantic highlighting, and error reporting.
 */
class NasmAnnotatorTest : BasePlatformTestCase() {

    fun testValidInstructionIsHighlighted() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, rbx
            """.trimIndent()
        )

        // Verify no errors
        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Valid instruction should not produce errors", errors.isEmpty())

        // Verify instruction is recognized (should have syntax highlighting)
        val infos = highlights.filter { it.severity == HighlightSeverity.INFORMATION }
        assertTrue("Valid instruction should be highlighted", infos.isNotEmpty())
    }

    fun testMultipleValidInstructions() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, rbx
                add rcx, rdx
                push rsi
                pop rdi
                ret
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Valid instructions should not produce errors", errors.isEmpty())
    }

    fun testInvalidInstructionProducesError() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                invalid_instruction rax, rbx
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        assertTrue("Invalid instruction should produce error", errors.isNotEmpty())
        val errorText = errors.first().description
        assertTrue("Error should mention unknown instruction",
            errorText.contains("unknown", ignoreCase = true) ||
            errorText.contains("invalid_instruction"))
    }

    fun testMacroInvocationIsHighlighted() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 0
                    nop
                %endmacro

                section .text
                my_macro
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()

        // Should not produce errors (macro is valid)
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Macro invocation should not produce errors", errors.isEmpty())

        // Macro should be highlighted with special highlighting
        val infos = highlights.filter { it.severity == HighlightSeverity.INFORMATION }
        assertTrue("Macro invocation should be highlighted", infos.isNotEmpty())
    }

    fun testMacroDefinitionIsHighlighted() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Macro definition should not produce errors", errors.isEmpty())

        // Macro name should be highlighted
        val infos = highlights.filter { it.severity == HighlightSeverity.INFORMATION }
        assertTrue("Macro definition name should be highlighted", infos.isNotEmpty())
    }

    fun testSingleLineMacroDefinitionIsHighlighted() {
        myFixture.configureByText(
            "test.asm",
            """
                %define MY_CONST 42

                section .text
                mov rax, MY_CONST
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Single-line macro should not produce errors", errors.isEmpty())
    }

    fun testEquConstantAsStatementProducesError() {
        myFixture.configureByText(
            "test.asm",
            """
                MY_CONST equ 42

                section .text
                MY_CONST
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        // Using EQU constant as a statement should produce an error
        // The parser treats this as an instruction, which will be flagged as unknown
        assertTrue("EQU constant used as statement should produce error", errors.isNotEmpty())

        val errorText = errors.first().description
        // Error should mention that it's unknown (could be improved to mention it's an EQU constant)
        assertTrue("Error should mention unknown instruction or invalid usage",
            errorText.contains("unknown", ignoreCase = true) ||
            errorText.contains("MY_CONST"))
    }

    fun testLabelAsStatementProducesError() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                my_label:
                    nop

                my_label
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        // Using label as a statement should produce an error
        assertTrue("Label used as statement should produce error", errors.isNotEmpty())
    }

    fun testMacroInvocationWithArguments() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro move_reg 2
                    mov %1, %2
                %endmacro

                section .text
                move_reg rax, rbx
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Macro invocation with arguments should not produce errors", errors.isEmpty())
    }

    fun testMixedValidAndInvalidInstructions() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, rbx
                invalid_inst
                add rcx, rdx
                another_invalid
                ret
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        // Should have exactly 2 errors (for the two invalid instructions)
        assertEquals("Should have 2 errors for invalid instructions", 2, errors.size)
    }

    fun testSymbolReferenceResolvingToMacroIsHighlighted() {
        myFixture.configureByText(
            "test.asm",
            """
                %define MY_VALUE 100

                section .text
                mov rax, MY_VALUE
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Symbol reference to macro should not produce errors", errors.isEmpty())
    }

    fun testComplexMacroScenario() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro PUSH_ALL 0
                    push rax
                    push rbx
                    push rcx
                %endmacro

                %macro POP_ALL 0
                    pop rcx
                    pop rbx
                    pop rax
                %endmacro

                section .text
                main:
                    PUSH_ALL
                    call some_function
                    POP_ALL
                    ret
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        // "some_function" will be an error since it's not defined, but macros should be fine
        // Filter to only check for macro-related errors
        val macroErrors = errors.filter {
            it.description.contains("PUSH_ALL") || it.description.contains("POP_ALL")
        }
        assertTrue("Macro invocations should not produce errors", macroErrors.isEmpty())
    }

    fun testSectionDirectiveDoesNotProduceError() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                section .data
                section .bss
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Section directives should not produce errors", errors.isEmpty())
    }

    fun testGlobalAndExternDirectives() {
        myFixture.configureByText(
            "test.asm",
            """
                global _start
                extern printf

                section .text
                _start:
                    call printf
                    ret
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }

        // Global and extern should not produce errors
        // "printf" is extern so calling it should be fine
        assertTrue("Global/extern directives should not produce errors", errors.isEmpty())
    }

    fun testDataDeclarations() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                my_string db 'Hello, World!', 0
                my_number dq 42
                my_array times 10 dw 0
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Data declarations should not produce errors", errors.isEmpty())
    }

    fun testLocalLabels() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                func1:
                    mov rax, 1
                    jmp .done
                .done:
                    ret

                func2:
                    mov rax, 2
                    jmp .done
                .done:
                    ret
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Local labels should not produce errors", errors.isEmpty())
    }

    fun testPreprocessorDirectives() {
        myFixture.configureByText(
            "test.asm",
            """
                %define SIZE 100
                %assign COUNTER 0
                %ifdef SIZE
                    mov rax, SIZE
                %endif
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Preprocessor directives should not produce errors", errors.isEmpty())
    }

    fun testMacroReferenceInMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %define ONE 1
                %define TWO (ONE + ONE)
                %define THREE (ONE + TWO)
                %define FOUR TWO * TWO
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Macro references in macro definitions should not produce errors", errors.isEmpty())

        // Check that macro references within definitions are highlighted
        val infos = highlights.filter { it.severity == HighlightSeverity.INFORMATION }
        assertTrue("Macro references in definitions should be highlighted", infos.isNotEmpty())

        // Verify that "ONE" in TWO's definition is highlighted
        val oneHighlights = infos.filter { it.text == "ONE" }
        assertTrue("Macro reference 'ONE' should be highlighted in other macro definitions",
            oneHighlights.size >= 2) // At least in TWO and THREE definitions
    }
}
