package dev.agb.nasmplugin.lexer

import com.intellij.lexer.FlexAdapter

/**
 * Adapter for the JFlex-generated lexer
 */
class NasmLexerAdapter : FlexAdapter(NasmLexer(null))
