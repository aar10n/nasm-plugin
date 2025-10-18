package dev.agb.nasmplugin.references

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.findLabels

class GlobalExternReferenceTest : BasePlatformTestCase() {

    fun testGlobalAndExternStatements() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                ; External function we're importing
                extern printf

                ; Export our main entry point
                global _start
                global helper_func

                section .text
                _start:
                    call helper_func
                    call printf         ; Use external function
                    ret

                helper_func:
                    mov rax, 42
                    ret

                unused_internal:        ; Should be marked as unused
                    nop
                    ret
            """.trimIndent()
        )

        // Test _start label has usage from global statement
        val startLabel = psiFile.findLabels().firstOrNull { it.name == "_start" }
        assertNotNull("_start label should exist", startLabel)

        val startUsages = ReferencesSearch.search(startLabel!!).findAll()
        assertTrue("_start should have at least 1 usage (from global)", startUsages.size >= 1)

        // Verify one usage is from the global statement
        val globalUsage = startUsages.any { usage ->
            usage.element.text == "_start" &&
            usage.element.parent?.parent?.text?.startsWith("global") == true
        }
        assertTrue("_start should have a usage from global statement", globalUsage)

        // Test helper_func has usages from both global and call
        val helperLabel = psiFile.findLabels().firstOrNull { it.name == "helper_func" }
        assertNotNull("helper_func label should exist", helperLabel)

        val helperUsages = ReferencesSearch.search(helperLabel!!).findAll()
        assertTrue("helper_func should have at least 2 usages (global and call)", helperUsages.size >= 2)

        // Test unused_internal has no usages
        val unusedLabel = psiFile.findLabels().firstOrNull { it.name == "unused_internal" }
        assertNotNull("unused_internal label should exist", unusedLabel)

        val unusedUsages = ReferencesSearch.search(unusedLabel!!).findAll()
        assertTrue("unused_internal should have no usages", unusedUsages.isEmpty())
    }

    fun testGlobalWithTypeSyntax() {
        val psiFile = myFixture.configureByText(
            "test2.asm",
            """
                ; Global with type specification
                global my_function:function
                global my_data:data

                my_function:
                    ret

                my_data:
                    db 0x42
            """.trimIndent()
        )

        // Test that symbols with type annotations still work
        val funcLabel = psiFile.findLabels().firstOrNull { it.name == "my_function" }
        assertNotNull("my_function label should exist", funcLabel)

        val funcUsages = ReferencesSearch.search(funcLabel!!).findAll()
        assertTrue("my_function should have usage from global", funcUsages.isNotEmpty())

        val dataLabel = psiFile.findLabels().firstOrNull { it.name == "my_data" }
        assertNotNull("my_data label should exist", dataLabel)

        val dataUsages = ReferencesSearch.search(dataLabel!!).findAll()
        assertTrue("my_data should have usage from global", dataUsages.isNotEmpty())
    }
}