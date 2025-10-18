package dev.agb.nasmplugin.references

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmLabelDef
import dev.agb.nasmplugin.psi.findLabelDefinition

/**
 * Tests that local labels are resolved only within their parent global label scope
 */
class LocalLabelScopeTest : BasePlatformTestCase() {

    fun testLocalLabelScopesAreSeparate() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                label1:
                    mov rax, 1
                    jmp .done
                .done:
                    ret

                label2:
                    mov rax, 2
                    jmp .done
                .done:
                    ret

                label3:
                    mov rax, 3
                    jmp .done
                .done:
                    ret
            """.trimIndent()
        )

        // Find all label definitions
        val allLabels = PsiTreeUtil.findChildrenOfType(psiFile, NasmLabelDef::class.java).toList()
        println("Found ${allLabels.size} labels:")
        allLabels.forEach { label ->
            println("  ${label.name} at offset ${label.textOffset}")
        }

        // Find the three .done label definitions
        val doneLabels = allLabels.filter { it.name == ".done" }
        assertEquals("Should have exactly 3 .done labels", 3, doneLabels.size)

        val done1 = doneLabels[0]
        val done2 = doneLabels[1]
        val done3 = doneLabels[2]

        println("\nLocal label positions:")
        println("  done1: offset ${done1.textOffset}")
        println("  done2: offset ${done2.textOffset}")
        println("  done3: offset ${done3.textOffset}")

        // Find all references to .done (in the jmp instructions)
        // We'll identify them by their position relative to the label definitions
        val label1Def = allLabels.first { it.name == "label1" }
        val label2Def = allLabels.first { it.name == "label2" }
        val label3Def = allLabels.first { it.name == "label3" }

        println("\nGlobal label positions:")
        println("  label1: offset ${label1Def.textOffset}")
        println("  label2: offset ${label2Def.textOffset}")
        println("  label3: offset ${label3Def.textOffset}")

        // Test resolution from different offsets
        // Create a reference element at a position between label1 and done1
        val offsetInLabel1Scope = label1Def.textOffset + 20
        val offsetInLabel2Scope = label2Def.textOffset + 20
        val offsetInLabel3Scope = label3Def.textOffset + 20

        // Find elements at these offsets to use as context
        val elementInLabel1Scope = psiFile.findElementAt(offsetInLabel1Scope)
        val elementInLabel2Scope = psiFile.findElementAt(offsetInLabel2Scope)
        val elementInLabel3Scope = psiFile.findElementAt(offsetInLabel3Scope)

        assertNotNull("Should find element in label1 scope", elementInLabel1Scope)
        assertNotNull("Should find element in label2 scope", elementInLabel2Scope)
        assertNotNull("Should find element in label3 scope", elementInLabel3Scope)

        println("\nTest context elements:")
        println("  elementInLabel1Scope: offset ${elementInLabel1Scope?.textOffset}, text '${elementInLabel1Scope?.text}'")
        println("  elementInLabel2Scope: offset ${elementInLabel2Scope?.textOffset}, text '${elementInLabel2Scope?.text}'")
        println("  elementInLabel3Scope: offset ${elementInLabel3Scope?.textOffset}, text '${elementInLabel3Scope?.text}'")

        // Test that .done resolves to the correct label in each scope
        val resolvedFromLabel1 = psiFile.findLabelDefinition(".done", elementInLabel1Scope)
        val resolvedFromLabel2 = psiFile.findLabelDefinition(".done", elementInLabel2Scope)
        val resolvedFromLabel3 = psiFile.findLabelDefinition(".done", elementInLabel3Scope)

        println("\nResolution results:")
        println("  From label1 scope: ${resolvedFromLabel1?.text} at offset ${resolvedFromLabel1?.textOffset}")
        println("  From label2 scope: ${resolvedFromLabel2?.text} at offset ${resolvedFromLabel2?.textOffset}")
        println("  From label3 scope: ${resolvedFromLabel3?.text} at offset ${resolvedFromLabel3?.textOffset}")

        assertNotNull("Should resolve .done from label1 scope", resolvedFromLabel1)
        assertNotNull("Should resolve .done from label2 scope", resolvedFromLabel2)
        assertNotNull("Should resolve .done from label3 scope", resolvedFromLabel3)

        // Verify each resolves to the correct .done label
        assertEquals(
            "Reference in label1 scope should resolve to first .done",
            done1.textOffset,
            resolvedFromLabel1!!.textOffset
        )

        assertEquals(
            "Reference in label2 scope should resolve to second .done",
            done2.textOffset,
            resolvedFromLabel2!!.textOffset
        )

        assertEquals(
            "Reference in label3 scope should resolve to third .done",
            done3.textOffset,
            resolvedFromLabel3!!.textOffset
        )

        println("\n✓ All local labels resolved to correct scopes")
    }
}
