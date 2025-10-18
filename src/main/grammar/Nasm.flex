package dev.agb.nasmplugin.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import dev.agb.nasmplugin.psi.NasmTypes;
import com.intellij.psi.TokenType;

%%

%class NasmLexer
%implements FlexLexer
%unicode
%ignorecase
%function advance
%type IElementType
%eof{  return;
%eof}

// ============================================================================
// CHARACTER CLASSES AND BASIC PATTERNS
// ============================================================================

// Whitespace and newlines
// IMPORTANT: Newlines are significant in NASM - they terminate statements
CRLF=\R
WHITE_SPACE=[\ \t\f]
EOL_COMMENT=";"[^\r\n]*
C_COMMENT="/"\*([^*]|\*+[^*/])*\*+"/"

// Identifiers and labels (NASM spec: can start with $@ and contain $#@~.?)
// Pattern: [\$@]?[A-Za-z._?][A-Za-z0-9_$#@~.?]*
// Context-local identifiers start with %$ (e.g., %$start, %$loop)
// Macro-local identifiers start with %% (e.g., %%start, %%loop)
CONTEXT_LOCAL_ID="%$"[a-zA-Z._?][a-zA-Z0-9_$#@~.?]*
MACRO_LOCAL_ID="%%"[a-zA-Z._?][a-zA-Z0-9_$#@~.?]*
IDENTIFIER=[\$@]?[a-zA-Z._?][a-zA-Z0-9_$#@~.?]*
LABEL={IDENTIFIER}:

// ============================================================================
// NUMERIC LITERALS
// ============================================================================

// Numbers - NASM supports underscores as separators in all number formats
// Hexadecimal: 0x1234, 1234h, $1234
HEX_NUMBER=0[xX][0-9a-fA-F_]+|[0-9][0-9a-fA-F_]*[hHxX]|\$[0-9a-fA-F_]+
// Decimal: 1234, 0d1234, 1234d (with optional underscores)
DECIMAL_NUMBER=[0-9][0-9_]*|0[dDtT][0-9_]+|[0-9][0-9_]*[dDtT]
// Binary: 0b1010, 1010b, 1010y (with optional underscores)
BINARY_NUMBER=0[bByY][01_]+|[01][01_]*[bByY]
// Octal: 0o77, 77o, 77q (with optional underscores)
OCTAL_NUMBER=0[oOqQ][0-7_]+|[0-7][0-7_]*[oOqQ]
NUMBER={HEX_NUMBER}|{DECIMAL_NUMBER}|{BINARY_NUMBER}|{OCTAL_NUMBER}

// Float literals - NASM supports floating point with various formats
FLOAT_LITERAL=[0-9][0-9_]*\.[0-9_]*([eE][-+]?[0-9_]+)?|[0-9][0-9_]*[eE][-+]?[0-9_]+|\.[0-9][0-9_]*([eE][-+]?[0-9_]+)?

// Strings - NASM supports double quotes, single quotes, and backticks
// Note: Single-quoted strings can also be used as character literals (numeric values)
STRING_LITERAL=\"([^\"\n\\]|\\.)*\"|'([^'\n\\]|\\.)*'|`([^`\n\\]|\\.)*`

// ============================================================================
// OPERATORS AND DELIMITERS (order matters - longer operators first!)
// ============================================================================

BOOLEAN_OR="||"
BOOLEAN_XOR="^^"
BOOLEAN_AND="&&"
LSHIFT_COMPLETE="<<<"
RSHIFT_COMPLETE=">>>"
SIGNED_DIV="//"
SIGNED_MOD="%%"
DOT_DOT=".."
LSHIFT="<<"
RSHIFT=">>"
SPACESHIP="<=>"
LTE="<="
GTE=">="
NOT_EQUAL_1="!="
NOT_EQUAL_2="<>"
EQ_EQ="=="
DOUBLE_DOLLAR="$$"
PASTE_OP="%+"
QUERY_EXPAND="%??"
QUERY="%?"
PLUS="+"
MINUS="-"
MUL="*"
DIV="/"
MOD="%"
LBRACE="{"
RBRACE="}"
LBRACKET="["
RBRACKET="]"
LPAREN="("
RPAREN=")"
COMMA=","
COLON=":"
DOLLAR="$"
QUESTION="?"
AMP="&"
PIPE="|"
CARET="^"
TILDE="~"
EQ="="
LT="<"
GT=">"
EXCLAIM="!"

// ============================================================================
// PREPROCESSOR TOKENS
// ============================================================================

// Preprocessor special tokens (must come before generic PREPROCESSOR_DIRECTIVE)
ENV_VAR_PREFIX="%!"
MACRO_EXPANSION_START="%["
MACRO_EXPLICIT_START="%{"
MACRO_PARAM_GREEDY="%+"[0-9]+
MACRO_PARAM_REVERSE="%-"[0-9]+
MACRO_PARAM="%"[0-9]+

// Generic preprocessor directive pattern
PREPROCESSOR_DIRECTIVE="%"[a-zA-Z][a-zA-Z0-9]*

// WRT suffixes (case-sensitive, specific tokens)
WRT_SUFFIX="..got"|"..plt"|"..sym"|"..tlsie"

%%

<YYINITIAL> {
  // ============================================================================
  // WHITESPACE AND COMMENTS
  // ============================================================================

  {WHITE_SPACE}+              { return TokenType.WHITE_SPACE; }
  {CRLF}                      { return NasmTypes.CRLF; }
  {EOL_COMMENT}               { return NasmTypes.COMMENT; }
  {C_COMMENT}                 { return NasmTypes.COMMENT; }

  // ============================================================================
  // PREPROCESSOR TOKENS (must come before PREPROCESSOR_DIRECTIVE)
  // ============================================================================

  {CONTEXT_LOCAL_ID}          { return NasmTypes.CONTEXT_LOCAL_REF; }
  {MACRO_LOCAL_ID}            { return NasmTypes.MACRO_LOCAL_REF; }
  {ENV_VAR_PREFIX}            { return NasmTypes.ENV_VAR_PREFIX; }
  {QUERY_EXPAND}              { return NasmTypes.QUERY_EXPAND; }
  {QUERY}                     { return NasmTypes.QUERY; }
  {MACRO_EXPANSION_START}     { return NasmTypes.MACRO_EXPANSION_START; }
  {MACRO_EXPLICIT_START}      { return NasmTypes.MACRO_EXPLICIT_START; }
  {PASTE_OP}                  { return NasmTypes.PASTE_OP; }
  {MACRO_PARAM_GREEDY}        { return NasmTypes.MACRO_PARAM_GREEDY; }
  {MACRO_PARAM_REVERSE}       { return NasmTypes.MACRO_PARAM_REVERSE; }
  {MACRO_PARAM}               { return NasmTypes.MACRO_PARAM; }

  // ============================================================================
  // BUILT-IN FUNCTIONS AND SPECIAL CONSTANTS (case-insensitive)
  // ============================================================================

  "__ilog2e__"|"__ILOG2E__"                { return NasmTypes.BUILTIN_FUNC; }
  "__ilog2w__"|"__ILOG2W__"                { return NasmTypes.BUILTIN_FUNC; }
  "__ilog2f__"|"__ILOG2F__"                { return NasmTypes.BUILTIN_FUNC; }
  "__ilog2c__"|"__ILOG2C__"                { return NasmTypes.BUILTIN_FUNC; }
  "__utf16__"|"__UTF16__"                  { return NasmTypes.STRING_FUNC; }
  "__utf16le__"|"__UTF16LE__"              { return NasmTypes.STRING_FUNC; }
  "__utf16be__"|"__UTF16BE__"              { return NasmTypes.STRING_FUNC; }
  "__utf32__"|"__UTF32__"                  { return NasmTypes.STRING_FUNC; }
  "__utf32le__"|"__UTF32LE__"              { return NasmTypes.STRING_FUNC; }
  "__utf32be__"|"__UTF32BE__"              { return NasmTypes.STRING_FUNC; }
  "__float8__"|"__FLOAT8__"                { return NasmTypes.FLOAT_FUNC; }
  "__float16__"|"__FLOAT16__"              { return NasmTypes.FLOAT_FUNC; }
  "__float32__"|"__FLOAT32__"              { return NasmTypes.FLOAT_FUNC; }
  "__float64__"|"__FLOAT64__"              { return NasmTypes.FLOAT_FUNC; }
  "__float80m__"|"__FLOAT80M__"            { return NasmTypes.FLOAT_FUNC; }
  "__float80e__"|"__FLOAT80E__"            { return NasmTypes.FLOAT_FUNC; }
  "__float128l__"|"__FLOAT128L__"          { return NasmTypes.FLOAT_FUNC; }
  "__float128h__"|"__FLOAT128H__"          { return NasmTypes.FLOAT_FUNC; }
  "__bfloat16__"|"__BFLOAT16__"            { return NasmTypes.FLOAT_FUNC; }
  "__infinity__"|"__INFINITY__"            { return NasmTypes.SPECIAL_FLOAT; }
  "__nan__"|"__NAN__"                      { return NasmTypes.SPECIAL_FLOAT; }
  "__qnan__"|"__QNAN__"                    { return NasmTypes.SPECIAL_FLOAT; }
  "__snan__"|"__SNAN__"                    { return NasmTypes.SPECIAL_FLOAT; }
  "defined"|"DEFINED"                      { return NasmTypes.DEFINED; }

  // ============================================================================
  // PREPROCESSOR DIRECTIVES
  // ============================================================================

  {PREPROCESSOR_DIRECTIVE}    {
      String text = yytext().toString().toLowerCase();
      switch (text) {
          // Macro definitions
          case "%macro": case "%imacro": case "%rmacro": case "%irmacro":
              return NasmTypes.MACRO_START;
          case "%endmacro": case "%endm": case "%endrmacro":
              return NasmTypes.MACRO_END;
          case "%define": case "%xdefine": case "%idefine": case "%ixdefine":
          case "%defstr": case "%idefstr": case "%deftok": case "%ideftok":
          case "%defalias": case "%idefalias":
              return NasmTypes.MACRO_DEFINE;
          case "%assign": case "%iassign":
              return NasmTypes.MACRO_ASSIGN;
          case "%undef": case "%undefalias":
              return NasmTypes.MACRO_UNDEF;
          case "%unmacro": case "%unimacro": case "%unrmacro": case "%unirmacro":
              return NasmTypes.MACRO_UNMACRO;

          // Include directives
          case "%include":
              return NasmTypes.MACRO_INCLUDE;

          // Conditional directives - IF variants
          case "%ifmacro": case "%ifnmacro":
              return NasmTypes.MACRO_IFMACRO;
          case "%if": case "%ifidn": case "%ifidni":
          case "%ifid": case "%ifnum": case "%ifstr": case "%ifenv": case "%ifctx":
          case "%ifempty": case "%iftoken": case "%ifnctx": case "%ifnidn": case "%ifnidni":
          case "%ifnid": case "%ifnnum": case "%ifnstr": case "%ifntoken":
          case "%ifdirective": case "%ifusable": case "%ifusing": case "%iffile":
          case "%ifn": case "%ifnempty": case "%ifnenv":
              return NasmTypes.MACRO_IF;
          case "%ifdef": case "%ifdefalias": case "%ifndefalias":
              return NasmTypes.MACRO_IFDEF;
          case "%ifndef": case "%ifnndef":
              return NasmTypes.MACRO_IFNDEF;

          // Conditional directives - ELIF variants
          case "%elifmacro": case "%elifnmacro":
              return NasmTypes.MACRO_ELIFMACRO;
          case "%elif": case "%elifdef": case "%elifndef": case "%elifidn": case "%elifidni":
          case "%elifctx": case "%elifnctx":
          case "%elifid": case "%elifnid": case "%elifnum": case "%elifnnum":
          case "%elifstr": case "%elifnstr": case "%eliftoken": case "%elifntoken":
          case "%elifempty": case "%elifnempty": case "%elififdirective": case "%elifusable":
          case "%elifusing": case "%eliffile": case "%elifn": case "%elifenv": case "%elifnenv":
          case "%elifdefalias": case "%elifndefalias":
              return NasmTypes.MACRO_ELIF;
          case "%else":
              return NasmTypes.MACRO_ELSE;
          case "%endif":
              return NasmTypes.MACRO_ENDIF;

          // Repetition and flow control
          case "%rep":
              return NasmTypes.MACRO_REP;
          case "%endrep":
              return NasmTypes.MACRO_ENDREP;
          case "%exitrep":
              return NasmTypes.MACRO_EXITREP;
          case "%exitmacro":
              return NasmTypes.MACRO_EXITMACRO;
          case "%while":
              return NasmTypes.MACRO_WHILE;
          case "%endwhile":
              return NasmTypes.MACRO_ENDWHILE;

          // Macro utilities
          case "%rotate":
              return NasmTypes.MACRO_ROTATE;
          case "%error": case "%warning": case "%fatal": case "%note":
              return NasmTypes.MACRO_ERROR;
          case "%use":
              return NasmTypes.MACRO_USE;
          case "%push":
              return NasmTypes.MACRO_PUSH;
          case "%pop":
              return NasmTypes.MACRO_POP;
          case "%repl":
              return NasmTypes.MACRO_REPL;

          // String operations
          case "%strlen":
              return NasmTypes.MACRO_STRLEN;
          case "%substr":
              return NasmTypes.MACRO_SUBSTR;
          case "%strcat":
              return NasmTypes.MACRO_STRCAT;

          // Other directives
          case "%pathsearch":
              return NasmTypes.MACRO_PATHSEARCH;
          case "%depend":
              return NasmTypes.MACRO_DEPEND;
          case "%arg":
              return NasmTypes.MACRO_ARG_DECL;
          case "%stacksize":
              return NasmTypes.MACRO_STACKSIZE;
          case "%local":
              return NasmTypes.MACRO_LOCAL;
          case "%line":
              return NasmTypes.MACRO_LINE;
          case "%pragma":
              return NasmTypes.MACRO_PRAGMA;
          case "%clear":
              return NasmTypes.MACRO_CLEAR;
          case "%aliases":
              return NasmTypes.MACRO_ALIASES;

          default:
              return NasmTypes.PREPROCESSOR_DIRECTIVE;
      }
  }

  // ============================================================================
  // INSTRUCTION PREFIXES (case-insensitive - both forms listed)
  // ============================================================================

  "lock"|"LOCK"|"rep"|"REP"|"repe"|"REPE"|"repz"|"REPZ"              { return NasmTypes.INSTRUCTION_PREFIX; }
  "repne"|"REPNE"|"repnz"|"REPNZ"|"xacquire"|"XACQUIRE"              { return NasmTypes.INSTRUCTION_PREFIX; }
  "xrelease"|"XRELEASE"|"bnd"|"BND"|"nobnd"|"NOBND"                  { return NasmTypes.INSTRUCTION_PREFIX; }
  "osp"|"OSP"|"wait"|"WAIT"                                          { return NasmTypes.INSTRUCTION_PREFIX; }
  "a16"|"A16"|"a32"|"A32"|"a64"|"A64"|"asp"|"ASP"                    { return NasmTypes.INSTRUCTION_PREFIX; }
  "o16"|"O16"|"o32"|"O32"|"o64"|"O64"                                { return NasmTypes.INSTRUCTION_PREFIX; }
  "{rex}"|"{REX}"|"{evex}"|"{EVEX}"                                  { return NasmTypes.INSTRUCTION_PREFIX; }
  "{vex}"|"{VEX}"|"{vex2}"|"{VEX2}"                                  { return NasmTypes.INSTRUCTION_PREFIX; }
  "{vex3}"|"{VEX3}"                                                  { return NasmTypes.INSTRUCTION_PREFIX; }

  // ============================================================================
  // NASM KEYWORDS (case-insensitive - both forms listed)
  // ============================================================================

  // Data directives
  "db"|"DB"|"dw"|"DW"|"dd"|"DD"|"dq"|"DQ"                            { return NasmTypes.DATA_SIZE; }
  "dt"|"DT"|"do"|"DO"|"dy"|"DY"|"dz"|"DZ"                            { return NasmTypes.DATA_SIZE; }
  "resb"|"RESB"|"resw"|"RESW"|"resd"|"RESD"|"resq"|"RESQ"            { return NasmTypes.SPACE_SIZE; }
  "rest"|"REST"|"reso"|"RESO"|"resy"|"RESY"|"resz"|"RESZ"            { return NasmTypes.SPACE_SIZE; }

  // Assembler directives
  "equ"|"EQU"                 { return NasmTypes.EQU; }
  "section"|"SECTION"         { return NasmTypes.SECTION_KW; }
  "segment"|"SEGMENT"         { return NasmTypes.SEGMENT_KW; }
  "global"|"GLOBAL"           { return NasmTypes.GLOBAL_KW; }
  "extern"|"EXTERN"           { return NasmTypes.EXTERN_KW; }
  "common"|"COMMON"           { return NasmTypes.COMMON_KW; }
  "static"|"STATIC"           { return NasmTypes.STATIC_KW; }
  "required"|"REQUIRED"       { return NasmTypes.REQUIRED_KW; }
  "incbin"|"INCBIN"           { return NasmTypes.INCBIN_KW; }
  "times"|"TIMES"             { return NasmTypes.TIMES; }
  "bits"|"BITS"               { return NasmTypes.BITS_KW; }
  "use16"|"USE16"             { return NasmTypes.USE16_KW; }
  "use32"|"USE32"             { return NasmTypes.USE32_KW; }
  "use64"|"USE64"             { return NasmTypes.USE64_KW; }
  "default"|"DEFAULT"         { return NasmTypes.DEFAULT_KW; }
  "absolute"|"ABSOLUTE"       { return NasmTypes.ABSOLUTE_KW; }
  "struc"|"STRUC"             { return NasmTypes.STRUC_KW; }
  "endstruc"|"ENDSTRUC"       { return NasmTypes.ENDSTRUC_KW; }
  "istruc"|"ISTRUC"           { return NasmTypes.ISTRUC_KW; }
  "iend"|"IEND"               { return NasmTypes.IEND_KW; }
  "at"|"AT"                   { return NasmTypes.AT_KW; }
  "align"|"ALIGN"             { return NasmTypes.ALIGN_KW; }
  "alignb"|"ALIGNB"           { return NasmTypes.ALIGNB_KW; }
  "sectalign"|"SECTALIGN"     { return NasmTypes.SECTALIGN_KW; }
  "cpu"|"CPU"                 { return NasmTypes.CPU_KW; }
  "float"|"FLOAT"             { return NasmTypes.FLOAT_KW; }
  "org"|"ORG"                 { return NasmTypes.ORG_KW; }
  "map"|"MAP"                 { return NasmTypes.MAP_KW; }

  // Section attributes (case-insensitive - both forms listed)
  "exec"|"EXEC"               { return NasmTypes.SECTION_ATTR_KW; }
  "noexec"|"NOEXEC"           { return NasmTypes.SECTION_ATTR_KW; }
  "alloc"|"ALLOC"             { return NasmTypes.SECTION_ATTR_KW; }
  "noalloc"|"NOALLOC"         { return NasmTypes.SECTION_ATTR_KW; }
  "write"|"WRITE"             { return NasmTypes.SECTION_ATTR_KW; }
  "nowrite"|"NOWRITE"         { return NasmTypes.SECTION_ATTR_KW; }
  "tls"|"TLS"                 { return NasmTypes.SECTION_ATTR_KW; }
  "notls"|"NOTLS"             { return NasmTypes.SECTION_ATTR_KW; }
  "nobits"|"NOBITS"           { return NasmTypes.SECTION_ATTR_KW; }
  "progbits"|"PROGBITS"       { return NasmTypes.SECTION_ATTR_KW; }
  "contents"|"CONTENTS"       { return NasmTypes.SECTION_ATTR_KW; }
  "merge"|"MERGE"             { return NasmTypes.SECTION_ATTR_KW; }
  "strings"|"STRINGS"         { return NasmTypes.SECTION_ATTR_KW; }

  // Special keywords
  "wrt"|"WRT"                 { return NasmTypes.WRT; }
  {WRT_SUFFIX}                { return NasmTypes.WRT_SUFFIX; }
  "seg"|"SEG"                 { return NasmTypes.SEG; }
  "dup"|"DUP"                 { return NasmTypes.DUP; }
  "strict"|"STRICT"           { return NasmTypes.STRICT; }
  "rel"|"REL"                 { return NasmTypes.REL; }
  "abs"|"ABS"                 { return NasmTypes.ABS; }

  // ============================================================================
  // SIZE SPECIFIERS (case-insensitive - both forms listed)
  // ============================================================================

  "byte"|"BYTE"|"word"|"WORD"|"dword"|"DWORD"|"qword"|"QWORD"        { return NasmTypes.SIZE_SPEC; }
  "tword"|"TWORD"|"oword"|"OWORD"|"yword"|"YWORD"|"zword"|"ZWORD"    { return NasmTypes.SIZE_SPEC; }
  "xmmword"|"XMMWORD"|"ymmword"|"YMMWORD"|"zmmword"|"ZMMWORD"        { return NasmTypes.SIZE_SPEC; }
  "ptr"|"PTR"|"near"|"NEAR"|"far"|"FAR"|"short"|"SHORT"              { return NasmTypes.SIZE_SPEC; }

  // ============================================================================
  // REGISTERS - Explicitly listed to avoid invalid register matches
  // ============================================================================

  // 64-bit general purpose registers
  "rax"|"RAX"|"rbx"|"RBX"|"rcx"|"RCX"|"rdx"|"RDX"                    { return NasmTypes.REGISTER; }
  "rsi"|"RSI"|"rdi"|"RDI"|"rsp"|"RSP"|"rbp"|"RBP"|"rip"|"RIP"        { return NasmTypes.REGISTER; }
  "r8"|"R8"|"r9"|"R9"|"r10"|"R10"|"r11"|"R11"                        { return NasmTypes.REGISTER; }
  "r12"|"R12"|"r13"|"R13"|"r14"|"R14"|"r15"|"R15"                    { return NasmTypes.REGISTER; }
  "r0"|"R0"|"r1"|"R1"|"r2"|"R2"|"r3"|"R3"                            { return NasmTypes.REGISTER; }
  "r4"|"R4"|"r5"|"R5"|"r6"|"R6"|"r7"|"R7"                            { return NasmTypes.REGISTER; }

  // 32-bit general purpose registers
  "eax"|"EAX"|"ebx"|"EBX"|"ecx"|"ECX"|"edx"|"EDX"                    { return NasmTypes.REGISTER; }
  "esi"|"ESI"|"edi"|"EDI"|"esp"|"ESP"|"ebp"|"EBP"                    { return NasmTypes.REGISTER; }
  "r8d"|"R8D"|"r9d"|"R9D"|"r10d"|"R10D"|"r11d"|"R11D"                { return NasmTypes.REGISTER; }
  "r12d"|"R12D"|"r13d"|"R13D"|"r14d"|"R14D"|"r15d"|"R15D"            { return NasmTypes.REGISTER; }
  "r0d"|"R0D"|"r1d"|"R1D"|"r2d"|"R2D"|"r3d"|"R3D"                    { return NasmTypes.REGISTER; }
  "r4d"|"R4D"|"r5d"|"R5D"|"r6d"|"R6D"|"r7d"|"R7D"                    { return NasmTypes.REGISTER; }

  // 16-bit general purpose registers
  "ax"|"AX"|"bx"|"BX"|"cx"|"CX"|"dx"|"DX"                            { return NasmTypes.REGISTER; }
  "si"|"SI"|"di"|"DI"|"sp"|"SP"|"bp"|"BP"                            { return NasmTypes.REGISTER; }
  "r8w"|"R8W"|"r9w"|"R9W"|"r10w"|"R10W"|"r11w"|"R11W"                { return NasmTypes.REGISTER; }
  "r12w"|"R12W"|"r13w"|"R13W"|"r14w"|"R14W"|"r15w"|"R15W"            { return NasmTypes.REGISTER; }
  "r0w"|"R0W"|"r1w"|"R1W"|"r2w"|"R2W"|"r3w"|"R3W"                    { return NasmTypes.REGISTER; }
  "r4w"|"R4W"|"r5w"|"R5W"|"r6w"|"R6W"|"r7w"|"R7W"                    { return NasmTypes.REGISTER; }

  // 8-bit general purpose registers
  "al"|"AL"|"ah"|"AH"|"bl"|"BL"|"bh"|"BH"                            { return NasmTypes.REGISTER; }
  "cl"|"CL"|"ch"|"CH"|"dl"|"DL"|"dh"|"DH"                            { return NasmTypes.REGISTER; }
  "sil"|"SIL"|"dil"|"DIL"|"spl"|"SPL"|"bpl"|"BPL"                    { return NasmTypes.REGISTER; }
  "r8b"|"R8B"|"r9b"|"R9B"|"r10b"|"R10B"|"r11b"|"R11B"                { return NasmTypes.REGISTER; }
  "r12b"|"R12B"|"r13b"|"R13B"|"r14b"|"R14B"|"r15b"|"R15B"            { return NasmTypes.REGISTER; }
  "r0b"|"R0B"|"r1b"|"R1B"|"r2b"|"R2B"|"r3b"|"R3B"                    { return NasmTypes.REGISTER; }
  "r4b"|"R4B"|"r5b"|"R5B"|"r6b"|"R6B"|"r7b"|"R7B"                    { return NasmTypes.REGISTER; }
  "r0l"|"R0L"|"r1l"|"R1L"|"r2l"|"R2L"|"r3l"|"R3L"                    { return NasmTypes.REGISTER; }
  "r4l"|"R4L"|"r5l"|"R5L"|"r6l"|"R6L"|"r7l"|"R7L"                    { return NasmTypes.REGISTER; }
  "r8l"|"R8L"|"r9l"|"R9L"|"r10l"|"R10L"|"r11l"|"R11L"                { return NasmTypes.REGISTER; }
  "r12l"|"R12L"|"r13l"|"R13L"|"r14l"|"R14L"|"r15l"|"R15L"            { return NasmTypes.REGISTER; }
  "r0h"|"R0H"|"r1h"|"R1H"|"r2h"|"R2H"|"r3h"|"R3H"                    { return NasmTypes.REGISTER; }

  // Segment registers
  "cs"|"CS"|"ds"|"DS"|"ss"|"SS"|"es"|"ES"|"fs"|"FS"|"gs"|"GS"        { return NasmTypes.SEG_REGISTER; }

  // Control, debug, and test registers
  "cr0"|"CR0"|"cr1"|"CR1"|"cr2"|"CR2"|"cr3"|"CR3"                    { return NasmTypes.REGISTER; }
  "cr4"|"CR4"|"cr5"|"CR5"|"cr6"|"CR6"|"cr7"|"CR7"|"cr8"|"CR8"        { return NasmTypes.REGISTER; }
  "dr0"|"DR0"|"dr1"|"DR1"|"dr2"|"DR2"|"dr3"|"DR3"                    { return NasmTypes.REGISTER; }
  "dr4"|"DR4"|"dr5"|"DR5"|"dr6"|"DR6"|"dr7"|"DR7"                    { return NasmTypes.REGISTER; }
  "tr0"|"TR0"|"tr1"|"TR1"|"tr2"|"TR2"|"tr3"|"TR3"                    { return NasmTypes.REGISTER; }
  "tr4"|"TR4"|"tr5"|"TR5"|"tr6"|"TR6"|"tr7"|"TR7"                    { return NasmTypes.REGISTER; }

  // FPU registers
  "st0"|"ST0"|"st1"|"ST1"|"st2"|"ST2"|"st3"|"ST3"                    { return NasmTypes.REGISTER; }
  "st4"|"ST4"|"st5"|"ST5"|"st6"|"ST6"|"st7"|"ST7"|"st"|"ST"          { return NasmTypes.REGISTER; }

  // MMX registers
  "mm0"|"MM0"|"mm1"|"MM1"|"mm2"|"MM2"|"mm3"|"MM3"                    { return NasmTypes.REGISTER; }
  "mm4"|"MM4"|"mm5"|"MM5"|"mm6"|"MM6"|"mm7"|"MM7"                    { return NasmTypes.REGISTER; }

  // XMM registers (SSE)
  "xmm0"|"XMM0"|"xmm1"|"XMM1"|"xmm2"|"XMM2"|"xmm3"|"XMM3"            { return NasmTypes.REGISTER; }
  "xmm4"|"XMM4"|"xmm5"|"XMM5"|"xmm6"|"XMM6"|"xmm7"|"XMM7"            { return NasmTypes.REGISTER; }
  "xmm8"|"XMM8"|"xmm9"|"XMM9"|"xmm10"|"XMM10"|"xmm11"|"XMM11"        { return NasmTypes.REGISTER; }
  "xmm12"|"XMM12"|"xmm13"|"XMM13"|"xmm14"|"XMM14"|"xmm15"|"XMM15"    { return NasmTypes.REGISTER; }
  "xmm16"|"XMM16"|"xmm17"|"XMM17"|"xmm18"|"XMM18"|"xmm19"|"XMM19"    { return NasmTypes.REGISTER; }
  "xmm20"|"XMM20"|"xmm21"|"XMM21"|"xmm22"|"XMM22"|"xmm23"|"XMM23"    { return NasmTypes.REGISTER; }
  "xmm24"|"XMM24"|"xmm25"|"XMM25"|"xmm26"|"XMM26"|"xmm27"|"XMM27"    { return NasmTypes.REGISTER; }
  "xmm28"|"XMM28"|"xmm29"|"XMM29"|"xmm30"|"XMM30"|"xmm31"|"XMM31"    { return NasmTypes.REGISTER; }

  // YMM registers (AVX)
  "ymm0"|"YMM0"|"ymm1"|"YMM1"|"ymm2"|"YMM2"|"ymm3"|"YMM3"            { return NasmTypes.REGISTER; }
  "ymm4"|"YMM4"|"ymm5"|"YMM5"|"ymm6"|"YMM6"|"ymm7"|"YMM7"            { return NasmTypes.REGISTER; }
  "ymm8"|"YMM8"|"ymm9"|"YMM9"|"ymm10"|"YMM10"|"ymm11"|"YMM11"        { return NasmTypes.REGISTER; }
  "ymm12"|"YMM12"|"ymm13"|"YMM13"|"ymm14"|"YMM14"|"ymm15"|"YMM15"    { return NasmTypes.REGISTER; }
  "ymm16"|"YMM16"|"ymm17"|"YMM17"|"ymm18"|"YMM18"|"ymm19"|"YMM19"    { return NasmTypes.REGISTER; }
  "ymm20"|"YMM20"|"ymm21"|"YMM21"|"ymm22"|"YMM22"|"ymm23"|"YMM23"    { return NasmTypes.REGISTER; }
  "ymm24"|"YMM24"|"ymm25"|"YMM25"|"ymm26"|"YMM26"|"ymm27"|"YMM27"    { return NasmTypes.REGISTER; }
  "ymm28"|"YMM28"|"ymm29"|"YMM29"|"ymm30"|"YMM30"|"ymm31"|"YMM31"    { return NasmTypes.REGISTER; }

  // ZMM registers (AVX-512)
  "zmm0"|"ZMM0"|"zmm1"|"ZMM1"|"zmm2"|"ZMM2"|"zmm3"|"ZMM3"            { return NasmTypes.REGISTER; }
  "zmm4"|"ZMM4"|"zmm5"|"ZMM5"|"zmm6"|"ZMM6"|"zmm7"|"ZMM7"            { return NasmTypes.REGISTER; }
  "zmm8"|"ZMM8"|"zmm9"|"ZMM9"|"zmm10"|"ZMM10"|"zmm11"|"ZMM11"        { return NasmTypes.REGISTER; }
  "zmm12"|"ZMM12"|"zmm13"|"ZMM13"|"zmm14"|"ZMM14"|"zmm15"|"ZMM15"    { return NasmTypes.REGISTER; }
  "zmm16"|"ZMM16"|"zmm17"|"ZMM17"|"zmm18"|"ZMM18"|"zmm19"|"ZMM19"    { return NasmTypes.REGISTER; }
  "zmm20"|"ZMM20"|"zmm21"|"ZMM21"|"zmm22"|"ZMM22"|"zmm23"|"ZMM23"    { return NasmTypes.REGISTER; }
  "zmm24"|"ZMM24"|"zmm25"|"ZMM25"|"zmm26"|"ZMM26"|"zmm27"|"ZMM27"    { return NasmTypes.REGISTER; }
  "zmm28"|"ZMM28"|"zmm29"|"ZMM29"|"zmm30"|"ZMM30"|"zmm31"|"ZMM31"    { return NasmTypes.REGISTER; }

  // AVX-512 mask registers
  "k0"|"K0"|"k1"|"K1"|"k2"|"K2"|"k3"|"K3"                            { return NasmTypes.MASK_REG; }
  "k4"|"K4"|"k5"|"K5"|"k6"|"K6"|"k7"|"K7"                            { return NasmTypes.MASK_REG; }

  // ============================================================================
  // AVX-512 DECORATORS (case-insensitive - both forms listed)
  // ============================================================================

  "z"|"Z"                                   { return NasmTypes.ZEROING; }
  "sae"|"SAE"                               { return NasmTypes.SAE; }
  "rn-sae"|"RN-SAE"|"rd-sae"|"RD-SAE"       { return NasmTypes.ROUNDING; }
  "ru-sae"|"RU-SAE"|"rz-sae"|"RZ-SAE"       { return NasmTypes.ROUNDING; }
  "1to2"|"1TO2"|"1to4"|"1TO4"|"1to8"|"1TO8" { return NasmTypes.BROADCAST; }
  "1to16"|"1TO16"|"1to32"|"1TO32"           { return NasmTypes.BROADCAST; }

  // ============================================================================
  // LITERALS
  // ============================================================================

  {FLOAT_LITERAL}             { return NasmTypes.FLOAT; }
  {NUMBER}                    { return NasmTypes.NUMBER; }
  {STRING_LITERAL}            { return NasmTypes.STRING; }

  // ============================================================================
  // OPERATORS (order matters: longer first)
  // ============================================================================

  {BOOLEAN_OR}                { return NasmTypes.BOOLEAN_OR; }
  {BOOLEAN_XOR}               { return NasmTypes.BOOLEAN_XOR; }
  {BOOLEAN_AND}               { return NasmTypes.BOOLEAN_AND; }
  {LSHIFT_COMPLETE}           { return NasmTypes.LSHIFT_COMPLETE; }
  {RSHIFT_COMPLETE}           { return NasmTypes.RSHIFT_COMPLETE; }
  {SIGNED_DIV}                { return NasmTypes.SIGNED_DIV; }
  {SIGNED_MOD}                { return NasmTypes.SIGNED_MOD; }
  {DOT_DOT}                   { return NasmTypes.DOT_DOT; }
  {LSHIFT}                    { return NasmTypes.LSHIFT; }
  {RSHIFT}                    { return NasmTypes.RSHIFT; }
  {SPACESHIP}                 { return NasmTypes.SPACESHIP; }
  {LTE}                       { return NasmTypes.LTE; }
  {GTE}                       { return NasmTypes.GTE; }
  {NOT_EQUAL_1}               { return NasmTypes.NOT_EQUAL_1; }
  {NOT_EQUAL_2}               { return NasmTypes.NOT_EQUAL_2; }
  {EQ_EQ}                     { return NasmTypes.EQ_EQ; }
  {DOUBLE_DOLLAR}             { return NasmTypes.DOUBLE_DOLLAR; }
  {PLUS}                      { return NasmTypes.PLUS; }
  {MINUS}                     { return NasmTypes.MINUS; }
  {MUL}                       { return NasmTypes.MUL; }
  {DIV}                       { return NasmTypes.DIV; }
  {MOD}                       { return NasmTypes.MOD; }
  {AMP}                       { return NasmTypes.AMP; }
  {PIPE}                      { return NasmTypes.PIPE; }
  {CARET}                     { return NasmTypes.CARET; }
  {TILDE}                     { return NasmTypes.TILDE; }
  {EXCLAIM}                   { return NasmTypes.EXCLAIM; }
  {EQ}                        { return NasmTypes.EQ; }
  {LT}                        { return NasmTypes.LT; }
  {GT}                        { return NasmTypes.GT; }
  {LBRACE}                    { return NasmTypes.LBRACE; }
  {RBRACE}                    { return NasmTypes.RBRACE; }
  {LBRACKET}                  { return NasmTypes.LBRACKET; }
  {RBRACKET}                  { return NasmTypes.RBRACKET; }
  {LPAREN}                    { return NasmTypes.LPAREN; }
  {RPAREN}                    { return NasmTypes.RPAREN; }
  {COMMA}                     { return NasmTypes.COMMA; }
  {DOLLAR}                    { return NasmTypes.DOLLAR; }
  {QUESTION}                  { return NasmTypes.QUESTION_MARK; }
  {COLON}                     { return NasmTypes.COLON; }

  // ============================================================================
  // IDENTIFIERS (must come last!)
  // ============================================================================

  {IDENTIFIER}                { return NasmTypes.IDENTIFIER; }
}

// ============================================================================
// ERROR FALLBACK
// ============================================================================

[^]                           { return TokenType.BAD_CHARACTER; }
