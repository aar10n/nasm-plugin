package dev.agb.nasmplugin.parser

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmLabelDef
import dev.agb.nasmplugin.psi.findLabels

class DataLabelRenameTest : BasePlatformTestCase() {

    fun testDataLabelsAreParsedAsLabels() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .data
                msg db "Hello", 0
                count dd 42
                buffer times 100 db 0
            """.trimIndent()
        )

        val labels = file.findLabels()
        assertEquals(3, labels.size)

        val labelNames = labels.mapNotNull { it.name }.sorted()
        assertEquals(listOf("buffer", "count", "msg"), labelNames)

        // Check that they are NasmLabelDef instances
        labels.forEach { label ->
            assertTrue(label is NasmLabelDef)
            assertNotNull(label.nameIdentifier)
        }
    }

    fun testDataLabelRename() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .data
                old_name db "test", 0

            section .text
                mov rsi, old_name
            """.trimIndent()
        )

        val labels = file.findLabels()
        val oldLabel = labels.find { it.name == "old_name" }
        assertNotNull(oldLabel)

        // Test setName - must be in a write command
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            oldLabel!!.setName("new_name")
        }

        // Check that the name was updated
        assertEquals("new_name", oldLabel!!.name)
    }
}