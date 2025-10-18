package dev.agb.nasmplugin.lexer

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes

class SectionAttributeLexerTest : BasePlatformTestCase() {

    fun testSectionAttributes() {
        val code = """
            section .text align=4096 exec
            section .bss nobits align=32
            section .rodata align=8 alloc nowrite
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val tokens = mutableListOf<Pair<String, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenText to lexer.tokenType.toString())
            lexer.advance()
        }

        // Debug output
        println("=== Tokens Found ===")
        tokens.forEach { (text, type) ->
            println("'$text' -> $type")
        }
        println("===================")

        // Verify that section attribute keywords are recognized
        assertTrue("Should recognize 'exec' as SECTION_ATTR_KW",
            tokens.any { it.first == "exec" && it.second == "NasmTokenType.SECTION_ATTR_KW" })
        assertTrue("Should recognize 'nobits' as SECTION_ATTR_KW",
            tokens.any { it.first == "nobits" && it.second == "NasmTokenType.SECTION_ATTR_KW" })
        assertTrue("Should recognize 'alloc' as SECTION_ATTR_KW",
            tokens.any { it.first == "alloc" && it.second == "NasmTokenType.SECTION_ATTR_KW" })
        assertTrue("Should recognize 'nowrite' as SECTION_ATTR_KW",
            tokens.any { it.first == "nowrite" && it.second == "NasmTokenType.SECTION_ATTR_KW" })
    }
}