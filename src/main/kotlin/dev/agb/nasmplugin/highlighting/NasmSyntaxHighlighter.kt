package dev.agb.nasmplugin.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.agb.nasmplugin.lexer.NasmLexerAdapter
import dev.agb.nasmplugin.psi.NasmTypes
import dev.agb.nasmplugin.psi.NasmTokenType

/**
 * Syntax highlighter for NASM assembly language
 */
class NasmSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = NasmLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        // Use the static token type constants from NasmTypes to avoid creating duplicate element types
        return when (tokenType) {
            NasmTypes.COMMENT -> COMMENT_KEYS

            // Registers
            NasmTypes.REGISTER, NasmTypes.SEG_REGISTER, NasmTypes.MASK_REG -> REGISTER_KEYS

            // Keywords and directives
            NasmTypes.EXTERN_KW, NasmTypes.EQU, NasmTypes.SECTION_KW, NasmTypes.SEGMENT_KW,
            NasmTypes.GLOBAL_KW, NasmTypes.BITS_KW, NasmTypes.ALIGN_KW, NasmTypes.ALIGNB_KW,
            NasmTypes.SECTALIGN_KW, NasmTypes.ORG_KW, NasmTypes.COMMON_KW, NasmTypes.CPU_KW,
            NasmTypes.FLOAT_KW, NasmTypes.INCBIN_KW, NasmTypes.USE16_KW, NasmTypes.USE32_KW,
            NasmTypes.USE64_KW, NasmTypes.ABSOLUTE_KW, NasmTypes.DEFAULT_KW, NasmTypes.STATIC_KW,
            NasmTypes.REQUIRED_KW, NasmTypes.STRUC_KW, NasmTypes.ENDSTRUC_KW, NasmTypes.ISTRUC_KW,
            NasmTypes.IEND_KW, NasmTypes.AT_KW, NasmTypes.MAP_KW -> DIRECTIVE_KEYS

            // Data directives
            NasmTypes.DATA_SIZE, NasmTypes.SPACE_SIZE -> DIRECTIVE_KEYS

            // Labels (colon)
            NasmTypes.COLON -> LABEL_KEYS

            // Numbers and floats
            NasmTypes.NUMBER, NasmTypes.FLOAT, NasmTypes.SPECIAL_FLOAT -> NUMBER_KEYS

            // Strings
            NasmTypes.STRING -> STRING_KEYS

            // Operators
            NasmTypes.PLUS, NasmTypes.MINUS, NasmTypes.MUL, NasmTypes.DIV, NasmTypes.MOD,
            NasmTypes.SIGNED_DIV, NasmTypes.SIGNED_MOD, NasmTypes.AMP, NasmTypes.PIPE,
            NasmTypes.CARET, NasmTypes.TILDE, NasmTypes.EXCLAIM, NasmTypes.EQ, NasmTypes.LT,
            NasmTypes.GT, NasmTypes.EQ_EQ, NasmTypes.NOT_EQUAL_1, NasmTypes.NOT_EQUAL_2,
            NasmTypes.LTE, NasmTypes.GTE, NasmTypes.SPACESHIP, NasmTypes.LSHIFT, NasmTypes.RSHIFT,
            NasmTypes.LSHIFT_COMPLETE, NasmTypes.RSHIFT_COMPLETE, NasmTypes.BOOLEAN_AND,
            NasmTypes.BOOLEAN_OR, NasmTypes.BOOLEAN_XOR -> OPERATOR_KEYS

            // Delimiters
            NasmTypes.LBRACKET, NasmTypes.RBRACKET, NasmTypes.COMMA, NasmTypes.LPAREN,
            NasmTypes.RPAREN, NasmTypes.LBRACE, NasmTypes.RBRACE -> OPERATOR_KEYS

            // Special symbols
            NasmTypes.DOLLAR, NasmTypes.DOUBLE_DOLLAR, NasmTypes.QUESTION_MARK -> OPERATOR_KEYS

            // Identifiers
            NasmTypes.IDENTIFIER -> IDENTIFIER_KEYS

            // Preprocessor/macro directives
            NasmTypes.MACRO_START, NasmTypes.MACRO_END, NasmTypes.MACRO_DEFINE, NasmTypes.MACRO_ASSIGN,
            NasmTypes.MACRO_IF, NasmTypes.MACRO_IFDEF, NasmTypes.MACRO_IFNDEF, NasmTypes.MACRO_ELIF,
            NasmTypes.MACRO_ELSE, NasmTypes.MACRO_ENDIF, NasmTypes.MACRO_REP, NasmTypes.MACRO_ENDREP,
            NasmTypes.MACRO_WHILE, NasmTypes.MACRO_ENDWHILE, NasmTypes.MACRO_INCLUDE, NasmTypes.MACRO_USE,
            NasmTypes.MACRO_PUSH, NasmTypes.MACRO_POP, NasmTypes.MACRO_REPL, NasmTypes.MACRO_UNDEF,
            NasmTypes.MACRO_UNMACRO, NasmTypes.MACRO_ERROR, NasmTypes.MACRO_ROTATE, NasmTypes.MACRO_STRLEN,
            NasmTypes.MACRO_SUBSTR, NasmTypes.MACRO_STRCAT, NasmTypes.MACRO_PATHSEARCH, NasmTypes.MACRO_DEPEND,
            NasmTypes.MACRO_ARG_DECL, NasmTypes.MACRO_STACKSIZE, NasmTypes.MACRO_LOCAL, NasmTypes.MACRO_LINE,
            NasmTypes.MACRO_PRAGMA, NasmTypes.MACRO_CLEAR, NasmTypes.MACRO_ALIASES,
            NasmTypes.PREPROCESSOR_DIRECTIVE -> MACRO_KEYS

            // Macro expansions and references
            NasmTypes.MACRO_PARAM, NasmTypes.MACRO_PARAM_GREEDY, NasmTypes.MACRO_PARAM_REVERSE,
            NasmTypes.MACRO_EXPANSION_START, NasmTypes.MACRO_EXPLICIT_START, NasmTypes.MACRO_LOCAL_REF,
            NasmTypes.CONTEXT_LOCAL_REF, NasmTypes.ENV_VAR_PREFIX, NasmTypes.PASTE_OP -> MACRO_KEYS

            // Modifiers and keywords
            NasmTypes.DUP, NasmTypes.STRICT, NasmTypes.REL, NasmTypes.WRT, NasmTypes.SEG,
            NasmTypes.SIZE_SPEC, NasmTypes.TIMES, NasmTypes.SECTION_ATTR_KW -> MODIFIER_KEYS

            // Built-in functions
            NasmTypes.BUILTIN_FUNC, NasmTypes.STRING_FUNC, NasmTypes.FLOAT_FUNC,
            NasmTypes.DEFINED, NasmTypes.QUERY, NasmTypes.QUERY_EXPAND -> MACRO_KEYS

            // AVX-512 decorators
            NasmTypes.ZEROING, NasmTypes.SAE, NasmTypes.ROUNDING, NasmTypes.BROADCAST -> MODIFIER_KEYS

            // Instruction prefixes
            NasmTypes.INSTRUCTION_PREFIX -> INSTRUCTION_PREFIX_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }

    companion object {
        // Define text attribute keys for different token types
        @JvmField
        val COMMENT = createTextAttributesKey("NASM_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        @JvmField
        val INSTRUCTION = createTextAttributesKey("NASM_INSTRUCTION", DefaultLanguageHighlighterColors.KEYWORD)

        @JvmField
        val REGISTER = createTextAttributesKey("NASM_REGISTER", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

        @JvmField
        val DIRECTIVE = createTextAttributesKey("NASM_DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD)

        @JvmField
        val LABEL = createTextAttributesKey("NASM_LABEL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)

        @JvmField
        val NUMBER = createTextAttributesKey("NASM_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

        @JvmField
        val STRING = createTextAttributesKey("NASM_STRING", DefaultLanguageHighlighterColors.STRING)

        @JvmField
        val OPERATOR = createTextAttributesKey("NASM_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)

        @JvmField
        val IDENTIFIER = createTextAttributesKey("NASM_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)

        @JvmField
        val MACRO = createTextAttributesKey("NASM_MACRO", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val MACRO_INVOCATION = createTextAttributesKey("NASM_MACRO_NAME", DefaultLanguageHighlighterColors.CONSTANT)

        @JvmField
        val MODIFIER = createTextAttributesKey("NASM_MODIFIER", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val INSTRUCTION_PREFIX = createTextAttributesKey("NASM_INSTRUCTION_PREFIX", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val BAD_CHARACTER = createTextAttributesKey("NASM_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val INSTRUCTION_KEYS = arrayOf(INSTRUCTION)
        private val REGISTER_KEYS = arrayOf(REGISTER)
        private val DIRECTIVE_KEYS = arrayOf(DIRECTIVE)
        private val LABEL_KEYS = arrayOf(LABEL)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val STRING_KEYS = arrayOf(STRING)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val MACRO_KEYS = arrayOf(MACRO)
        private val MODIFIER_KEYS = arrayOf(MODIFIER)
        private val INSTRUCTION_PREFIX_KEYS = arrayOf(INSTRUCTION_PREFIX)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
