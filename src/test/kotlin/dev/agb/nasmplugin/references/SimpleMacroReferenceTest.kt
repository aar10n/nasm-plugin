package dev.agb.nasmplugin.references

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes
import dev.agb.nasmplugin.psi.impl.NasmPsiUtils.isInMacroDefinitionBody

class SimpleMacroReferenceTest : BasePlatformTestCase() {

    fun testIdentifierHasReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %define MACRO 123
                %define ANOTHER_MACRO (MACRO+456)
            """.trimIndent()
        )

        // Find IDENTIFIER tokens in the file
        val identifiers = PsiTreeUtil.findChildrenOfType(psiFile, com.intellij.psi.PsiElement::class.java)
            .filter { it.node?.elementType == NasmTypes.IDENTIFIER }

        println("Found ${identifiers.size} IDENTIFIER tokens")
        identifiers.forEach { id ->
            println("IDENTIFIER: ${id.text} at offset ${id.textOffset}")
            println("  Parent: ${id.parent?.javaClass?.simpleName}")
            println("  In macro definition body: ${id.isInMacroDefinitionBody()}")

            // Check references on the identifier itself
            val refs = id.references
            println("  Direct references: ${refs.size}")

            // Also check references on the parent (for NasmPreprocessorTokenImpl)
            val parentRefs = id.parent?.references ?: emptyArray()
            println("  Parent references: ${parentRefs.size}")
            parentRefs.forEach { ref ->
                println("    Parent reference: ${ref.javaClass.simpleName}")
                val resolved = ref.resolve()
                println("    Resolves to: ${resolved?.text}")
            }
        }

        // Find the MACRO identifier in the second %define
        val macroInSecondDefine = identifiers.find {
            it.text == "MACRO" && it.textOffset > 20 // After first line
        }

        assertNotNull("Should find MACRO in second %define", macroInSecondDefine)

        // Check references on the parent element (NasmPreprocessorToken)
        val parentReferences = macroInSecondDefine!!.parent?.references ?: emptyArray()
        assertTrue("MACRO's parent in second %define should have references, but has ${parentReferences.size}", parentReferences.isNotEmpty())

        val resolved = parentReferences.firstOrNull()?.resolve()
        assertNotNull("Reference should resolve to something", resolved)

        println("MACRO reference resolves to: ${resolved?.text}")
    }
}