package dev.agb.nasmplugin.lexer

import com.intellij.psi.TokenType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Comprehensive lexer tests for the NASM plugin.
 * Tests tokenization of all major NASM token types including keywords,
 * registers, operators, literals, and preprocessor directives.
 */
class LexerTests : BasePlatformTestCase() {

    // ============================================================================
    // KEYWORD TESTS
    // ============================================================================

    fun testDataDirectives() {
        assertTokens(
            "db dw dd dq dt do dy dz",
            listOf(
                "db" to NasmTypes.DATA_SIZE,
                "dw" to NasmTypes.DATA_SIZE,
                "dd" to NasmTypes.DATA_SIZE,
                "dq" to NasmTypes.DATA_SIZE,
                "dt" to NasmTypes.DATA_SIZE,
                "do" to NasmTypes.DATA_SIZE,
                "dy" to NasmTypes.DATA_SIZE,
                "dz" to NasmTypes.DATA_SIZE
            )
        )
    }

    fun testDataDirectivesCaseInsensitive() {
        assertTokens(
            "DB Db dB",
            listOf(
                "DB" to NasmTypes.DATA_SIZE,
                "Db" to NasmTypes.DATA_SIZE,
                "dB" to NasmTypes.DATA_SIZE
            )
        )
    }

    fun testReserveDirectives() {
        assertTokens(
            "resb resw resd resq rest reso resy resz",
            listOf(
                "resb" to NasmTypes.SPACE_SIZE,
                "resw" to NasmTypes.SPACE_SIZE,
                "resd" to NasmTypes.SPACE_SIZE,
                "resq" to NasmTypes.SPACE_SIZE,
                "rest" to NasmTypes.SPACE_SIZE,
                "reso" to NasmTypes.SPACE_SIZE,
                "resy" to NasmTypes.SPACE_SIZE,
                "resz" to NasmTypes.SPACE_SIZE
            )
        )
    }

    fun testAssemblerKeywords() {
        assertTokens(
            "equ section segment global extern common",
            listOf(
                "equ" to NasmTypes.EQU,
                "section" to NasmTypes.SECTION_KW,
                "segment" to NasmTypes.SEGMENT_KW,
                "global" to NasmTypes.GLOBAL_KW,
                "extern" to NasmTypes.EXTERN_KW,
                "common" to NasmTypes.COMMON_KW
            )
        )
    }

    fun testDirectiveKeywords() {
        assertTokens(
            "bits align org cpu default",
            listOf(
                "bits" to NasmTypes.BITS_KW,
                "align" to NasmTypes.ALIGN_KW,
                "org" to NasmTypes.ORG_KW,
                "cpu" to NasmTypes.CPU_KW,
                "default" to NasmTypes.DEFAULT_KW
            )
        )
    }

    fun testSpecialKeywords() {
        assertTokens(
            "wrt seg dup strict rel abs times",
            listOf(
                "wrt" to NasmTypes.WRT,
                "seg" to NasmTypes.SEG,
                "dup" to NasmTypes.DUP,
                "strict" to NasmTypes.STRICT,
                "rel" to NasmTypes.REL,
                "abs" to NasmTypes.ABS,
                "times" to NasmTypes.TIMES
            )
        )
    }

    fun testStructureKeywords() {
        assertTokens(
            "struc endstruc istruc iend at",
            listOf(
                "struc" to NasmTypes.STRUC_KW,
                "endstruc" to NasmTypes.ENDSTRUC_KW,
                "istruc" to NasmTypes.ISTRUC_KW,
                "iend" to NasmTypes.IEND_KW,
                "at" to NasmTypes.AT_KW
            )
        )
    }

    // ============================================================================
    // REGISTER TESTS
    // ============================================================================

    fun testGeneralPurposeRegisters64() {
        assertTokens(
            "rax rbx rcx rdx rsi rdi rsp rbp",
            listOf(
                "rax" to NasmTypes.REGISTER,
                "rbx" to NasmTypes.REGISTER,
                "rcx" to NasmTypes.REGISTER,
                "rdx" to NasmTypes.REGISTER,
                "rsi" to NasmTypes.REGISTER,
                "rdi" to NasmTypes.REGISTER,
                "rsp" to NasmTypes.REGISTER,
                "rbp" to NasmTypes.REGISTER
            )
        )
    }

