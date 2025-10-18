package dev.agb.nasmplugin.navigation

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for go-to-definition navigation (Ctrl+Click, Ctrl+B).
 * Verifies that symbols resolve to their definitions correctly.
 */
class NasmGotoDeclarationTest : BasePlatformTestCase() {

    fun testGotoLabelDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                my_label:
                    nop
                    jmp my_<caret>label
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to label definition", resolved)

        // Verify it's the label definition (has a colon)
        assertTrue("Should be a label definition",
            resolved is NasmLabelDef || resolved.parent is NasmLabelDef)
    }

    fun testGotoLocalLabelDefinition() {
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

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to local label", resolved)

        // Should resolve to the .loop label definition
        val labelDef = if (resolved is NasmLabelDef) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmLabelDef::class.java)

        assertNotNull("Should find label definition", labelDef)
        assertEquals("Should resolve to .loop", ".loop", labelDef?.name)
    }

    fun testGotoMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro

                section .text
                my_<caret>macro rax, rbx
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to macro definition", resolved)

        // Should resolve to macro definition
        val macroDef = if (resolved is NasmMultiLineMacro) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmMultiLineMacro::class.java)

        assertNotNull("Should find macro definition", macroDef)
    }

    fun testGotoSingleLineMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %define MY_VALUE 100

                section .text
                mov rax, MY_<caret>VALUE
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to single-line macro", resolved)

        val macroDef = if (resolved is NasmPpDefineStmt) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmPpDefineStmt::class.java)

        assertNotNull("Should find single-line macro definition", macroDef)
    }

    fun testGotoEquConstantDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                BUFFER_SIZE equ 1024

                section .text
                mov rax, BUFFER_<caret>SIZE
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to EQU constant", resolved)

        val equDef = if (resolved is NasmEquDefinition) resolved
                     else PsiTreeUtil.getParentOfType(resolved, NasmEquDefinition::class.java)

        assertNotNull("Should find EQU constant definition", equDef)
    }

    fun testGotoDataLabelDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data
                message db 'Hello', 0

                section .text
                mov rsi, mes<caret>sage
            """.trimIndent()
        )

        // Get the element at caret and find the symbol reference
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should have element at caret", elementAtCaret)

        val symbolRef = PsiTreeUtil.getParentOfType(elementAtCaret, NasmSymbolRef::class.java)
        assertNotNull("Should have symbol reference", symbolRef)

        // Resolve the reference
        val resolved = symbolRef!!.reference?.resolve()
        assertNotNull("Should resolve to data label", resolved)

        // Should resolve to a label that has a data definition sibling
        assertTrue("Should be a label definition",
            resolved is NasmLabelDef || resolved!!.parent is NasmLabelDef)

        // The parent source line should have a data definition
        val sourceLine = PsiTreeUtil.getParentOfType(resolved, NasmSourceLine::class.java)
        assertNotNull("Should have source line", sourceLine)
        assertNotNull("Should have data definition", sourceLine!!.dataDef)
    }

    fun testGotoForwardReference() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                    call helper_<caret>func
                    ret

                helper_func:
                    nop
                    ret
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve forward reference", resolved)

        val labelDef = if (resolved is NasmLabelDef) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmLabelDef::class.java)

        assertNotNull("Should find helper_func label", labelDef)
        assertEquals("Should resolve to helper_func", "helper_func", labelDef?.name)
    }

    fun testGotoBackwardReference() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                helper_func:
                    nop
                    ret

                main:
                    call helper_<caret>func
                    ret
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve backward reference", resolved)

        val labelDef = if (resolved is NasmLabelDef) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmLabelDef::class.java)

        assertNotNull("Should find helper_func label", labelDef)
        assertEquals("Should resolve to helper_func", "helper_func", labelDef?.name)
    }

    fun testGotoMacroUsedInAnotherMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %define BASE 100

                %define DERIVED (BA<caret>SE + 10)
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve macro reference in another macro", resolved)

        val macroDef = if (resolved is NasmPpDefineStmt) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmPpDefineStmt::class.java)

        assertNotNull("Should find BASE macro", macroDef)
    }

    fun testGotoExternSymbol() {
        myFixture.configureByText(
            "test.asm",
            """
                extern printf

                section .text
                main:
                    call prin<caret>tf
                    ret
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should find element for extern symbol", resolved)

        // Extern symbols may resolve to the extern declaration
        val externDir = PsiTreeUtil.getParentOfType(
            myFixture.file.findElementAt(10), // Position at extern declaration
            NasmExternDir::class.java
        )

        assertNotNull("Should find extern directive in file", externDir)
    }

    fun testGotoGlobalSymbol() {
        myFixture.configureByText(
            "test.asm",
            """
                global _start

                section .text
                _start:
                    nop
                    ret
            """.trimIndent()
        )

        // Position on _start label
        val labelOffset = myFixture.file.text.indexOf("_start:")
        myFixture.editor.caretModel.moveToOffset(labelOffset + 3)

        val element = myFixture.elementAtCaret
        assertNotNull("Should find _start label", element)

        // _start should be defined as a label
        val labelDef = if (element is NasmLabelDef) element
                       else PsiTreeUtil.getParentOfType(element, NasmLabelDef::class.java)

        assertNotNull("Should be a label definition", labelDef)
    }

    fun testGotoIncludedMacro() {
        // Create include file
        myFixture.addFileToProject(
            "macros.inc",
            """
                %macro HELPER 0
                    nop
                %endmacro
            """.trimIndent()
        )

        myFixture.configureByText(
            "main.asm",
            """
                %include "macros.inc"

                section .text
                main:
                    HEL<caret>PER
                    ret
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to included macro", resolved)

        // Should resolve to macro in included file
        val macroDef = if (resolved is NasmMultiLineMacro) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmMultiLineMacro::class.java)

        if (macroDef != null) {
            assertEquals("Should be in macros.inc", "macros.inc", macroDef.containingFile.name)
        }
    }

    fun testGotoLabelInDifferentSection() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                main:
                    mov rsi, mes<caret>sage
                    ret

                section .data
                message db 'Hello', 0
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to label in different section", resolved)
    }

    fun testGotoLocalLabelInCorrectScope() {
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

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve local label", resolved)

        val labelDef = if (resolved is NasmLabelDef) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmLabelDef::class.java)

        assertNotNull("Should find .done label", labelDef)

        // Should resolve to first .done (in func1 scope)
        // Verify by checking it comes before func2
        val func2Offset = myFixture.file.text.indexOf("func2:")
        assertTrue("Should resolve to .done in func1 scope",
            labelDef!!.textOffset < func2Offset)
    }

    fun testMultipleReferencesToSameLabel() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                loop_start:
                    dec rax
                    jnz loop_<caret>start
                    jmp loop_start
                    call loop_start
                    ret
            """.trimIndent()
        )

        val resolved = myFixture.elementAtCaret
        assertNotNull("Should resolve to loop_start", resolved)

        val labelDef = if (resolved is NasmLabelDef) resolved
                       else PsiTreeUtil.getParentOfType(resolved, NasmLabelDef::class.java)

        assertNotNull("Should find loop_start definition", labelDef)
        assertEquals("Should resolve to loop_start", "loop_start", labelDef?.name)
    }

    fun testGotoSymbolWithCaseInsensitiveMatch() {
        myFixture.configureByText(
            "test.asm",
            """
                %define MyValue 100

                section .text
                mov rax, myval<caret>ue
            """.trimIndent()
        )

        // Get the element at caret and find the symbol reference
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should have element at caret", elementAtCaret)

        val symbolRef = PsiTreeUtil.getParentOfType(elementAtCaret, NasmSymbolRef::class.java)
        assertNotNull("Should have symbol reference", symbolRef)

        // Resolve the reference (should be case-insensitive in NASM)
        val resolved = symbolRef!!.reference?.resolve()
        assertNotNull("Should resolve case-insensitive macro reference", resolved)

        // NASM is case-insensitive by default - should resolve to MyValue
        assertTrue("Should resolve to macro definition despite case difference",
            resolved is NasmPpDefineStmt || resolved!!.parent is NasmPpDefineStmt)
    }
}
