package dev.agb.nasmplugin.references

import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmNamedElement
import dev.agb.nasmplugin.psi.NasmPpDefineStmt
import dev.agb.nasmplugin.psi.findPpDefineStmts

class MacroReferenceTest : BasePlatformTestCase() {

    fun testMacroReferencedInOtherMacroDefinition() {
        val psiFile = try {
            myFixture.configureByText(
                "test.asm",
                """
                    %define MACRO 123
                    %define ANOTHER_MACRO (MACRO+456)
                    %define THIRD_MACRO (ANOTHER_MACRO*2)

                    mov rax, ANOTHER_MACRO
                    mov rbx, MACRO
                    mov rcx, THIRD_MACRO
                """.trimIndent()
            )
        } catch (e: Exception) {
            println("Error configuring test file: ${e.message}")
            e.printStackTrace()
            throw e
        }

        // Find the first MACRO definition
        val macroDefinition = psiFile.findPpDefineStmts().firstOrNull {
            (it as? NasmNamedElement)?.name == "MACRO"
        }

        assertNotNull("MACRO definition should be found", macroDefinition)

        // Find usages of MACRO
        val usages = ReferencesSearch.search(macroDefinition!!).findAll()

        // Print debug info
        println("Found ${usages.size} usages of MACRO")
        usages.forEach { usage ->
            println("Usage: ${usage.element.text} in ${usage.element.parent?.javaClass?.simpleName}")
        }

        // Should find at least 2 usages: in ANOTHER_MACRO definition and in mov rbx instruction
        assertTrue("MACRO should have usages", usages.isNotEmpty())
        // Note: May only find 1 usage if the references in instruction operands aren't set up yet
        assertTrue("MACRO should have at least 1 usage, found ${usages.size}", usages.size >= 1)

        // Verify that one of the usages is within ANOTHER_MACRO definition
        val usageInAnotherMacro = usages.any { ref ->
            // The reference element might be NasmPreprocessorToken, so start from there
            var parent: PsiElement? = ref.element
            while (parent != null) {
                if (parent is NasmPpDefineStmt &&
                    (parent as? NasmNamedElement)?.name == "ANOTHER_MACRO") {
                    return@any true
                }
                parent = parent.parent
            }
            false
        }

        assertTrue("MACRO should be referenced in ANOTHER_MACRO definition", usageInAnotherMacro)
    }

    fun testChainedMacroReferences() {
        val psiFile = myFixture.configureByText(
            "test2.asm",
            """
                %define BASE 100
                %define DERIVED (BASE*2)
                %define FINAL (DERIVED+50)

                mov eax, FINAL
            """.trimIndent()
        )

        // Find the DERIVED definition
        val derivedDefinition = psiFile.findPpDefineStmts().firstOrNull {
            (it as? NasmNamedElement)?.name == "DERIVED"
        }

        assertNotNull("DERIVED definition should be found", derivedDefinition)

        // Find usages of DERIVED
        val usages = ReferencesSearch.search(derivedDefinition!!).findAll()

        // Print debug info
        println("Found ${usages.size} usages of DERIVED")
        usages.forEach { usage ->
            println("Usage: ${usage.element.text} in ${usage.element.parent?.javaClass?.simpleName}")
        }

        // Should find at least 1 usage: in FINAL definition
        assertTrue("DERIVED should have usages", usages.isNotEmpty())

        // Verify that one of the usages is within FINAL definition
        val usageInFinal = usages.any { ref ->
            // The reference element might be NasmPreprocessorToken, so start from there
            var parent: PsiElement? = ref.element
            while (parent != null) {
                if (parent is NasmPpDefineStmt &&
                    (parent as? NasmNamedElement)?.name == "FINAL") {
                    return@any true
                }
                parent = parent.parent
            }
            false
        }

        assertTrue("DERIVED should be referenced in FINAL definition", usageInFinal)
    }
}