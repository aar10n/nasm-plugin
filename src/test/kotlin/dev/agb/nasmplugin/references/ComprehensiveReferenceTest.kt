package dev.agb.nasmplugin.references

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmNamedElement
import dev.agb.nasmplugin.psi.findLabels
import dev.agb.nasmplugin.psi.findPpDefineStmts

class ComprehensiveReferenceTest : BasePlatformTestCase() {

    fun testMacroAndLabelReferences() {
        val psiFile = myFixture.configureByText(
            "comprehensive.asm",
            """
                ; Macros that reference each other
                %define BASE 100
                %define DERIVED (BASE*2)

                ; Labels
                main:
                    mov rax, DERIVED    ; Uses DERIVED macro
                    jmp main           ; References main label

                loop_start:
                    inc rcx
                    jmp loop_start     ; References loop_start label

                ; Macro using another macro
                %define FINAL (DERIVED+50)
                    mov rbx, FINAL
            """.trimIndent()
        )

        // Test macro references
        val baseMacro = psiFile.findPpDefineStmts().firstOrNull {
            (it as? NasmNamedElement)?.name == "BASE"
        }
        assertNotNull("BASE macro should be found", baseMacro)

        val baseUsages = ReferencesSearch.search(baseMacro!!).findAll()
        assertTrue("BASE should have at least 1 usage (in DERIVED)", baseUsages.size >= 1)

        val derivedMacro = psiFile.findPpDefineStmts().firstOrNull {
            (it as? NasmNamedElement)?.name == "DERIVED"
        }
        assertNotNull("DERIVED macro should be found", derivedMacro)

        val derivedUsages = ReferencesSearch.search(derivedMacro!!).findAll()
        assertTrue("DERIVED should have at least 1 usage (in FINAL)", derivedUsages.size >= 1)

        // Test label references
        val mainLabel = psiFile.findLabels().firstOrNull {
            it.name == "main"
        }
        assertNotNull("main label should be found", mainLabel)

        val mainUsages = ReferencesSearch.search(mainLabel!!).findAll()
        assertTrue("main label should have at least 1 usage", mainUsages.size >= 1)

        val loopLabel = psiFile.findLabels().firstOrNull {
            it.name == "loop_start"
        }
        assertNotNull("loop_start label should be found", loopLabel)

        val loopUsages = ReferencesSearch.search(loopLabel!!).findAll()
        assertTrue("loop_start label should have at least 1 usage", loopUsages.size >= 1)
    }
}