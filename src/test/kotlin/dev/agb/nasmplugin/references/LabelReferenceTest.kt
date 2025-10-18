package dev.agb.nasmplugin.references

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes

class LabelReferenceTest : BasePlatformTestCase() {

    fun testSimpleLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                my_label:
                    mov rax, 1
                    jmp my_label
            """.trimIndent()
        )

        // Find all IDENTIFIER tokens
        val identifiers = PsiTreeUtil.findChildrenOfType(psiFile, com.intellij.psi.PsiElement::class.java)
            .filter { it.node?.elementType == NasmTypes.IDENTIFIER }

        println("Found ${identifiers.size} IDENTIFIER tokens")
        identifiers.forEach { id ->
            println("IDENTIFIER: ${id.text} at offset ${id.textOffset}")
            println("  Parent: ${id.parent?.javaClass?.simpleName}")

            // Check references on the identifier itself
            val refs = id.references
            println("  Direct references: ${refs.size}")
            refs.forEach { ref ->
                println("    Reference: ${ref.javaClass.simpleName}")
                val resolved = ref.resolve()
                println("    Resolves to: ${resolved?.text}")
            }

            // Check references on the parent
            val parentRefs = id.parent?.references ?: emptyArray()
            println("  Parent references: ${parentRefs.size}")
            parentRefs.forEach { ref ->
                println("    Parent reference: ${ref.javaClass.simpleName}")
                val resolved = ref.resolve()
                println("    Resolves to: ${resolved?.text}")
            }
        }

        // Find the "my_label" in the jmp instruction
        val labelRef = identifiers.find {
            it.text == "my_label" && it.textOffset > 10  // After the label definition
        }

        assertNotNull("Should find my_label reference in jmp instruction", labelRef)

        // Check if it has references (either directly or on parent)
        val hasReference = labelRef!!.references.isNotEmpty() ||
                           labelRef.parent?.references?.isNotEmpty() == true

        assertTrue("Label reference should have references", hasReference)

        // Check if it resolves
        val resolved = labelRef.references.firstOrNull()?.resolve() ?:
                       labelRef.parent?.references?.firstOrNull()?.resolve()

        assertNotNull("Label reference should resolve to something", resolved)
        println("Label reference resolves to: ${resolved?.text}")
    }

    fun testLocalLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                global_label:
                    .local:
                        mov rax, 1
                    jmp .local
            """.trimIndent()
        )

        // Find all symbol_ref elements that contain local labels
        val symbolRefs = PsiTreeUtil.findChildrenOfType(psiFile, dev.agb.nasmplugin.psi.NasmSymbolRef::class.java)

        println("Found ${symbolRefs.size} NasmSymbolRef elements")
        symbolRefs.forEach { ref ->
            println("  SymbolRef: '${ref.text}' at offset ${ref.textOffset}")
            println("    Has references: ${ref.references.isNotEmpty()}")
        }

        // Find the .local in jmp instruction (after offset 30)
        val labelRef = symbolRefs.find {
            it.text == ".local" && it.textOffset > 30
        }

        if (labelRef == null) {
            // If we can't find it as NasmSymbolRef, let's check all elements for debugging
            println("\nDebugging: All elements with '.local' text:")
            val allLocalElements = PsiTreeUtil.findChildrenOfType(psiFile, com.intellij.psi.PsiElement::class.java)
                .filter { it.text == ".local" }

            allLocalElements.forEach { elem ->
                println("  Element: '${elem.text}' at offset ${elem.textOffset}")
                println("    Type: ${elem.node?.elementType}")
                println("    Parent: ${elem.parent?.javaClass?.simpleName}")
                println("    Has references: ${elem.references.isNotEmpty()}")
                println("    Parent has references: ${elem.parent?.references?.isNotEmpty() == true}")
            }
        }

        assertNotNull("Should find .local reference in jmp instruction as NasmSymbolRef", labelRef)

        // Check if it has references
        val hasReference = labelRef!!.references.isNotEmpty()

        assertTrue(".local reference should have references", hasReference)
    }
}