    fun testGeneralPurposeRegisters32() {
        assertTokens(
            "eax ebx ecx edx esi edi esp ebp",
            listOf(
                "eax" to NasmTypes.REGISTER,
                "ebx" to NasmTypes.REGISTER,
                "ecx" to NasmTypes.REGISTER,
                "edx" to NasmTypes.REGISTER,
                "esi" to NasmTypes.REGISTER,
                "edi" to NasmTypes.REGISTER,
                "esp" to NasmTypes.REGISTER,
                "ebp" to NasmTypes.REGISTER
            )
        )
    }

    fun testGeneralPurposeRegisters16() {
        assertTokens(
            "ax bx cx dx si di sp bp",
            listOf(
                "ax" to NasmTypes.REGISTER,
                "bx" to NasmTypes.REGISTER,
                "cx" to NasmTypes.REGISTER,
                "dx" to NasmTypes.REGISTER,
                "si" to NasmTypes.REGISTER,
                "di" to NasmTypes.REGISTER,
                "sp" to NasmTypes.REGISTER,
                "bp" to NasmTypes.REGISTER
            )
        )
    }

    fun testGeneralPurposeRegisters8() {
        assertTokens(
            "al ah bl bh cl ch dl dh",
            listOf(
                "al" to NasmTypes.REGISTER,
                "ah" to NasmTypes.REGISTER,
                "bl" to NasmTypes.REGISTER,
                "bh" to NasmTypes.REGISTER,
                "cl" to NasmTypes.REGISTER,
                "ch" to NasmTypes.REGISTER,
                "dl" to NasmTypes.REGISTER,
                "dh" to NasmTypes.REGISTER
            )
        )
    }

    fun testExtendedRegisters() {
        assertTokens(
            "r8 r9 r10 r11 r12 r13 r14 r15",
            listOf(
                "r8" to NasmTypes.REGISTER,
                "r9" to NasmTypes.REGISTER,
                "r10" to NasmTypes.REGISTER,
                "r11" to NasmTypes.REGISTER,
                "r12" to NasmTypes.REGISTER,
                "r13" to NasmTypes.REGISTER,
                "r14" to NasmTypes.REGISTER,
                "r15" to NasmTypes.REGISTER
            )
        )
    }

    fun testSegmentRegisters() {
        assertTokens(
            "cs ds es fs gs ss",
            listOf(
                "cs" to NasmTypes.SEG_REGISTER,
                "ds" to NasmTypes.SEG_REGISTER,
                "es" to NasmTypes.SEG_REGISTER,
                "fs" to NasmTypes.SEG_REGISTER,
                "gs" to NasmTypes.SEG_REGISTER,
                "ss" to NasmTypes.SEG_REGISTER
            )
        )
    }

    fun testXMMRegisters() {
        assertTokens(
            "xmm0 xmm1 xmm15 xmm31",
            listOf(
                "xmm0" to NasmTypes.REGISTER,
                "xmm1" to NasmTypes.REGISTER,
                "xmm15" to NasmTypes.REGISTER,
                "xmm31" to NasmTypes.REGISTER
            )
        )
    }

    fun testYMMRegisters() {
        assertTokens(
            "ymm0 ymm1 ymm15 ymm31",
            listOf(
                "ymm0" to NasmTypes.REGISTER,
                "ymm1" to NasmTypes.REGISTER,
                "ymm15" to NasmTypes.REGISTER,
                "ymm31" to NasmTypes.REGISTER
            )
        )
    }

    fun testZMMRegisters() {
        assertTokens(
            "zmm0 zmm1 zmm15 zmm31",
            listOf(
                "zmm0" to NasmTypes.REGISTER,
                "zmm1" to NasmTypes.REGISTER,
                "zmm15" to NasmTypes.REGISTER,
                "zmm31" to NasmTypes.REGISTER
            )
        )
    }

