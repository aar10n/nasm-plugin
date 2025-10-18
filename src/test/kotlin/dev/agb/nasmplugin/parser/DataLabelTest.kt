package dev.agb.nasmplugin.parser

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for data declarations with and without colons
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class DataLabelTest : BasePlatformTestCase() {

    fun testLabelWithColonAndData() {
        val code = """
            section .data
            my_label: db 10
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val labelDefs = PsiTreeUtil.findChildrenOfType(file, NasmLabelDef::class.java)

        assertEquals(1, labelDefs.size)
        assertEquals("my_label", labelDefs.first().name)

        val dataDefs = PsiTreeUtil.findChildrenOfType(file, NasmDataDef::class.java)
        assertEquals(1, dataDefs.size)
    }

    fun testLabelWithoutColonAndData() {
        val code = """
            section .data
            my_label db 10
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val labelDefs = PsiTreeUtil.findChildrenOfType(file, NasmLabelDef::class.java)

        assertEquals(1, labelDefs.size)
        assertEquals("my_label", labelDefs.first().name)

        val dataDefs = PsiTreeUtil.findChildrenOfType(file, NasmDataDef::class.java)
        assertEquals(1, dataDefs.size)
    }

    fun testNewlineCharExample() {
        val code = """
            section .data
            newline_char db 0x0A
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)

        // Should parse as label + data, not as instruction
        val labelDefs = PsiTreeUtil.findChildrenOfType(file, NasmLabelDef::class.java)
        assertEquals(1, labelDefs.size)
        assertEquals("newline_char", labelDefs.first().name)

        // Should not have any instruction elements
        val instructions = PsiTreeUtil.findChildrenOfType(file, NasmInstruction::class.java)
        assertEquals(0, instructions.size)

        // Should have a data definition
        val dataDefs = PsiTreeUtil.findChildrenOfType(file, NasmDataDef::class.java)
        assertEquals(1, dataDefs.size)
    }

    fun testMultipleDataLabels() {
        val code = """
            section .data
            label1: db 10
            label2 db 20
            label3 dw 300
            label4: dd 400
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)

        val labelDefs = PsiTreeUtil.findChildrenOfType(file, NasmLabelDef::class.java)
        assertEquals(4, labelDefs.size)

        val labelNames = labelDefs.map { it.name }.toSet()
        assertEquals(setOf("label1", "label2", "label3", "label4"), labelNames)

        val dataDefs = PsiTreeUtil.findChildrenOfType(file, NasmDataDef::class.java)
        assertEquals(4, dataDefs.size)
    }

    fun testInstructionVsDataAmbiguity() {
        val code = """
            section .text
            mov rax, rbx    ; This is an instruction

            section .data
            mov db 10       ; This is a label 'mov' with data
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)

        // Should have one instruction (the actual mov)
        val instructions = PsiTreeUtil.findChildrenOfType(file, NasmInstruction::class.java)
        assertEquals(1, instructions.size)

        // Should have one label named 'mov' (yes, this is valid NASM!)
        val labelDefs = PsiTreeUtil.findChildrenOfType(file, NasmLabelDef::class.java)
        assertEquals(1, labelDefs.size)
        assertEquals("mov", labelDefs.first().name)

        // Should have one data definition
        val dataDefs = PsiTreeUtil.findChildrenOfType(file, NasmDataDef::class.java)
        assertEquals(1, dataDefs.size)
    }
}