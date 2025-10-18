package dev.agb.nasmplugin.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Provides brace matching support for NASM assembly language.
 * Supports matching of:
 * - [ and ] for memory addressing
 * - %macro and %endmacro (structural braces)
 * - %if/%ifdef/%ifndef and %endif (structural braces)
 * - %rep and %endrep (structural braces)
 */
class NasmBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean =
        true // Allow braces to be typed anywhere

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int =
        openingBraceOffset // Return the offset of the opening brace for structural navigation

}

private val PAIRS = arrayOf(
    // Memory addressing brackets
    BracePair(NasmTypes.LBRACKET, NasmTypes.RBRACKET, false),

    // Macro definition blocks (structural)
    BracePair(NasmTypes.MACRO_START, NasmTypes.MACRO_END, true),

    // Conditional blocks (structural)
    BracePair(NasmTypes.MACRO_IF, NasmTypes.MACRO_ENDIF, true),

    // Repeat blocks (structural)
    BracePair(NasmTypes.MACRO_REP, NasmTypes.MACRO_ENDREP, true)
)