    fun testMaskRegisters() {
        assertTokens(
            "k0 k1 k2 k3 k4 k5 k6 k7",
            listOf(
                "k0" to NasmTypes.MASK_REG,
                "k1" to NasmTypes.MASK_REG,
                "k2" to NasmTypes.MASK_REG,
                "k3" to NasmTypes.MASK_REG,
                "k4" to NasmTypes.MASK_REG,
                "k5" to NasmTypes.MASK_REG,
                "k6" to NasmTypes.MASK_REG,
                "k7" to NasmTypes.MASK_REG
            )
        )
    }

    fun testRegistersCaseInsensitive() {
        assertTokens(
            "RAX Rax rAx",
            listOf(
                "RAX" to NasmTypes.REGISTER,
                "Rax" to NasmTypes.REGISTER,
                "rAx" to NasmTypes.REGISTER
            )
        )
    }

    // ============================================================================
    // SIZE SPECIFIER TESTS
    // ============================================================================

    fun testSizeSpecifiers() {
        assertTokens(
            "byte word dword qword tword oword yword zword",
            listOf(
                "byte" to NasmTypes.SIZE_SPEC,
                "word" to NasmTypes.SIZE_SPEC,
                "dword" to NasmTypes.SIZE_SPEC,
                "qword" to NasmTypes.SIZE_SPEC,
                "tword" to NasmTypes.SIZE_SPEC,
                "oword" to NasmTypes.SIZE_SPEC,
                "yword" to NasmTypes.SIZE_SPEC,
                "zword" to NasmTypes.SIZE_SPEC
            )
        )
    }

    fun testPointerSpecifiers() {
        assertTokens(
            "ptr near far short",
            listOf(
                "ptr" to NasmTypes.SIZE_SPEC,
                "near" to NasmTypes.SIZE_SPEC,
                "far" to NasmTypes.SIZE_SPEC,
                "short" to NasmTypes.SIZE_SPEC
            )
        )
    }

    // ============================================================================
    // PREPROCESSOR DIRECTIVE TESTS
    // ============================================================================

    fun testMacroDirectives() {
        assertTokens(
            "%macro %endmacro %define %assign",
            listOf(
                "%macro" to NasmTypes.MACRO_START,
                "%endmacro" to NasmTypes.MACRO_END,
                "%define" to NasmTypes.MACRO_DEFINE,
                "%assign" to NasmTypes.MACRO_ASSIGN
            )
        )
    }

    fun testConditionalDirectives() {
        assertTokens(
            "%if %ifdef %ifndef %elif %else %endif",
            listOf(
                "%if" to NasmTypes.MACRO_IF,
                "%ifdef" to NasmTypes.MACRO_IFDEF,
                "%ifndef" to NasmTypes.MACRO_IFNDEF,
                "%elif" to NasmTypes.MACRO_ELIF,
                "%else" to NasmTypes.MACRO_ELSE,
                "%endif" to NasmTypes.MACRO_ENDIF
            )
        )
    }

    fun testRepetitionDirectives() {
        assertTokens(
            "%rep %endrep %exitrep",
            listOf(
                "%rep" to NasmTypes.MACRO_REP,
                "%endrep" to NasmTypes.MACRO_ENDREP,
                "%exitrep" to NasmTypes.MACRO_EXITREP
            )
        )
    }

    fun testIncludeDirective() {
        assertTokens(
            "%include",
            listOf(
                "%include" to NasmTypes.MACRO_INCLUDE
            )
        )
    }

    fun testContextDirectives() {
        assertTokens(
            "%push %pop %repl",
            listOf(
                "%push" to NasmTypes.MACRO_PUSH,
                "%pop" to NasmTypes.MACRO_POP,
                "%repl" to NasmTypes.MACRO_REPL
            )
        )
    }

    fun testStringDirectives() {
        assertTokens(
            "%strlen %substr %strcat",
            listOf(
                "%strlen" to NasmTypes.MACRO_STRLEN,
                "%substr" to NasmTypes.MACRO_SUBSTR,
                "%strcat" to NasmTypes.MACRO_STRCAT
            )
        )
    }

