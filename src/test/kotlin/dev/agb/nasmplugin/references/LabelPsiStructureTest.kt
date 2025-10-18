package dev.agb.nasmplugin.references

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmLabelDef

class LabelPsiStructureTest : BasePlatformTestCase() {

    fun testLabelDefStructure() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                my_label:
                    mov rax, 1
            """.trimIndent()
        )

        // Find all NasmLabelDef elements
        val labelDefs = PsiTreeUtil.findChildrenOfType(psiFile, NasmLabelDef::class.java)

        println("Found ${labelDefs.size} NasmLabelDef elements")
        labelDefs.forEach { labelDef ->
            println("NasmLabelDef: ${labelDef.text}")
            println("  Name: ${labelDef.name}")
            println("  Class: ${labelDef.javaClass.simpleName}")
            println("  Children:")
            labelDef.children.forEach { child ->
                println("    ${child.javaClass.simpleName}: ${child.text}")
            }
        }

        assertTrue("Should find at least one NasmLabelDef", labelDefs.isNotEmpty())

        val firstLabel = labelDefs.firstOrNull()
        assertNotNull("Should find first label", firstLabel)
        assertEquals("Label name should be 'my_label'", "my_label", firstLabel?.name)
    }

    fun testLabelDefAndReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                my_label:
                    mov rax, 1
                    jmp my_label
            """.trimIndent()
        )

        // Find all label definitions
        val labelDefs = PsiTreeUtil.findChildrenOfType(psiFile, NasmLabelDef::class.java)
        println("Found ${labelDefs.size} label definitions:")
        labelDefs.forEach { def ->
            println("  ${def.name} at ${def.textOffset}")
        }

        // Find all symbol references
        val symbolRefs = PsiTreeUtil.findChildrenOfType(psiFile,
            dev.agb.nasmplugin.psi.NasmSymbolRef::class.java)
        println("Found ${symbolRefs.size} symbol references:")
        symbolRefs.forEach { ref ->
            println("  ${ref.text} at ${ref.textOffset}")
            val resolved = ref.reference?.resolve()
            println("    Resolves to: ${resolved?.text} (${resolved?.javaClass?.simpleName})")
        }
    }
}