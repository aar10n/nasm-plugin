package dev.agb.nasmplugin.inspections

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for NasmUnusedSymbolInspection.
 */
class NasmUnusedSymbolInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(NasmUnusedSymbolInspection::class.java)
    }

    fun testUnusedLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <warning descr="Label 'unused_label' is never used">unused_label</warning>:
                    mov rax, rbx
                    ret

                main:
                    call main
                    ret
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testUnusedMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro <warning descr="Macro 'unused_macro' is never used">unused_macro</warning> 0
                    mov rax, rbx
                %endmacro

                %macro used_macro 0
                    mov rcx, rdx
                %endmacro

                section .text
                main:
                    used_macro
                    ret
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testUnusedEquConstant() {
        myFixture.configureByText(
            "test.asm",
            """
                <warning descr="Constant 'UNUSED_CONST' is never used">UNUSED_CONST</warning> equ 42
                USED_CONST equ 100

                section .text
                main:
                    mov rax, USED_CONST
                    ret
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testAssignInRepBlockNotUnused() {
        // This is the key test - %assign inside %rep should NOT be marked as unused
        myFixture.configureByText(
            "test.asm",
            """
                ; Loop counter pattern
                %assign i 0
                %rep 10
                    db i
                    %assign i i+1
                %endrep

                ; Another pattern
                %assign counter 0
                %rep 5
                    dw counter
                    %assign counter counter+1
                %endrep
            """.trimIndent()
        )

        // Should have no warnings about unused %assign statements
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("is never used") == true }
        assertTrue("No warnings expected for %assign inside %rep blocks", warnings.isEmpty())
    }

    fun testAssignOutsideRepBlockCanBeUnused() {
        myFixture.configureByText(
            "test.asm",
            """
                ; This %assign is outside %rep and truly unused
                %assign <warning descr="Variable 'unused_var' is never used">unused_var</warning> 42

                ; This one is used
                %assign used_var 100

                section .data
                    db used_var
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testGlobalSymbolNotUnused() {
        myFixture.configureByText(
            "test.asm",
            """
                global exported_function

                section .text
                exported_function:
                    mov rax, rbx
                    ret
            """.trimIndent()
        )

        // Should have no warnings - global symbols are not marked as unused
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("is never used") == true }
        assertTrue("No warnings expected for global symbols", warnings.isEmpty())
    }

    fun testEntryPointsNotUnused() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                _start:
                    ret

                main:
                    ret

                _main:
                    ret
            """.trimIndent()
        )

        // Should have no warnings - entry points are not marked as unused
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("is never used") == true }
        assertTrue("No warnings expected for entry points", warnings.isEmpty())
    }

    fun testLocalLabelNotUnused() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                .local_label:
                    mov rax, rbx
                    jmp .local_label
                    ret
            """.trimIndent()
        )

        // Should have no warnings - local labels starting with . are skipped
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("is never used") == true }
        assertTrue("No warnings expected for local labels", warnings.isEmpty())
    }
}