    fun testPreprocessorDirectivesCaseInsensitive() {
        assertTokens(
            "%MACRO %Macro %macro",
            listOf(
                "%MACRO" to NasmTypes.MACRO_START,
                "%Macro" to NasmTypes.MACRO_START,
                "%macro" to NasmTypes.MACRO_START
            )
        )
    }

    // ============================================================================
    // PREPROCESSOR TOKEN TESTS
    // ============================================================================

    fun testMacroParameters() {
        assertTokens(
            "%0 %1 %2 %9",
            listOf(
                "%0" to NasmTypes.MACRO_PARAM,
                "%1" to NasmTypes.MACRO_PARAM,
                "%2" to NasmTypes.MACRO_PARAM,
                "%9" to NasmTypes.MACRO_PARAM
            )
        )
    }

    fun testMacroParameterGreedy() {
        assertTokens(
            "%+1 %+2 %+10",
            listOf(
                "%+1" to NasmTypes.MACRO_PARAM_GREEDY,
                "%+2" to NasmTypes.MACRO_PARAM_GREEDY,
                "%+10" to NasmTypes.MACRO_PARAM_GREEDY
            )
        )
    }

    fun testMacroParameterReverse() {
        assertTokens(
            "%-1 %-2 %-10",
            listOf(
                "%-1" to NasmTypes.MACRO_PARAM_REVERSE,
                "%-2" to NasmTypes.MACRO_PARAM_REVERSE,
                "%-10" to NasmTypes.MACRO_PARAM_REVERSE
            )
        )
    }

    fun testMacroExpansionStart() {
        assertTokens(
            "%[",
            listOf(
                "%[" to NasmTypes.MACRO_EXPANSION_START
            )
        )
    }

    fun testMacroExplicitStart() {
        assertTokens(
            "%{",
            listOf(
                "%{" to NasmTypes.MACRO_EXPLICIT_START
            )
        )
    }

    fun testContextLocalIdentifier() {
        assertTokens(
            "%\$label %\$loop",
            listOf(
                "%\$label" to NasmTypes.CONTEXT_LOCAL_REF,
                "%\$loop" to NasmTypes.CONTEXT_LOCAL_REF
            )
        )
    }

    fun testMacroLocalIdentifier() {
        assertTokens(
            "%%label %%loop",
            listOf(
                "%%label" to NasmTypes.MACRO_LOCAL_REF,
                "%%loop" to NasmTypes.MACRO_LOCAL_REF
            )
        )
    }

    fun testEnvironmentVariablePrefix() {
        assertTokens(
            "%!",
            listOf(
                "%!" to NasmTypes.ENV_VAR_PREFIX
            )
        )
    }

    fun testQueryOperators() {
        assertTokens(
            "%? %??",
            listOf(
                "%?" to NasmTypes.QUERY,
                "%??" to NasmTypes.QUERY_EXPAND
            )
        )
    }

    fun testPasteOperator() {
        assertTokens(
            "%+",
            listOf(
                "%+" to NasmTypes.PASTE_OP
            )
        )
    }

    // ============================================================================
    // OPERATOR TESTS
    // ============================================================================

    fun testArithmeticOperators() {
        assertTokens(
            "+ - * /",
            listOf(
                "+" to NasmTypes.PLUS,
                "-" to NasmTypes.MINUS,
                "*" to NasmTypes.MUL,
                "/" to NasmTypes.DIV
            )
        )
    }

    fun testBitwiseOperators() {
        assertTokens(
            "& | ^ ~ %",
            listOf(
                "&" to NasmTypes.AMP,
                "|" to NasmTypes.PIPE,
                "^" to NasmTypes.CARET,
                "~" to NasmTypes.TILDE,
                "%" to NasmTypes.MOD
            )
        )
    }

    fun testShiftOperators() {
        assertTokens(
            "<< >> <<< >>>",
            listOf(
                "<<" to NasmTypes.LSHIFT,
                ">>" to NasmTypes.RSHIFT,
                "<<<" to NasmTypes.LSHIFT_COMPLETE,
                ">>>" to NasmTypes.RSHIFT_COMPLETE
            )
        )
    }

