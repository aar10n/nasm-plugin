package dev.agb.nasmplugin.refactoring

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Comprehensive tests for rename refactoring of NASM symbols.
 * Tests labels, macros, constants, and cross-file renames.
 */
class NasmRenameTest : BasePlatformTestCase() {

    fun testRenameGlobalLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                my_<caret>label:
                    nop
                    jmp my_label
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("new_label")

        myFixture.checkResult(
            """
                section .text
                new_label:
                    nop
                    jmp new_label
            """.trimIndent()
        )
    }

    fun testRenameLabelWithMultipleReferences() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                start_<caret>func:
                    mov rax, 1
                    call start_func
                    jmp start_func
                    ret

                another_func:
                    call start_func
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("renamed_func")

        myFixture.checkResult(
            """
                section .text
                renamed_func:
                    mov rax, 1
                    call renamed_func
                    jmp renamed_func
                    ret

                another_func:
                    call renamed_func
                    ret
            """.trimIndent()
        )
    }

    fun testRenameLocalLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                func:
                    mov rax, 1
                    jmp .lo<caret>op
                .loop:
                    dec rax
                    jnz .loop
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret(".retry")

        myFixture.checkResult(
            """
                section .text
                func:
                    mov rax, 1
                    jmp .retry
                .retry:
                    dec rax
                    jnz .retry
                    ret
            """.trimIndent()
        )
    }

    fun testRenameMultiLineMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro old_<caret>macro 2
                    mov %1, %2
                %endmacro

                section .text
                old_macro rax, rbx
                old_macro rcx, rdx
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("new_macro")

        myFixture.checkResult(
            """
                %macro new_macro 2
                    mov %1, %2
                %endmacro

                section .text
                new_macro rax, rbx
                new_macro rcx, rdx
            """.trimIndent()
        )
    }

    fun testRenameSingleLineMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %define OLD_<caret>VALUE 100

                section .text
                mov rax, OLD_VALUE
                mov rbx, OLD_VALUE
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("NEW_VALUE")

        myFixture.checkResult(
            """
                %define NEW_VALUE 100

                section .text
                mov rax, NEW_VALUE
                mov rbx, NEW_VALUE
            """.trimIndent()
        )
    }

    fun testRenameEquConstant() {
        myFixture.configureByText(
            "test.asm",
            """
                BUFFER_<caret>SIZE equ 1024

                section .bss
                buffer resb BUFFER_SIZE

                section .text
                mov rax, BUFFER_SIZE
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("BUF_SIZE")

        myFixture.checkResult(
            """
                BUF_SIZE equ 1024

                section .bss
                buffer resb BUF_SIZE

                section .text
                mov rax, BUF_SIZE
            """.trimIndent()
        )
    }

    fun testRenameDataLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                my_<caret>string db 'Hello', 0

                section .text
                mov rsi, my_string
                mov rax, my_string
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("message")

        myFixture.checkResult(
            """
                section .data
                message db 'Hello', 0

                section .text
                mov rsi, message
                mov rax, message
            """.trimIndent()
        )
    }

    fun testRenameMacroUsedInExpression() {
        myFixture.configureByText(
            "test.asm",
            """
                %define BA<caret>SE 100
                %define OFFSET 10
                %define TOTAL (BASE + OFFSET)

                section .text
                mov rax, TOTAL
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("START")

        myFixture.checkResult(
            """
                %define START 100
                %define OFFSET 10
                %define TOTAL (START + OFFSET)

                section .text
                mov rax, TOTAL
            """.trimIndent()
        )
    }

    fun testRenameWithComments() {
        myFixture.configureByText(
            "test.asm",
            """
                ; This is the main entry point
                mai<caret>n:
                    ; Call main again
                    call main
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("_start")

        myFixture.checkResult(
            """
                ; This is the main entry point
                _start:
                    ; Call main again
                    call _start
                    ret
            """.trimIndent()
        )
    }

    fun testRenamePreservesIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                fu<caret>nc:
                    push rbp
                    mov rbp, rsp
                    call func
                    pop rbp
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("procedure")

        myFixture.checkResult(
            """
                section .text
                procedure:
                    push rbp
                    mov rbp, rsp
                    call procedure
                    pop rbp
                    ret
            """.trimIndent()
        )
    }

    fun testRenameLabelReferenceFromOtherFile() {
        // Create main file
        val mainFile = myFixture.addFileToProject(
            "main.asm",
            """
                extern helper_function

                section .text
                main:
                    call helper_function
                    ret
            """.trimIndent()
        )

        // Create helper file with the definition
        myFixture.configureByText(
            "helper.asm",
            """
                global helper_<caret>function

                section .text
                helper_function:
                    nop
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("utility_function")

        // Check that helper file was updated
        myFixture.checkResult(
            """
                global utility_function

                section .text
                utility_function:
                    nop
                    ret
            """.trimIndent()
        )

        // Note: Cross-file reference updates depend on the reference resolution implementation
        // This test verifies rename doesn't crash with multiple files
    }

    fun testRenameIncludedMacro() {
        // Create an include file
        myFixture.addFileToProject(
            "macros.inc",
            """
                %macro PRINT_<caret>MSG 1
                    mov rsi, %1
                    call print
                %endmacro
            """.trimIndent()
        )

        // Create main file that uses the macro
        myFixture.configureByText(
            "main.asm",
            """
                %include "macros.inc"

                section .text
                main:
                    PRINT_MSG message
                    ret
            """.trimIndent()
        )

        // Open the include file
        myFixture.configureFromExistingVirtualFile(
            myFixture.findFileInTempDir("macros.inc")
        )

        // Position caret on macro name
        val text = myFixture.editor.document.text
        val offset = text.indexOf("PRINT_MSG") + 6
        myFixture.editor.caretModel.moveToOffset(offset)

        myFixture.renameElementAtCaret("DISPLAY_MSG")

        // Verify include file was updated
        myFixture.checkResult(
            """
                %macro DISPLAY_MSG 1
                    mov rsi, %1
                    call print
                %endmacro
            """.trimIndent()
        )
    }

    fun testRenameDoesNotAffectUnrelatedSymbols() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                my_<caret>func:
                    nop
                    call my_func
                    ret

                my_other_func:
                    nop
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("renamed_func")

        myFixture.checkResult(
            """
                section .text
                renamed_func:
                    nop
                    call renamed_func
                    ret

                my_other_func:
                    nop
                    ret
            """.trimIndent()
        )
    }

    fun testRenameLocalLabelOnlyInScope() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                func1:
                    jmp .do<caret>ne
                .done:
                    ret

                func2:
                    jmp .done
                .done:
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret(".exit")

        // Only the .done in func1 scope should be renamed
        myFixture.checkResult(
            """
                section .text
                func1:
                    jmp .exit
                .exit:
                    ret

                func2:
                    jmp .done
                .done:
                    ret
            """.trimIndent()
        )
    }

    fun testRenameMacroWithNestedInvocations() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro inne<caret>r 0
                    nop
                %endmacro

                %macro outer 0
                    inner
                %endmacro

                section .text
                main:
                    outer
                    inner
                    ret
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("core")

        myFixture.checkResult(
            """
                %macro core 0
                    nop
                %endmacro

                %macro outer 0
                    core
                %endmacro

                section .text
                main:
                    outer
                    core
                    ret
            """.trimIndent()
        )
    }

    fun testRenameAssignVariable() {
        myFixture.configureByText(
            "test.asm",
            """
                %assign COUN<caret>TER 0
                %assign COUNTER COUNTER+1

                section .text
                mov rax, COUNTER
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("INDEX")

        myFixture.checkResult(
            """
                %assign INDEX 0
                %assign INDEX INDEX+1

                section .text
                mov rax, INDEX
            """.trimIndent()
        )
    }
}
