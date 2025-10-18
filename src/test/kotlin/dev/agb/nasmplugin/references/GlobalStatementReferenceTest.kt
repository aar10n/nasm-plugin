package dev.agb.nasmplugin.references

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmGlobalDir
import dev.agb.nasmplugin.psi.NasmSymbolDecl
import dev.agb.nasmplugin.psi.findLabels

class GlobalStatementReferenceTest : BasePlatformTestCase() {

    fun testGlobalStatementReferencesLabel() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                global _start       ; This should count as a reference to _start
                global main         ; This should count as a reference to main

                _start:
                    mov rax, 1
                    ret

                main:
                    xor rax, rax
                    ret

                unused_label:       ; This should be marked as unused
                    nop
                    ret
            """.trimIndent()
        )

        // Find global statements
        val globalDirs = PsiTreeUtil.findChildrenOfType(psiFile, NasmGlobalDir::class.java)
        println("Found ${globalDirs.size} global directives")

        // Find symbol declarations in global statements
        val symbolDecls = PsiTreeUtil.findChildrenOfType(psiFile, NasmSymbolDecl::class.java)
        println("Found ${symbolDecls.size} symbol declarations")
        symbolDecls.forEach { decl ->
            println("Symbol decl: ${decl.text}")
            println("  Name: ${decl.name}")

            // Check if it has a reference
            val refs = decl.references
            println("  Has ${refs.size} references")
            refs.forEach { ref ->
                println("    Reference: ${ref.javaClass.simpleName}")
                val resolved = ref.resolve()
                println("    Resolves to: ${resolved?.text} (${resolved?.javaClass?.simpleName})")
            }
        }

        // Find labels
        val labels = psiFile.findLabels()
        println("\nFound ${labels.size} labels:")
        labels.forEach { label ->
            println("  ${label.name}")

            // Check usages
            val usages = ReferencesSearch.search(label).findAll()
            println("    Has ${usages.size} usages")
            usages.forEach { usage ->
                println("      Usage at: ${usage.element.text} in ${usage.element.parent?.text}")
            }
        }

        // Test that _start has at least one usage (from global statement)
        val startLabel = labels.firstOrNull { it.name == "_start" }
        assertNotNull("_start label should exist", startLabel)

        val startUsages = ReferencesSearch.search(startLabel!!).findAll()
        assertTrue("_start should have at least 1 usage (from global statement)", startUsages.isNotEmpty())

        // Test that main has at least one usage (from global statement)
        val mainLabel = labels.firstOrNull { it.name == "main" }
        assertNotNull("main label should exist", mainLabel)

        val mainUsages = ReferencesSearch.search(mainLabel!!).findAll()
        assertTrue("main should have at least 1 usage (from global statement)", mainUsages.isNotEmpty())

        // Test that unused_label has no usages
        val unusedLabel = labels.firstOrNull { it.name == "unused_label" }
        assertNotNull("unused_label should exist", unusedLabel)

        val unusedUsages = ReferencesSearch.search(unusedLabel!!).findAll()
        assertTrue("unused_label should have no usages", unusedUsages.isEmpty())
    }
}