    fun testComparisonOperators() {
        assertTokens(
            "== != <> < > <= >= <=>",
            listOf(
                "==" to NasmTypes.EQ_EQ,
                "!=" to NasmTypes.NOT_EQUAL_1,
                "<>" to NasmTypes.NOT_EQUAL_2,
                "<" to NasmTypes.LT,
                ">" to NasmTypes.GT,
                "<=" to NasmTypes.LTE,
                ">=" to NasmTypes.GTE,
                "<=>" to NasmTypes.SPACESHIP
            )
        )
    }

    fun testLogicalOperators() {
        assertTokens(
            "&& || ^^",
            listOf(
                "&&" to NasmTypes.BOOLEAN_AND,
                "||" to NasmTypes.BOOLEAN_OR,
                "^^" to NasmTypes.BOOLEAN_XOR
            )
        )
    }

    fun testSignedOperators() {
        assertTokens(
            "// %%",
            listOf(
                "//" to NasmTypes.SIGNED_DIV,
                "%%" to NasmTypes.SIGNED_MOD
            )
        )
    }

    // ============================================================================
    // DELIMITER TESTS
    // ============================================================================

    fun testDelimiters() {
        assertTokens(
            "( ) [ ] { } , : = !",
            listOf(
                "(" to NasmTypes.LPAREN,
                ")" to NasmTypes.RPAREN,
                "[" to NasmTypes.LBRACKET,
                "]" to NasmTypes.RBRACKET,
                "{" to NasmTypes.LBRACE,
                "}" to NasmTypes.RBRACE,
                "," to NasmTypes.COMMA,
                ":" to NasmTypes.COLON,
                "=" to NasmTypes.EQ,
                "!" to NasmTypes.EXCLAIM
            )
        )
    }

    fun testSpecialSymbols() {
        assertTokens(
            "\$ \$\$ ? ..",
            listOf(
                "\$" to NasmTypes.DOLLAR,
                "\$\$" to NasmTypes.DOUBLE_DOLLAR,
                "?" to NasmTypes.QUESTION_MARK,
                ".." to NasmTypes.DOT_DOT
            )
        )
    }

    // ============================================================================
    // NUMERIC LITERAL TESTS
    // ============================================================================

    fun testDecimalNumbers() {
        assertTokenSequence(
            "123 0d456 789d",
            listOf(
                NasmTypes.NUMBER,
                NasmTypes.NUMBER,
                NasmTypes.NUMBER
            )
        )
    }

    fun testHexadecimalNumbers() {
        assertTokenSequence(
            "0x1234 1234h \$ABCD",
            listOf(
                NasmTypes.NUMBER,
                NasmTypes.NUMBER,
                NasmTypes.NUMBER
            )
        )
    }

    fun testBinaryNumbers() {
        assertTokenSequence(
            "0b1010 1010b 1010y",
            listOf(
                NasmTypes.NUMBER,
                NasmTypes.NUMBER,
                NasmTypes.NUMBER
            )
        )
    }

    fun testOctalNumbers() {
        assertTokenSequence(
            "0o777 777o 777q",
            listOf(
                NasmTypes.NUMBER,
                NasmTypes.NUMBER,
                NasmTypes.NUMBER
            )
        )
    }

    fun testNumbersWithUnderscores() {
        assertTokenSequence(
            "1_000_000 0xFF_FF_FF_FF 0b1010_1010",
            listOf(
                NasmTypes.NUMBER,
                NasmTypes.NUMBER,
                NasmTypes.NUMBER
            )
        )
    }

    fun testFloatLiterals() {
        assertTokenSequence(
            "3.14 2.71828 1.0e10 .5",
            listOf(
                NasmTypes.FLOAT,
                NasmTypes.FLOAT,
                NasmTypes.FLOAT,
                NasmTypes.FLOAT
            )
        )
    }

    // ============================================================================
    // STRING LITERAL TESTS
    // ============================================================================

