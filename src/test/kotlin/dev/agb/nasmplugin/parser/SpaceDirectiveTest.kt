package dev.agb.nasmplugin.parser

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmInstruction
import dev.agb.nasmplugin.psi.NasmLabelDef
import dev.agb.nasmplugin.psi.NasmSpaceDef
import dev.agb.nasmplugin.psi.findLabels

class SpaceDirectiveTest : BasePlatformTestCase() {

    fun testLabelsWithSpaceDirectives() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                ; Test that labels before space directives are parsed correctly
                aligned_buffer resb 128
                result resq 1
                data_array resd 100
                small_buffer resw 64

                ; Labels with colons should also work
                colon_buffer: resb 256

                ; Mixed with data directives
                my_data db 10, 20, 30
                my_buffer resb 512
            """.trimIndent()
        )

        // Find all label definitions
        val labels = psiFile.findLabels()
        println("Found ${labels.size} labels:")
        labels.forEach { label ->
            println("  Label: ${label.name}")
        }

        // Verify we found all the expected labels
        val expectedLabels = listOf(
            "aligned_buffer",
            "result",
            "data_array",
            "small_buffer",
            "colon_buffer",
            "my_data",
            "my_buffer"
        )

        expectedLabels.forEach { expectedName ->
            val label = labels.firstOrNull { it.name == expectedName }
            assertNotNull("Label '$expectedName' should be found", label)
        }

        // Verify these are NOT parsed as instructions/mnemonics
        val instructions = PsiTreeUtil.findChildrenOfType(psiFile, NasmInstruction::class.java)
        println("\nFound ${instructions.size} instructions:")
        instructions.forEach { instruction ->
            println("  Instruction: ${instruction.text}")
        }

        // None of our label names should appear as instructions
        instructions.forEach { instruction ->
            val text = instruction.text
            assertFalse(
                "Label should not be parsed as instruction: $text",
                expectedLabels.any { text.startsWith(it) }
            )
        }

        // Verify we can find space definitions
        val spaceDefs = PsiTreeUtil.findChildrenOfType(psiFile, NasmSpaceDef::class.java)
        println("\nFound ${spaceDefs.size} space definitions:")
        spaceDefs.forEach { spaceDef ->
            println("  Space def: ${spaceDef.text}")
        }

        // We should have 6 space definitions (resb, resq, resd, resw, resb, resb)
        assertEquals("Should have 6 space definitions", 6, spaceDefs.size)
    }

    fun testLabelParsing() {
        val psiFile = myFixture.configureByText(
            "test2.asm",
            """
                buffer resb 1024
            """.trimIndent()
        )

        // Check what PSI elements are created
        val labels = PsiTreeUtil.findChildrenOfType(psiFile, NasmLabelDef::class.java)
        println("Label definitions found: ${labels.size}")
        labels.forEach { label ->
            println("  ${label.text} (name: ${label.name})")
        }

        val instructions = PsiTreeUtil.findChildrenOfType(psiFile, NasmInstruction::class.java)
        println("Instructions found: ${instructions.size}")
        instructions.forEach { instruction ->
            println("  ${instruction.text}")
        }

        val spaceDefs = PsiTreeUtil.findChildrenOfType(psiFile, NasmSpaceDef::class.java)
        println("Space definitions found: ${spaceDefs.size}")
        spaceDefs.forEach { spaceDef ->
            println("  ${spaceDef.text}")
        }

        // Assertions
        assertTrue("Should find label 'buffer'", labels.any { it.name == "buffer" })
        assertTrue("Should find space definition", spaceDefs.isNotEmpty())
        assertTrue("Should NOT parse 'buffer' as instruction",
            instructions.none { it.text.startsWith("buffer") })
    }
}