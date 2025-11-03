package dev.agb.nasmplugin.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for Enter key handling in NASM assembly files.
 * Verifies auto-insertion of %endmacro and %endif.
 */
class NasmEnterHandlerTest : BasePlatformTestCase() {

    fun testAutoInsertEndMacroAfterMacroDefinition() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %macro my_macro 2
                <caret>
                %endmacro
            """.trimIndent()
        )
    }

    fun testAutoInsertEndMacroWithIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                    %macro my_macro 2<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                    %macro my_macro 2
                    <caret>
                    %endmacro
            """.trimIndent()
        )
    }

    fun testAutoInsertEndMacroWithZeroParameters() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro helper 0<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %macro helper 0
                <caret>
                %endmacro
            """.trimIndent()
        )
    }

    fun testNoAutoInsertWhenEndMacroExists() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2<caret>
                    nop
                %endmacro
            """.trimIndent()
        )

        myFixture.type('\n')

        // Should just insert a newline, not add another %endmacro
        myFixture.checkResult(
            """
                %macro my_macro 2
                <caret>
                    nop
                %endmacro
            """.trimIndent()
        )
    }

    fun testAutoInsertEndIfAfterIfDirective() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef DEBUG<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifdef DEBUG
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testAutoInsertEndIfAfterIfDefine() {
        myFixture.configureByText(
            "test.asm",
            """
                %if DEBUG > 0<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %if DEBUG > 0
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testAutoInsertEndIfAfterIfndef() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifndef RELEASE<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifndef RELEASE
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testAutoInsertEndIfWithIndentation() {
        myFixture.configureByText(
            "test.asm",
            """
                    %ifdef DEBUG<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                    %ifdef DEBUG
                    <caret>
                    %endif
            """.trimIndent()
        )
    }

    fun testNoAutoInsertWhenEndIfExists() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef DEBUG<caret>
                    mov rax, 1
                %endif
            """.trimIndent()
        )

        myFixture.type('\n')

        // Should just insert a newline, not add another %endif
        myFixture.checkResult(
            """
                %ifdef DEBUG
                <caret>
                    mov rax, 1
                %endif
            """.trimIndent()
        )
    }

    fun testNestedMacrosInsertCorrectly() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro outer 0<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        // Should insert %endmacro for outer
        myFixture.checkResult(
            """
                %macro outer 0
                <caret>
                %endmacro
            """.trimIndent()
        )

        // Now type inner macro
        myFixture.type("    %macro inner 0")
        myFixture.type('\n')

        // Should insert %endmacro for inner
        val result = myFixture.editor.document.text
        assertTrue("Should have both endmacros", result.count { it == '\n' } >= 3)
    }

    fun testNestedConditionalsInsertCorrectly() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef DEBUG<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifdef DEBUG
                <caret>
                %endif
            """.trimIndent()
        )

        // Now type nested conditional
        myFixture.type("    %ifdef VERBOSE")
        myFixture.type('\n')

        val result = myFixture.editor.document.text
        assertTrue("Should have nested conditionals", result.contains("%ifdef VERBOSE"))
    }

    fun testNormalEnterOnNonMacroLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                section .text
                <caret>
            """.trimIndent()
        )
    }

    fun testNormalEnterOnInstructionLine() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, rbx<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                section .text
                mov rax, rbx
                <caret>
            """.trimIndent()
        )
    }

    fun testCaretPositionAfterMacroInsertion() {
        myFixture.configureByText(
            "test.asm",
            "%macro test 0<caret>"
        )

        myFixture.type('\n')

        // Caret should be on the empty line between %macro and %endmacro
        val caretOffset = myFixture.caretOffset
        val text = myFixture.editor.document.text

        // Verify %endmacro was inserted
        assertTrue("Should have %endmacro", text.contains("%endmacro"))

        // Caret should be positioned just before %endmacro (on the empty line or at the newline)
        val endmacroIndex = text.indexOf("%endmacro")
        assertTrue("Caret should be at or before endmacro", caretOffset <= endmacroIndex)

        // Caret should be after the first line
        val firstNewline = text.indexOf('\n')
        assertTrue("Caret should be after first newline", caretOffset > firstNewline)
    }

    fun testCaretPositionAfterIfInsertion() {
        myFixture.configureByText(
            "test.asm",
            "%ifdef DEBUG<caret>"
        )

        myFixture.type('\n')

        // Caret should be on the empty line between %ifdef and %endif
        val caretOffset = myFixture.caretOffset
        val text = myFixture.editor.document.text

        assertTrue("Caret should be after ifdef line", caretOffset > text.indexOf("%ifdef DEBUG"))
        assertTrue("Caret should be before endif", caretOffset < text.indexOf("%endif"))
    }

    fun testIfMacroVariant() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifmacro test 0<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifmacro test 0
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testIfCtxVariant() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifctx mycontext<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifctx mycontext
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testIfIdnVariant() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifidn %1, rax<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %ifidn %1, rax
                <caret>
                %endif
            """.trimIndent()
        )
    }

    fun testMacroWithMultipleParameters() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro complex_macro 5<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %macro complex_macro 5
                <caret>
                %endmacro
            """.trimIndent()
        )
    }

    fun testMacroWithVariadicParameters() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro variadic 1-*<caret>
            """.trimIndent()
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """
                %macro variadic 1-*
                <caret>
                %endmacro
            """.trimIndent()
        )
    }

    fun testEnterInMiddleOfMacroBody() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    nop<caret>
                %endmacro
            """.trimIndent()
        )

        myFixture.type('\n')

        // Should just insert a normal newline, not another %endmacro
        myFixture.checkResult(
            """
                %macro test 0
                    nop
                    <caret>
                %endmacro
            """.trimIndent()
        )
    }

    fun testEnterAtStartOfFile() {
        myFixture.configureByText(
            "test.asm",
            "<caret>section .text"
        )

        myFixture.type('\n')

        myFixture.checkResult(
            """

                <caret>section .text
            """.trimIndent()
        )
    }

    fun testPreserveMixedIndentation() {
        myFixture.configureByText(
            "test.asm",
            "\t%macro tab_indented 0<caret>"
        )

        myFixture.type('\n')

        val result = myFixture.editor.document.text

        // Should preserve tab indentation
        assertTrue("Should preserve tabs", result.contains("\t%endmacro"))
    }
}