    fun testDoubleQuotedStrings() {
        assertTokenSequence(
            "\"Hello\" \"World\"",
            listOf(
                NasmTypes.STRING,
                NasmTypes.STRING
            )
        )
    }

    fun testSingleQuotedStrings() {
        assertTokenSequence(
            "'Hello' 'World'",
            listOf(
                NasmTypes.STRING,
                NasmTypes.STRING
            )
        )
    }

    fun testBacktickStrings() {
        assertTokenSequence(
            "`Hello` `World`",
            listOf(
                NasmTypes.STRING,
                NasmTypes.STRING
            )
        )
    }

    fun testStringsWithEscapes() {
        assertTokenSequence(
            "\"Hello\\n\" \"Tab\\t\"",
            listOf(
                NasmTypes.STRING,
                NasmTypes.STRING
            )
        )
    }

    // ============================================================================
    // IDENTIFIER TESTS
    // ============================================================================

    fun testSimpleIdentifiers() {
        assertTokenSequence(
            "label main loop",
            listOf(
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER
            )
        )
    }

    fun testIdentifiersWithSpecialChars() {
        assertTokenSequence(
            "\$start _loop .data",
            listOf(
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER
            )
        )
    }

    fun testIdentifiersWithNumbers() {
        assertTokenSequence(
            "label1 loop2 data3",
            listOf(
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER,
                NasmTypes.IDENTIFIER
            )
        )
    }

    // ============================================================================
    // COMMENT TESTS
    // ============================================================================

    fun testSemicolonComments() {
        assertTokenSequence(
            "; This is a comment\nmov rax, 1",
            listOf(
                NasmTypes.COMMENT,
                NasmTypes.CRLF,
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rax
                NasmTypes.COMMA,
                NasmTypes.NUMBER
            )
        )
    }

    // Note: C++ style comments (//) are NOT standard NASM
    // The // operator is signed division, not a comment

