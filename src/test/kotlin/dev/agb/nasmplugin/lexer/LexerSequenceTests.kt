package dev.agb.nasmplugin.lexer

import com.intellij.psi.TokenType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Advanced lexer tests that validate complete token sequences.
 * These tests ensure the lexer correctly tokenizes complex, real-world code patterns.
 */
class LexerSequenceTests : BasePlatformTestCase() {

    fun testCompleteInstructionTokenization() {
        val code = "mov rax, [rbx + rcx * 8 + 0x10]"
        val expected = listOf(
            "mov" to NasmTypes.IDENTIFIER,
            "rax" to NasmTypes.REGISTER,
            "," to NasmTypes.COMMA,
            "[" to NasmTypes.LBRACKET,
            "rbx" to NasmTypes.REGISTER,
            "+" to NasmTypes.PLUS,
            "rcx" to NasmTypes.REGISTER,
            "*" to NasmTypes.MUL,
            "8" to NasmTypes.NUMBER,
            "+" to NasmTypes.PLUS,
            "0x10" to NasmTypes.NUMBER,
            "]" to NasmTypes.RBRACKET
        )

        assertTokenSequence(code, expected)
    }

    fun testMacroDefinitionTokenization() {
        val code = "%macro PUSH_ALL 0"
        val expected = listOf(
            "%macro" to NasmTypes.MACRO_START,
            "PUSH_ALL" to NasmTypes.IDENTIFIER,
            "0" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testDataDefinitionTokenization() {
        val code = "msg db 'Hello', 0"
        val expected = listOf(
            "msg" to NasmTypes.IDENTIFIER,
            "db" to NasmTypes.DATA_SIZE,
            "'Hello'" to NasmTypes.STRING,
            "," to NasmTypes.COMMA,
            "0" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testConditionalTokenization() {
        val code = "%if SIZE > 100"
        val expected = listOf(
            "%if" to NasmTypes.MACRO_IF,
            "SIZE" to NasmTypes.IDENTIFIER,
            ">" to NasmTypes.GT,
            "100" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testAVX512InstructionTokenization() {
        val code = "vaddps zmm0 {k1}{z}, zmm1, [rax]{1to16}"
        val expected = listOf(
            "vaddps" to NasmTypes.IDENTIFIER,
            "zmm0" to NasmTypes.REGISTER,
            "{" to NasmTypes.LBRACE,
            "k1" to NasmTypes.MASK_REG,
            "}" to NasmTypes.RBRACE,
            "{" to NasmTypes.LBRACE,
            "z" to NasmTypes.ZEROING,
            "}" to NasmTypes.RBRACE,
            "," to NasmTypes.COMMA,
            "zmm1" to NasmTypes.REGISTER,
            "," to NasmTypes.COMMA,
            "[" to NasmTypes.LBRACKET,
            "rax" to NasmTypes.REGISTER,
            "]" to NasmTypes.RBRACKET,
            "{" to NasmTypes.LBRACE,
            "1to16" to NasmTypes.BROADCAST,
            "}" to NasmTypes.RBRACE
        )

        assertTokenSequence(code, expected)
    }

    fun testSectionDirectiveTokenization() {
        val code = "section .text align=16 exec"
        val expected = listOf(
            "section" to NasmTypes.SECTION_KW,
            ".text" to NasmTypes.IDENTIFIER,
            "align" to NasmTypes.ALIGN_KW,
            "=" to NasmTypes.EQ,
            "16" to NasmTypes.NUMBER,
            "exec" to NasmTypes.SECTION_ATTR_KW
        )

        assertTokenSequence(code, expected)
    }

    fun testMacroParameterTokenization() {
        val code = "mov %1, %2"
        val expected = listOf(
            "mov" to NasmTypes.IDENTIFIER,
            "%1" to NasmTypes.MACRO_PARAM,
            "," to NasmTypes.COMMA,
            "%2" to NasmTypes.MACRO_PARAM
        )

        assertTokenSequence(code, expected)
    }

    fun testExpressionTokenization() {
        val code = "(SIZE * 2 + OFFSET) & 0xFF"
        val expected = listOf(
            "(" to NasmTypes.LPAREN,
            "SIZE" to NasmTypes.IDENTIFIER,
            "*" to NasmTypes.MUL,
            "2" to NasmTypes.NUMBER,
            "+" to NasmTypes.PLUS,
            "OFFSET" to NasmTypes.IDENTIFIER,
            ")" to NasmTypes.RPAREN,
            "&" to NasmTypes.AMP,
            "0xFF" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testComplexMacroExpansion() {
        val code = "%[SIZE * 2]"
        val expected = listOf(
            "%[" to NasmTypes.MACRO_EXPANSION_START,
            "SIZE" to NasmTypes.IDENTIFIER,
            "*" to NasmTypes.MUL,
            "2" to NasmTypes.NUMBER,
            "]" to NasmTypes.RBRACKET
        )

        assertTokenSequence(code, expected)
    }

    fun testLabelWithInstructionTokenization() {
        val code = "loop: dec rcx"
        val expected = listOf(
            "loop" to NasmTypes.IDENTIFIER,
            ":" to NasmTypes.COLON,
            "dec" to NasmTypes.IDENTIFIER,
            "rcx" to NasmTypes.REGISTER
        )

        assertTokenSequence(code, expected)
    }

    fun testTimesDirectiveTokenization() {
        val code = "times 10 db 0"
        val expected = listOf(
            "times" to NasmTypes.TIMES,
            "10" to NasmTypes.NUMBER,
            "db" to NasmTypes.DATA_SIZE,
            "0" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testEquDefinitionTokenization() {
        val code = "SIZE equ 1024"
        val expected = listOf(
            "SIZE" to NasmTypes.IDENTIFIER,
            "equ" to NasmTypes.EQU,
            "1024" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testMultiLineWithComments() {
        val code = """
            ; Comment
            mov rax, 1
            add rbx, 2  ; End comment
        """.trimIndent()

        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val tokens = mutableListOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                tokens.add(lexer.tokenType.toString())
            }
            lexer.advance()
        }

        // Should have COMMENT, CRLF, instruction tokens, COMMENT
        assertTrue("Should contain comment tokens",
            tokens.any { it.contains("COMMENT") })
        assertTrue("Should contain CRLF tokens",
            tokens.any { it.contains("CRLF") })
    }

    fun testFloatFunctionTokenization() {
        val code = "__float32__(3.14)"
        val expected = listOf(
            "__float32__" to NasmTypes.FLOAT_FUNC,
            "(" to NasmTypes.LPAREN,
            "3.14" to NasmTypes.FLOAT,
            ")" to NasmTypes.RPAREN
        )

        assertTokenSequence(code, expected)
    }

    fun testStringFunctionTokenization() {
        val code = "__utf16__(\"Hello\")"
        val expected = listOf(
            "__utf16__" to NasmTypes.STRING_FUNC,
            "(" to NasmTypes.LPAREN,
            "\"Hello\"" to NasmTypes.STRING,
            ")" to NasmTypes.RPAREN
        )

        assertTokenSequence(code, expected)
    }

    fun testBuiltInFunctionTokenization() {
        val code = "__ilog2w__(1024)"
        val expected = listOf(
            "__ilog2w__" to NasmTypes.BUILTIN_FUNC,
            "(" to NasmTypes.LPAREN,
            "1024" to NasmTypes.NUMBER,
            ")" to NasmTypes.RPAREN
        )

        assertTokenSequence(code, expected)
    }

    fun testContextLocalLabelTokenization() {
        val code = "%\$label:"
        val expected = listOf(
            "%\$label" to NasmTypes.CONTEXT_LOCAL_REF,
            ":" to NasmTypes.COLON
        )

        assertTokenSequence(code, expected)
    }

    fun testMacroLocalLabelTokenization() {
        val code = "%%label:"
        val expected = listOf(
            "%%label" to NasmTypes.MACRO_LOCAL_REF,
            ":" to NasmTypes.COLON
        )

        assertTokenSequence(code, expected)
    }

    fun testAllNumericFormatsTokenization() {
        val code = "0x1234 1234h \$1234 1234 0d1234 1234d 0b1010 1010b 0o777 777o"

        val lexer = NasmLexerAdapter()
        lexer.start(code)

        var numberCount = 0
        while (lexer.tokenType != null) {
            if (lexer.tokenType == NasmTypes.NUMBER) {
                numberCount++
            }
            lexer.advance()
        }

        assertEquals("Should recognize 10 different number formats", 10, numberCount)
    }

    fun testOperatorPrecedenceTokenization() {
        val code = "1 + 2 * 3 << 4 & 5"
        val expected = listOf(
            "1" to NasmTypes.NUMBER,
            "+" to NasmTypes.PLUS,
            "2" to NasmTypes.NUMBER,
            "*" to NasmTypes.MUL,
            "3" to NasmTypes.NUMBER,
            "<<" to NasmTypes.LSHIFT,
            "4" to NasmTypes.NUMBER,
            "&" to NasmTypes.AMP,
            "5" to NasmTypes.NUMBER
        )

        assertTokenSequence(code, expected)
    }

    fun testAllComparisonOperatorsTokenization() {
        val code = "== != <> < > <= >= <=>"
        val expected = listOf(
            "==" to NasmTypes.EQ_EQ,
            "!=" to NasmTypes.NOT_EQUAL_1,
            "<>" to NasmTypes.NOT_EQUAL_2,
            "<" to NasmTypes.LT,
            ">" to NasmTypes.GT,
            "<=" to NasmTypes.LTE,
            ">=" to NasmTypes.GTE,
            "<=>" to NasmTypes.SPACESHIP
        )

        assertTokenSequence(code, expected)
    }

    fun testAllLogicalOperatorsTokenization() {
        val code = "&& || ^^"
        val expected = listOf(
            "&&" to NasmTypes.BOOLEAN_AND,
            "||" to NasmTypes.BOOLEAN_OR,
            "^^" to NasmTypes.BOOLEAN_XOR
        )

        assertTokenSequence(code, expected)
    }

    fun testSignedDivModTokenization() {
        val code = "a // b %% c"
        val expected = listOf(
            "a" to NasmTypes.IDENTIFIER,
            "//" to NasmTypes.SIGNED_DIV,
            "b" to NasmTypes.IDENTIFIER,
            "%%" to NasmTypes.SIGNED_MOD,
            "c" to NasmTypes.IDENTIFIER
        )

        assertTokenSequence(code, expected)
    }

    fun testDollarSymbolsTokenization() {
        val code = "\$ \$\$"
        val expected = listOf(
            "\$" to NasmTypes.DOLLAR,
            "\$\$" to NasmTypes.DOUBLE_DOLLAR
        )

        assertTokenSequence(code, expected)
    }

    fun testLocalLabelSyntaxTokenization() {
        val code = ".loop .. ..global"
        val expected = listOf(
            ".loop" to NasmTypes.IDENTIFIER,
            ".." to NasmTypes.DOT_DOT,
            "..global" to NasmTypes.IDENTIFIER
        )

        assertTokenSequence(code, expected)
    }

    fun testWrtSuffixTokenization() {
        val code = "wrt ..got ..plt ..sym ..tlsie"
        val expected = listOf(
            "wrt" to NasmTypes.WRT,
            "..got" to NasmTypes.WRT_SUFFIX,
            "..plt" to NasmTypes.WRT_SUFFIX,
            "..sym" to NasmTypes.WRT_SUFFIX,
            "..tlsie" to NasmTypes.WRT_SUFFIX
        )

        assertTokenSequence(code, expected)
    }

    fun testIncludeDirectiveTokenization() {
        val code = "%include \"macros.inc\""
        val expected = listOf(
            "%include" to NasmTypes.MACRO_INCLUDE,
            "\"macros.inc\"" to NasmTypes.STRING
        )

        assertTokenSequence(code, expected)
    }

    fun testCompleteProgram() {
        val code = """
            section .text
            global main

            main:
                mov rax, 1
                ret
        """.trimIndent()

        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val tokenTypes = mutableSetOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                tokenTypes.add(lexer.tokenType.toString())
            }
            lexer.advance()
        }

        // Verify we see all expected token types
        assertTrue("Should have SECTION_KW",
            tokenTypes.any { it.contains("SECTION_KW") })
        assertTrue("Should have GLOBAL_KW",
            tokenTypes.any { it.contains("GLOBAL_KW") })
        assertTrue("Should have IDENTIFIER",
            tokenTypes.any { it.contains("IDENTIFIER") })
        assertTrue("Should have REGISTER",
            tokenTypes.any { it.contains("REGISTER") })
        assertTrue("Should have COLON",
            tokenTypes.any { it.contains("COLON") })
        assertTrue("Should have CRLF",
            tokenTypes.any { it.contains("CRLF") })
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private fun assertTokenSequence(
        code: String,
        expected: List<Pair<String, com.intellij.psi.tree.IElementType>>
    ) {
        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val actual = mutableListOf<Pair<String, com.intellij.psi.tree.IElementType>>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                actual.add(lexer.tokenText to lexer.tokenType!!)
            }
            lexer.advance()
        }

        // Debug output on failure
        if (actual.size != expected.size || actual != expected) {
            println("\n=== Expected Tokens ===")
            expected.forEachIndexed { i, (text, type) ->
                println("$i: '$text' -> $type")
            }
            println("\n=== Actual Tokens ===")
            actual.forEachIndexed { i, (text, type) ->
                println("$i: '$text' -> $type")
            }
            println("=======================\n")
        }

        assertEquals("Token count mismatch for code: $code", expected.size, actual.size)

        for (i in expected.indices) {
            assertEquals(
                "Token text mismatch at position $i",
                expected[i].first,
                actual[i].first
            )
            assertEquals(
                "Token type mismatch at position $i for '${expected[i].first}'",
                expected[i].second,
                actual[i].second
            )
        }
    }
}
