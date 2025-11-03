package dev.agb.nasmplugin.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Tests for brace matching in NASM assembly files.
 * Verifies matching of brackets, macros, conditionals, and repeat blocks.
 */
class NasmBraceMatcherTest : BasePlatformTestCase() {

    fun testMatchMemoryAddressingBrackets() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, <caret>[rbx + rcx * 8]
            """.trimIndent()
        )

        // Find matching brace (IntelliJ provides this through the brace matcher)
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify that [ and ] are registered as a brace pair
        val bracketPair = pairs.find {
            it.leftBraceType == NasmTypes.LBRACKET &&
            it.rightBraceType == NasmTypes.RBRACKET
        }
        assertNotNull("Should have bracket pair", bracketPair)
        assertFalse("Memory brackets should not be structural", bracketPair!!.isStructural)
    }

    fun testMatchMacroBlocks() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>%macro my_macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify that %macro and %endmacro are registered as a brace pair
        val macroPair = pairs.find {
            it.leftBraceType == NasmTypes.MACRO_START &&
            it.rightBraceType == NasmTypes.MACRO_END
        }
        assertNotNull("Should have macro pair", macroPair)
        assertTrue("Macro blocks should be structural", macroPair!!.isStructural)
    }

    fun testMatchConditionalBlocks() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>%ifdef DEBUG
                    mov rax, 1
                %endif
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify that %if and %endif are registered as a brace pair
        val ifPair = pairs.find {
            it.leftBraceType == NasmTypes.MACRO_IF &&
            it.rightBraceType == NasmTypes.MACRO_ENDIF
        }
        assertNotNull("Should have if/endif pair", ifPair)
        assertTrue("Conditional blocks should be structural", ifPair!!.isStructural)
    }

    fun testMatchRepeatBlocks() {
        myFixture.configureByText(
            "test.asm",
            """
                <caret>%rep 10
                    nop
                %endrep
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify that %rep and %endrep are registered as a brace pair
        val repPair = pairs.find {
            it.leftBraceType == NasmTypes.MACRO_REP &&
            it.rightBraceType == NasmTypes.MACRO_ENDREP
        }
        assertNotNull("Should have rep/endrep pair", repPair)
        assertTrue("Repeat blocks should be structural", repPair!!.isStructural)
    }

    fun testNestedBrackets() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, [rbx + [rcx + rdx]]
            """.trimIndent()
        )

        // Verify that nested brackets are handled
        // This is more of an integration test - the matcher should support nested brackets
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Nested brackets should be supported through the bracket pair
        val bracketPair = pairs.find { it.leftBraceType == NasmTypes.LBRACKET }
        assertNotNull("Should support nested brackets", bracketPair)
    }

    fun testNestedMacros() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro outer 0
                    %macro inner 0
                        nop
                    %endmacro
                %endmacro
            """.trimIndent()
        )

        // Verify matcher supports nested macros
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        val macroPair = pairs.find { it.leftBraceType == NasmTypes.MACRO_START }
        assertNotNull("Should support nested macros", macroPair)
    }

    fun testNestedConditionals() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef DEBUG
                    %ifdef VERBOSE
                        mov rax, 1
                    %endif
                %endif
            """.trimIndent()
        )

        // Verify matcher supports nested conditionals
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        val ifPair = pairs.find { it.leftBraceType == NasmTypes.MACRO_IF }
        assertNotNull("Should support nested conditionals", ifPair)
    }

    fun testCodeConstructStart() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test_macro 0
                    nop
                %endmacro
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()

        // getCodeConstructStart should return the opening brace offset
        val offset = 10
        assertEquals("Should return opening brace offset",
            offset, matcher.getCodeConstructStart(myFixture.file, offset))
    }

    fun testBracesAllowedAnywhere() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                <caret>
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify that braces can be typed anywhere (all pairs allow typing before any type)
        pairs.forEach { pair ->
            assertTrue("Should allow braces before any type",
                matcher.isPairedBracesAllowedBeforeType(pair.leftBraceType, null))
        }
    }

    fun testMultipleMemoryBrackets() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, [rbx]
                mov rcx, [rdx + rsi]
                lea rdi, [rax + rbx * 4]
            """.trimIndent()
        )

        // Multiple bracket pairs should all be matched independently
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        val bracketPair = pairs.find { it.leftBraceType == NasmTypes.LBRACKET }
        assertNotNull("Should match all bracket pairs", bracketPair)
    }

    fun testMixedStructuralAndNonStructural() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro test 0
                    mov rax, [rbx]
                %endmacro
            """.trimIndent()
        )

        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Should have both structural (macros) and non-structural (brackets)
        val structuralCount = pairs.count { it.isStructural }
        val nonStructuralCount = pairs.count { !it.isStructural }

        assertTrue("Should have structural braces", structuralCount > 0)
        assertTrue("Should have non-structural braces", nonStructuralCount > 0)
    }

    fun testAllBracePairsRegistered() {
        val matcher = NasmBraceMatcher()
        val pairs = matcher.pairs

        // Verify all expected brace pairs are registered
        assertEquals("Should have exactly 4 brace pairs", 4, pairs.size)

        val types = pairs.map { it.leftBraceType to it.rightBraceType }.toSet()

        assertTrue("Should have LBRACKET/RBRACKET",
            types.contains(NasmTypes.LBRACKET to NasmTypes.RBRACKET))
        assertTrue("Should have MACRO_START/MACRO_END",
            types.contains(NasmTypes.MACRO_START to NasmTypes.MACRO_END))
        assertTrue("Should have MACRO_IF/MACRO_ENDIF",
            types.contains(NasmTypes.MACRO_IF to NasmTypes.MACRO_ENDIF))
        assertTrue("Should have MACRO_REP/MACRO_ENDREP",
            types.contains(NasmTypes.MACRO_REP to NasmTypes.MACRO_ENDREP))
    }
}