    fun testMultiLineComments() {
        val code = "/* Comment */\nmov rax, 1"
        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val tokens = mutableListOf<Pair<String, String>>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                tokens.add(lexer.tokenText to lexer.tokenType.toString())
            }
            lexer.advance()
        }

        assertTrue("Should recognize multi-line comment",
            tokens.any { it.second.contains("COMMENT") })
    }

    // ============================================================================
    // INSTRUCTION PREFIX TESTS
    // ============================================================================

    fun testInstructionPrefixes() {
        assertTokens(
            "lock rep repe repne",
            listOf(
                "lock" to NasmTypes.INSTRUCTION_PREFIX,
                "rep" to NasmTypes.INSTRUCTION_PREFIX,
                "repe" to NasmTypes.INSTRUCTION_PREFIX,
                "repne" to NasmTypes.INSTRUCTION_PREFIX
            )
        )
    }

    fun testSizeOverridePrefixes() {
        assertTokens(
            "o16 o32 o64 a16 a32 a64",
            listOf(
                "o16" to NasmTypes.INSTRUCTION_PREFIX,
                "o32" to NasmTypes.INSTRUCTION_PREFIX,
                "o64" to NasmTypes.INSTRUCTION_PREFIX,
                "a16" to NasmTypes.INSTRUCTION_PREFIX,
                "a32" to NasmTypes.INSTRUCTION_PREFIX,
                "a64" to NasmTypes.INSTRUCTION_PREFIX
            )
        )
    }

    // ============================================================================
    // AVX-512 DECORATOR TESTS
    // ============================================================================

    fun testAVX512Decorators() {
        assertTokens(
            "z sae",
            listOf(
                "z" to NasmTypes.ZEROING,
                "sae" to NasmTypes.SAE
            )
        )
    }

    fun testAVX512Rounding() {
        assertTokens(
            "rn-sae rd-sae ru-sae rz-sae",
            listOf(
                "rn-sae" to NasmTypes.ROUNDING,
                "rd-sae" to NasmTypes.ROUNDING,
                "ru-sae" to NasmTypes.ROUNDING,
                "rz-sae" to NasmTypes.ROUNDING
            )
        )
    }

    fun testAVX512Broadcast() {
        assertTokens(
            "1to2 1to4 1to8 1to16 1to32",
            listOf(
                "1to2" to NasmTypes.BROADCAST,
                "1to4" to NasmTypes.BROADCAST,
                "1to8" to NasmTypes.BROADCAST,
                "1to16" to NasmTypes.BROADCAST,
                "1to32" to NasmTypes.BROADCAST
            )
        )
    }

    // ============================================================================
    // BUILT-IN FUNCTION TESTS
    // ============================================================================

    fun testBuiltInFunctions() {
        assertTokens(
            "__ilog2e__ __ilog2w__ __ilog2f__ __ilog2c__",
            listOf(
                "__ilog2e__" to NasmTypes.BUILTIN_FUNC,
                "__ilog2w__" to NasmTypes.BUILTIN_FUNC,
                "__ilog2f__" to NasmTypes.BUILTIN_FUNC,
                "__ilog2c__" to NasmTypes.BUILTIN_FUNC
            )
        )
    }

    fun testStringFunctions() {
        assertTokens(
            "__utf16__ __utf32__ __utf16le__",
            listOf(
                "__utf16__" to NasmTypes.STRING_FUNC,
                "__utf32__" to NasmTypes.STRING_FUNC,
                "__utf16le__" to NasmTypes.STRING_FUNC
            )
        )
    }

    fun testFloatFunctions() {
        assertTokens(
            "__float8__ __float16__ __float32__ __float64__",
            listOf(
                "__float8__" to NasmTypes.FLOAT_FUNC,
                "__float16__" to NasmTypes.FLOAT_FUNC,
                "__float32__" to NasmTypes.FLOAT_FUNC,
                "__float64__" to NasmTypes.FLOAT_FUNC
            )
        )
    }

    fun testSpecialFloats() {
        assertTokens(
            "__infinity__ __nan__ __qnan__ __snan__",
            listOf(
                "__infinity__" to NasmTypes.SPECIAL_FLOAT,
                "__nan__" to NasmTypes.SPECIAL_FLOAT,
                "__qnan__" to NasmTypes.SPECIAL_FLOAT,
                "__snan__" to NasmTypes.SPECIAL_FLOAT
            )
        )
    }

    fun testDefined() {
        assertTokens(
            "defined DEFINED",
            listOf(
                "defined" to NasmTypes.DEFINED,
                "DEFINED" to NasmTypes.DEFINED
            )
        )
    }

    // ============================================================================
    // SECTION ATTRIBUTE TESTS
    // ============================================================================

    fun testSectionAttributes() {
        assertTokens(
            "exec noexec alloc noalloc write nowrite",
            listOf(
                "exec" to NasmTypes.SECTION_ATTR_KW,
                "noexec" to NasmTypes.SECTION_ATTR_KW,
                "alloc" to NasmTypes.SECTION_ATTR_KW,
                "noalloc" to NasmTypes.SECTION_ATTR_KW,
                "write" to NasmTypes.SECTION_ATTR_KW,
                "nowrite" to NasmTypes.SECTION_ATTR_KW
            )
        )
    }

    fun testSectionTypeAttributes() {
        assertTokens(
            "nobits progbits tls notls",
            listOf(
                "nobits" to NasmTypes.SECTION_ATTR_KW,
                "progbits" to NasmTypes.SECTION_ATTR_KW,
                "tls" to NasmTypes.SECTION_ATTR_KW,
                "notls" to NasmTypes.SECTION_ATTR_KW
            )
        )
    }

    // ============================================================================
    // WRT SUFFIX TESTS
    // ============================================================================

    fun testWrtSuffixes() {
        assertTokens(
            "..got ..plt ..sym ..tlsie",
            listOf(
                "..got" to NasmTypes.WRT_SUFFIX,
                "..plt" to NasmTypes.WRT_SUFFIX,
                "..sym" to NasmTypes.WRT_SUFFIX,
                "..tlsie" to NasmTypes.WRT_SUFFIX
            )
        )
    }

    // ============================================================================
    // NEWLINE TESTS
    // ============================================================================

    fun testUnixNewline() {
        assertTokenSequence(
            "mov rax, 1\nmov rbx, 2",
            listOf(
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rax
                NasmTypes.COMMA,
                NasmTypes.NUMBER,
                NasmTypes.CRLF,
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rbx
                NasmTypes.COMMA,
                NasmTypes.NUMBER
            )
        )
    }

    fun testWindowsNewline() {
        assertTokenSequence(
            "mov rax, 1\r\nmov rbx, 2",
            listOf(
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rax
                NasmTypes.COMMA,
                NasmTypes.NUMBER,
                NasmTypes.CRLF,
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rbx
                NasmTypes.COMMA,
                NasmTypes.NUMBER
            )
        )
    }

    fun testMacNewline() {
        assertTokenSequence(
            "mov rax, 1\rmov rbx, 2",
            listOf(
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rax
                NasmTypes.COMMA,
                NasmTypes.NUMBER,
                NasmTypes.CRLF,
                NasmTypes.IDENTIFIER,  // mov
                NasmTypes.REGISTER,     // rbx
                NasmTypes.COMMA,
                NasmTypes.NUMBER
            )
        )
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    fun testEmptyInput() {
        val lexer = NasmLexerAdapter()
        lexer.start("")
        assertNull("Empty input should produce no tokens", lexer.tokenType)
    }

    fun testWhitespaceOnly() {
        val lexer = NasmLexerAdapter()
        lexer.start("   \t  \t  ")

        val tokens = collectTokens(lexer)
        assertTrue("Whitespace-only input should have only whitespace tokens",
            tokens.all { it.second == TokenType.WHITE_SPACE })
    }

    fun testConsecutiveOperators() {
        assertTokenSequence(
            "++--**//",
            listOf(
                NasmTypes.PLUS,
                NasmTypes.PLUS,
                NasmTypes.MINUS,
                NasmTypes.MINUS,
                NasmTypes.MUL,
                NasmTypes.MUL,
                NasmTypes.SIGNED_DIV
            )
        )
    }

    fun testBadCharacters() {
        val lexer = NasmLexerAdapter()
        lexer.start("@#")

        val tokens = collectTokens(lexer)
        assertTrue("Bad characters should produce BAD_CHARACTER tokens",
            tokens.any { it.second == TokenType.BAD_CHARACTER })
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private fun assertTokens(code: String, expectedTokens: List<Pair<String, com.intellij.psi.tree.IElementType>>) {
        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val actualTokens = mutableListOf<Pair<String, com.intellij.psi.tree.IElementType>>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                actualTokens.add(lexer.tokenText to lexer.tokenType!!)
            }
            lexer.advance()
        }

        assertEquals("Token count mismatch", expectedTokens.size, actualTokens.size)

        for ((index, expected) in expectedTokens.withIndex()) {
            val actual = actualTokens[index]
            assertEquals(
                "Token text mismatch at position $index",
                expected.first,
                actual.first
            )
            assertEquals(
                "Token type mismatch at position $index for '${expected.first}'",
                expected.second,
                actual.second
            )
        }
    }

    private fun assertTokenSequence(code: String, expectedTypes: List<com.intellij.psi.tree.IElementType>) {
        val lexer = NasmLexerAdapter()
        lexer.start(code)

        val actualTypes = mutableListOf<com.intellij.psi.tree.IElementType>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) {
                actualTypes.add(lexer.tokenType!!)
            }
            lexer.advance()
        }

        assertEquals("Token count mismatch", expectedTypes.size, actualTypes.size)

        for ((index, expectedType) in expectedTypes.withIndex()) {
            assertEquals(
                "Token type mismatch at position $index",
                expectedType,
                actualTypes[index]
            )
        }
    }

    private fun collectTokens(lexer: NasmLexerAdapter): List<Pair<String, com.intellij.psi.tree.IElementType>> {
        val tokens = mutableListOf<Pair<String, com.intellij.psi.tree.IElementType>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenText to lexer.tokenType!!)
            lexer.advance()
        }
        return tokens
    }
}
