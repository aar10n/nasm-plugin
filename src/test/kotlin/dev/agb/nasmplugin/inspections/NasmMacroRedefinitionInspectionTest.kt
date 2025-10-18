package dev.agb.nasmplugin.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for NasmMacroRedefinitionInspection.
 */
class NasmMacroRedefinitionInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(NasmMacroRedefinitionInspection::class.java)
    }

    fun testSimpleRedefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %define FOO 1
                %define <warning descr="Macro 'FOO' is already defined at line 1">FOO</warning> 2
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testMacroRedefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    nop
                %endmacro

                %macro <warning descr="Macro 'test' is already defined at line 1">test</warning> 0
                    mov rax, rbx
                %endmacro
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testNoRedefinitionInMutuallyExclusiveBranches() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef SOME_MACRO
                  %define MACRO 1
                %elif ANOTHER_MACRO
                  %define MACRO 2
                %else
                  %define MACRO 3
                %endif
            """.trimIndent()
        )

        // Should have no warnings - all branches are mutually exclusive
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("already defined") == true }
        assertTrue("No warnings expected for mutually exclusive branches", warnings.isEmpty())
    }

    fun testNoRedefinitionWithIfElifElse() {
        myFixture.configureByText(
            "test.asm",
            """
                %if 0
                  %define FOO 10
                %elif 1
                  %define FOO 20
                %else
                  %define FOO 30
                %endif
            """.trimIndent()
        )

        // Should have no warnings - all branches are mutually exclusive
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("already defined") == true }
        assertTrue("No warnings expected for mutually exclusive branches", warnings.isEmpty())
    }

    fun testRedefinitionInSameBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef TEST
                  %define BAR 1
                  %define <warning descr="Macro 'BAR' is already defined at line 2">BAR</warning> 2
                %endif
            """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testRedefinitionAcrossDifferentConditionalsFlagged() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef OPTION_A
                  %define CONFIG 1
                %endif

                %ifdef OPTION_B
                  %define <warning descr="Macro 'CONFIG' is already defined at line 2">CONFIG</warning> 2
                %endif
            """.trimIndent()
        )

        // Different conditional blocks that could both be active should flag as redefinition
        // This is conservative but safer - if both OPTION_A and OPTION_B are defined,
        // this would be a real redefinition
        myFixture.testHighlighting()
    }

    fun testAssignNotFlagged() {
        myFixture.configureByText(
            "test.asm",
            """
                %assign COUNTER 0
                %assign COUNTER 1
                %assign COUNTER 2
            """.trimIndent()
        )

        // Should have no warnings - %assign is allowed to redefine
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("already defined") == true }
        assertTrue("No warnings expected for %assign", warnings.isEmpty())
    }

    fun testMultiLineMacrosWithDifferentParamsNotFlagged() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    nop
                %endmacro

                %macro test 1
                    mov rax, %1
                %endmacro
            """.trimIndent()
        )

        // Should have no warnings - different parameter counts make them different macros
        val highlights = myFixture.doHighlighting()
        val warnings = highlights.filter { it.description?.contains("already defined") == true }
        assertTrue("No warnings expected for macros with different parameter counts", warnings.isEmpty())
    }
}
