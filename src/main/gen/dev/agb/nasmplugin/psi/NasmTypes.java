// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.agb.nasmplugin.psi.impl.*;

public interface NasmTypes {

  IElementType ABSOLUTE_DIR = new NasmElementType("ABSOLUTE_DIR");
  IElementType ALIGN_DIR = new NasmElementType("ALIGN_DIR");
  IElementType ARG_DIRECTIVE = new NasmElementType("ARG_DIRECTIVE");
  IElementType ASSIGNMENT = new NasmElementType("ASSIGNMENT");
  IElementType ATOM_EXPR = new NasmElementType("ATOM_EXPR");
  IElementType AT_DIRECTIVE = new NasmElementType("AT_DIRECTIVE");
  IElementType BITS_DIR = new NasmElementType("BITS_DIR");
  IElementType BRACKETED_DIRECTIVE = new NasmElementType("BRACKETED_DIRECTIVE");
  IElementType BUILTIN_FUNCTION = new NasmElementType("BUILTIN_FUNCTION");
  IElementType CLEAR_DIR = new NasmElementType("CLEAR_DIR");
  IElementType CLEAR_TARGET = new NasmElementType("CLEAR_TARGET");
  IElementType COMMON_DIR = new NasmElementType("COMMON_DIR");
  IElementType CONDITION = new NasmElementType("CONDITION");
  IElementType CONDITIONAL_BLOCK = new NasmElementType("CONDITIONAL_BLOCK");
  IElementType CONDITIONAL_DATA = new NasmElementType("CONDITIONAL_DATA");
  IElementType CONDITIONAL_OPERAND = new NasmElementType("CONDITIONAL_OPERAND");
  IElementType CONTEXT_DIR = new NasmElementType("CONTEXT_DIR");
  IElementType CONTEXT_NAME = new NasmElementType("CONTEXT_NAME");
  IElementType CONTEXT_REF = new NasmElementType("CONTEXT_REF");
  IElementType CPU_DIR = new NasmElementType("CPU_DIR");
  IElementType DATA_DEF = new NasmElementType("DATA_DEF");
  IElementType DATA_ITEM = new NasmElementType("DATA_ITEM");
  IElementType DATA_LIST = new NasmElementType("DATA_LIST");
  IElementType DECORATOR = new NasmElementType("DECORATOR");
  IElementType DECORATOR_ITEM = new NasmElementType("DECORATOR_ITEM");
  IElementType DECORATOR_LIST = new NasmElementType("DECORATOR_LIST");
  IElementType DEFAULT_DIR = new NasmElementType("DEFAULT_DIR");
  IElementType DEFAULT_OPTION = new NasmElementType("DEFAULT_OPTION");
  IElementType DEFAULT_VALUE = new NasmElementType("DEFAULT_VALUE");
  IElementType DEFINED_FUNC = new NasmElementType("DEFINED_FUNC");
  IElementType EA_EXPR = new NasmElementType("EA_EXPR");
  IElementType EA_TERM = new NasmElementType("EA_TERM");
  IElementType EFFECTIVE_ADDR = new NasmElementType("EFFECTIVE_ADDR");
  IElementType ELIF_DIR = new NasmElementType("ELIF_DIR");
  IElementType ELSE_DIR = new NasmElementType("ELSE_DIR");
  IElementType ENDIF_DIR = new NasmElementType("ENDIF_DIR");
  IElementType ENV_VAR_REF = new NasmElementType("ENV_VAR_REF");
  IElementType EQU_DEFINITION = new NasmElementType("EQU_DEFINITION");
  IElementType EXIT_DIR = new NasmElementType("EXIT_DIR");
  IElementType EXPANDED_OPERAND = new NasmElementType("EXPANDED_OPERAND");
  IElementType EXPRESSION = new NasmElementType("EXPRESSION");
  IElementType EXTERN_DIR = new NasmElementType("EXTERN_DIR");
  IElementType FLOAT_DIR = new NasmElementType("FLOAT_DIR");
  IElementType FLOAT_FORMAT = new NasmElementType("FLOAT_FORMAT");
  IElementType FLOAT_VALUE = new NasmElementType("FLOAT_VALUE");
  IElementType FUNCTION_MACRO_CALL = new NasmElementType("FUNCTION_MACRO_CALL");
  IElementType GLOBAL_DIR = new NasmElementType("GLOBAL_DIR");
  IElementType GLOBAL_LABEL = new NasmElementType("GLOBAL_LABEL");
  IElementType IF_DIR = new NasmElementType("IF_DIR");
  IElementType INCLUDE_DIR = new NasmElementType("INCLUDE_DIR");
  IElementType INSTRUCTION = new NasmElementType("INSTRUCTION");
  IElementType INTEGER_FUNCTION = new NasmElementType("INTEGER_FUNCTION");
  IElementType INVOCATION = new NasmElementType("INVOCATION");
  IElementType ISTRUC_BLOCK = new NasmElementType("ISTRUC_BLOCK");
  IElementType KEYWORD_AS_NAME = new NasmElementType("KEYWORD_AS_NAME");
  IElementType LABEL_DEF = new NasmElementType("LABEL_DEF");
  IElementType LINE_DIR = new NasmElementType("LINE_DIR");
  IElementType LOCAL_DIRECTIVE = new NasmElementType("LOCAL_DIRECTIVE");
  IElementType LOCAL_LABEL = new NasmElementType("LOCAL_LABEL");
  IElementType LOCK_PREFIX = new NasmElementType("LOCK_PREFIX");
  IElementType MACRO_ARG = new NasmElementType("MACRO_ARG");
  IElementType MACRO_ARGS = new NasmElementType("MACRO_ARGS");
  IElementType MACRO_BODY_INLINE = new NasmElementType("MACRO_BODY_INLINE");
  IElementType MACRO_CALL = new NasmElementType("MACRO_CALL");
  IElementType MACRO_DIR = new NasmElementType("MACRO_DIR");
  IElementType MACRO_EXPANSION = new NasmElementType("MACRO_EXPANSION");
  IElementType MACRO_FLAGS = new NasmElementType("MACRO_FLAGS");
  IElementType MACRO_LINES = new NasmElementType("MACRO_LINES");
  IElementType MACRO_NAME = new NasmElementType("MACRO_NAME");
  IElementType MACRO_PARAMS = new NasmElementType("MACRO_PARAMS");
  IElementType MACRO_PARAM_REF = new NasmElementType("MACRO_PARAM_REF");
  IElementType MACRO_REF = new NasmElementType("MACRO_REF");
  IElementType MEMORY_EXPR = new NasmElementType("MEMORY_EXPR");
  IElementType MEMORY_REF = new NasmElementType("MEMORY_REF");
  IElementType MESSAGE_DIR = new NasmElementType("MESSAGE_DIR");
  IElementType MESSAGE_TEXT = new NasmElementType("MESSAGE_TEXT");
  IElementType MNEMONIC = new NasmElementType("MNEMONIC");
  IElementType MULTI_LINE_MACRO = new NasmElementType("MULTI_LINE_MACRO");
  IElementType OPERAND = new NasmElementType("OPERAND");
  IElementType OPERAND_LIST = new NasmElementType("OPERAND_LIST");
  IElementType OPERATOR = new NasmElementType("OPERATOR");
  IElementType ORG_DIR = new NasmElementType("ORG_DIR");
  IElementType PACKAGE_NAME = new NasmElementType("PACKAGE_NAME");
  IElementType PARAM_COUNT = new NasmElementType("PARAM_COUNT");
  IElementType PARAM_LIST = new NasmElementType("PARAM_LIST");
  IElementType PARAM_NAME = new NasmElementType("PARAM_NAME");
  IElementType PARAM_QUALIFIER = new NasmElementType("PARAM_QUALIFIER");
  IElementType PARAM_SPEC = new NasmElementType("PARAM_SPEC");
  IElementType PP_ASSIGN_STMT = new NasmElementType("PP_ASSIGN_STMT");
  IElementType PP_DEFINE_STMT = new NasmElementType("PP_DEFINE_STMT");
  IElementType PREPROCESSOR_FUNCTION = new NasmElementType("PREPROCESSOR_FUNCTION");
  IElementType PREPROCESSOR_ID = new NasmElementType("PREPROCESSOR_ID");
  IElementType PREPROCESSOR_LINE = new NasmElementType("PREPROCESSOR_LINE");
  IElementType PREPROCESSOR_TOKEN = new NasmElementType("PREPROCESSOR_TOKEN");
  IElementType PSEUDO_INSTRUCTION = new NasmElementType("PSEUDO_INSTRUCTION");
  IElementType QUERY_FUNCTION = new NasmElementType("QUERY_FUNCTION");
  IElementType REP_BLOCK = new NasmElementType("REP_BLOCK");
  IElementType SCALE = new NasmElementType("SCALE");
  IElementType SECTION_ATTR = new NasmElementType("SECTION_ATTR");
  IElementType SECTION_ATTRS = new NasmElementType("SECTION_ATTRS");
  IElementType SECTION_ATTR_NAME = new NasmElementType("SECTION_ATTR_NAME");
  IElementType SECTION_DIR = new NasmElementType("SECTION_DIR");
  IElementType SECTION_NAME = new NasmElementType("SECTION_NAME");
  IElementType SEGMENT_OVERRIDE = new NasmElementType("SEGMENT_OVERRIDE");
  IElementType SEGMENT_REG = new NasmElementType("SEGMENT_REG");
  IElementType SEPARATOR = new NasmElementType("SEPARATOR");
  IElementType SIZE_OVERRIDE = new NasmElementType("SIZE_OVERRIDE");
  IElementType SMACRO_EXPANSION = new NasmElementType("SMACRO_EXPANSION");
  IElementType SOURCE_LINE = new NasmElementType("SOURCE_LINE");
  IElementType SOURCE_LINES = new NasmElementType("SOURCE_LINES");
  IElementType SPACE_DEF = new NasmElementType("SPACE_DEF");
  IElementType SPECIAL_SYMBOL = new NasmElementType("SPECIAL_SYMBOL");
  IElementType STRINGIZE_OP = new NasmElementType("STRINGIZE_OP");
  IElementType STRING_FUNCTION = new NasmElementType("STRING_FUNCTION");
  IElementType STRING_OR_ENV = new NasmElementType("STRING_OR_ENV");
  IElementType STRING_TRANSFORM = new NasmElementType("STRING_TRANSFORM");
  IElementType STRUC_BLOCK = new NasmElementType("STRUC_BLOCK");
  IElementType SYMBOL_DECL = new NasmElementType("SYMBOL_DECL");
  IElementType SYMBOL_LIST = new NasmElementType("SYMBOL_LIST");
  IElementType SYMBOL_NAME = new NasmElementType("SYMBOL_NAME");
  IElementType SYMBOL_REF = new NasmElementType("SYMBOL_REF");
  IElementType TIMES_EXPR = new NasmElementType("TIMES_EXPR");
  IElementType TOKEN_COMPARISON = new NasmElementType("TOKEN_COMPARISON");
  IElementType TOKEN_SEQUENCE = new NasmElementType("TOKEN_SEQUENCE");
  IElementType USE_PACKAGE = new NasmElementType("USE_PACKAGE");
  IElementType VEX_PREFIX = new NasmElementType("VEX_PREFIX");

  IElementType ABS = new NasmTokenType("ABS");
  IElementType ABSOLUTE_KW = new NasmTokenType("ABSOLUTE_KW");
  IElementType ALIGNB_KW = new NasmTokenType("ALIGNB_KW");
  IElementType ALIGN_KW = new NasmTokenType("ALIGN_KW");
  IElementType AMP = new NasmTokenType("AMP");
  IElementType AT_KW = new NasmTokenType("AT_KW");
  IElementType BITS_KW = new NasmTokenType("BITS_KW");
  IElementType BOOLEAN_AND = new NasmTokenType("BOOLEAN_AND");
  IElementType BOOLEAN_OR = new NasmTokenType("BOOLEAN_OR");
  IElementType BOOLEAN_XOR = new NasmTokenType("BOOLEAN_XOR");
  IElementType BROADCAST = new NasmTokenType("BROADCAST");
  IElementType BUILTIN_FUNC = new NasmTokenType("BUILTIN_FUNC");
  IElementType CARET = new NasmTokenType("CARET");
  IElementType COLON = new NasmTokenType("COLON");
  IElementType COMMA = new NasmTokenType("COMMA");
  IElementType COMMENT = new NasmTokenType("COMMENT");
  IElementType COMMON_KW = new NasmTokenType("COMMON_KW");
  IElementType CONTEXT_LOCAL_REF = new NasmTokenType("CONTEXT_LOCAL_REF");
  IElementType CPU_KW = new NasmTokenType("CPU_KW");
  IElementType CRLF = new NasmTokenType("CRLF");
  IElementType DATA_SIZE = new NasmTokenType("DATA_SIZE");
  IElementType DEFAULT_KW = new NasmTokenType("DEFAULT_KW");
  IElementType DEFINED = new NasmTokenType("DEFINED");
  IElementType DIV = new NasmTokenType("DIV");
  IElementType DOLLAR = new NasmTokenType("DOLLAR");
  IElementType DOT_DOT = new NasmTokenType("DOT_DOT");
  IElementType DOUBLE_DOLLAR = new NasmTokenType("DOUBLE_DOLLAR");
  IElementType DUP = new NasmTokenType("DUP");
  IElementType ENDSTRUC_KW = new NasmTokenType("ENDSTRUC_KW");
  IElementType ENV_VAR_PREFIX = new NasmTokenType("ENV_VAR_PREFIX");
  IElementType EQ = new NasmTokenType("EQ");
  IElementType EQU = new NasmTokenType("EQU");
  IElementType EQ_EQ = new NasmTokenType("EQ_EQ");
  IElementType EXCLAIM = new NasmTokenType("EXCLAIM");
  IElementType EXTERN_KW = new NasmTokenType("EXTERN_KW");
  IElementType FLOAT = new NasmTokenType("FLOAT");
  IElementType FLOAT_FUNC = new NasmTokenType("FLOAT_FUNC");
  IElementType FLOAT_KW = new NasmTokenType("FLOAT_KW");
  IElementType GLOBAL_KW = new NasmTokenType("GLOBAL_KW");
  IElementType GT = new NasmTokenType("GT");
  IElementType GTE = new NasmTokenType("GTE");
  IElementType IDENTIFIER = new NasmTokenType("IDENTIFIER");
  IElementType IEND_KW = new NasmTokenType("IEND_KW");
  IElementType INCBIN_KW = new NasmTokenType("INCBIN_KW");
  IElementType INSTRUCTION_PREFIX = new NasmTokenType("INSTRUCTION_PREFIX");
  IElementType ISTRUC_KW = new NasmTokenType("ISTRUC_KW");
  IElementType LBRACE = new NasmTokenType("LBRACE");
  IElementType LBRACKET = new NasmTokenType("LBRACKET");
  IElementType LPAREN = new NasmTokenType("LPAREN");
  IElementType LSHIFT = new NasmTokenType("LSHIFT");
  IElementType LSHIFT_COMPLETE = new NasmTokenType("LSHIFT_COMPLETE");
  IElementType LT = new NasmTokenType("LT");
  IElementType LTE = new NasmTokenType("LTE");
  IElementType MACRO_ALIASES = new NasmTokenType("MACRO_ALIASES");
  IElementType MACRO_ARG_DECL = new NasmTokenType("MACRO_ARG_DECL");
  IElementType MACRO_ASSIGN = new NasmTokenType("MACRO_ASSIGN");
  IElementType MACRO_CLEAR = new NasmTokenType("MACRO_CLEAR");
  IElementType MACRO_DEFINE = new NasmTokenType("MACRO_DEFINE");
  IElementType MACRO_DEPEND = new NasmTokenType("MACRO_DEPEND");
  IElementType MACRO_ELIF = new NasmTokenType("MACRO_ELIF");
  IElementType MACRO_ELIFMACRO = new NasmTokenType("MACRO_ELIFMACRO");
  IElementType MACRO_ELSE = new NasmTokenType("MACRO_ELSE");
  IElementType MACRO_END = new NasmTokenType("MACRO_END");
  IElementType MACRO_ENDIF = new NasmTokenType("MACRO_ENDIF");
  IElementType MACRO_ENDREP = new NasmTokenType("MACRO_ENDREP");
  IElementType MACRO_ENDWHILE = new NasmTokenType("MACRO_ENDWHILE");
  IElementType MACRO_ERROR = new NasmTokenType("MACRO_ERROR");
  IElementType MACRO_EXITMACRO = new NasmTokenType("MACRO_EXITMACRO");
  IElementType MACRO_EXITREP = new NasmTokenType("MACRO_EXITREP");
  IElementType MACRO_EXPANSION_START = new NasmTokenType("MACRO_EXPANSION_START");
  IElementType MACRO_EXPLICIT_START = new NasmTokenType("MACRO_EXPLICIT_START");
  IElementType MACRO_IF = new NasmTokenType("MACRO_IF");
  IElementType MACRO_IFDEF = new NasmTokenType("MACRO_IFDEF");
  IElementType MACRO_IFMACRO = new NasmTokenType("MACRO_IFMACRO");
  IElementType MACRO_IFNDEF = new NasmTokenType("MACRO_IFNDEF");
  IElementType MACRO_INCLUDE = new NasmTokenType("MACRO_INCLUDE");
  IElementType MACRO_LINE = new NasmTokenType("MACRO_LINE");
  IElementType MACRO_LOCAL = new NasmTokenType("MACRO_LOCAL");
  IElementType MACRO_LOCAL_REF = new NasmTokenType("MACRO_LOCAL_REF");
  IElementType MACRO_PARAM = new NasmTokenType("MACRO_PARAM");
  IElementType MACRO_PARAM_GREEDY = new NasmTokenType("MACRO_PARAM_GREEDY");
  IElementType MACRO_PARAM_REVERSE = new NasmTokenType("MACRO_PARAM_REVERSE");
  IElementType MACRO_PATHSEARCH = new NasmTokenType("MACRO_PATHSEARCH");
  IElementType MACRO_POP = new NasmTokenType("MACRO_POP");
  IElementType MACRO_PRAGMA = new NasmTokenType("MACRO_PRAGMA");
  IElementType MACRO_PUSH = new NasmTokenType("MACRO_PUSH");
  IElementType MACRO_REP = new NasmTokenType("MACRO_REP");
  IElementType MACRO_REPL = new NasmTokenType("MACRO_REPL");
  IElementType MACRO_ROTATE = new NasmTokenType("MACRO_ROTATE");
  IElementType MACRO_STACKSIZE = new NasmTokenType("MACRO_STACKSIZE");
  IElementType MACRO_START = new NasmTokenType("MACRO_START");
  IElementType MACRO_STRCAT = new NasmTokenType("MACRO_STRCAT");
  IElementType MACRO_STRLEN = new NasmTokenType("MACRO_STRLEN");
  IElementType MACRO_SUBSTR = new NasmTokenType("MACRO_SUBSTR");
  IElementType MACRO_UNDEF = new NasmTokenType("MACRO_UNDEF");
  IElementType MACRO_UNMACRO = new NasmTokenType("MACRO_UNMACRO");
  IElementType MACRO_USE = new NasmTokenType("MACRO_USE");
  IElementType MACRO_WHILE = new NasmTokenType("MACRO_WHILE");
  IElementType MAP_KW = new NasmTokenType("MAP_KW");
  IElementType MASK_REG = new NasmTokenType("MASK_REG");
  IElementType MINUS = new NasmTokenType("MINUS");
  IElementType MOD = new NasmTokenType("MOD");
  IElementType MUL = new NasmTokenType("MUL");
  IElementType NOT_EQUAL_1 = new NasmTokenType("NOT_EQUAL_1");
  IElementType NOT_EQUAL_2 = new NasmTokenType("NOT_EQUAL_2");
  IElementType NUMBER = new NasmTokenType("NUMBER");
  IElementType ORG_KW = new NasmTokenType("ORG_KW");
  IElementType PASTE_OP = new NasmTokenType("PASTE_OP");
  IElementType PIPE = new NasmTokenType("PIPE");
  IElementType PLUS = new NasmTokenType("PLUS");
  IElementType PREPROCESSOR_DIRECTIVE = new NasmTokenType("PREPROCESSOR_DIRECTIVE");
  IElementType QUERY = new NasmTokenType("QUERY");
  IElementType QUERY_EXPAND = new NasmTokenType("QUERY_EXPAND");
  IElementType QUESTION_MARK = new NasmTokenType("QUESTION_MARK");
  IElementType RBRACE = new NasmTokenType("RBRACE");
  IElementType RBRACKET = new NasmTokenType("RBRACKET");
  IElementType REGISTER = new NasmTokenType("REGISTER");
  IElementType REL = new NasmTokenType("REL");
  IElementType REQUIRED_KW = new NasmTokenType("REQUIRED_KW");
  IElementType ROUNDING = new NasmTokenType("ROUNDING");
  IElementType RPAREN = new NasmTokenType("RPAREN");
  IElementType RSHIFT = new NasmTokenType("RSHIFT");
  IElementType RSHIFT_COMPLETE = new NasmTokenType("RSHIFT_COMPLETE");
  IElementType SAE = new NasmTokenType("SAE");
  IElementType SECTALIGN_KW = new NasmTokenType("SECTALIGN_KW");
  IElementType SECTION_ATTR_KW = new NasmTokenType("SECTION_ATTR_KW");
  IElementType SECTION_KW = new NasmTokenType("SECTION_KW");
  IElementType SEG = new NasmTokenType("SEG");
  IElementType SEGMENT_KW = new NasmTokenType("SEGMENT_KW");
  IElementType SEG_REGISTER = new NasmTokenType("SEG_REGISTER");
  IElementType SIGNED_DIV = new NasmTokenType("SIGNED_DIV");
  IElementType SIGNED_MOD = new NasmTokenType("SIGNED_MOD");
  IElementType SIZE_SPEC = new NasmTokenType("SIZE_SPEC");
  IElementType SPACESHIP = new NasmTokenType("SPACESHIP");
  IElementType SPACE_SIZE = new NasmTokenType("SPACE_SIZE");
  IElementType SPECIAL_FLOAT = new NasmTokenType("SPECIAL_FLOAT");
  IElementType STATIC_KW = new NasmTokenType("STATIC_KW");
  IElementType STRICT = new NasmTokenType("STRICT");
  IElementType STRING = new NasmTokenType("STRING");
  IElementType STRING_FUNC = new NasmTokenType("STRING_FUNC");
  IElementType STRUC_KW = new NasmTokenType("STRUC_KW");
  IElementType TILDE = new NasmTokenType("TILDE");
  IElementType TIMES = new NasmTokenType("TIMES");
  IElementType USE16_KW = new NasmTokenType("USE16_KW");
  IElementType USE32_KW = new NasmTokenType("USE32_KW");
  IElementType USE64_KW = new NasmTokenType("USE64_KW");
  IElementType WRT = new NasmTokenType("WRT");
  IElementType WRT_SUFFIX = new NasmTokenType("WRT_SUFFIX");
  IElementType ZEROING = new NasmTokenType("ZEROING");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ABSOLUTE_DIR) {
        return new NasmAbsoluteDirImpl(node);
      }
      else if (type == ALIGN_DIR) {
        return new NasmAlignDirImpl(node);
      }
      else if (type == ARG_DIRECTIVE) {
        return new NasmArgDirectiveImpl(node);
      }
      else if (type == ASSIGNMENT) {
        return new NasmAssignmentImpl(node);
      }
      else if (type == ATOM_EXPR) {
        return new NasmAtomExprImpl(node);
      }
      else if (type == AT_DIRECTIVE) {
        return new NasmAtDirectiveImpl(node);
      }
      else if (type == BITS_DIR) {
        return new NasmBitsDirImpl(node);
      }
      else if (type == BRACKETED_DIRECTIVE) {
        return new NasmBracketedDirectiveImpl(node);
      }
      else if (type == BUILTIN_FUNCTION) {
        return new NasmBuiltinFunctionImpl(node);
      }
      else if (type == CLEAR_DIR) {
        return new NasmClearDirImpl(node);
      }
      else if (type == CLEAR_TARGET) {
        return new NasmClearTargetImpl(node);
      }
      else if (type == COMMON_DIR) {
        return new NasmCommonDirImpl(node);
      }
      else if (type == CONDITION) {
        return new NasmConditionImpl(node);
      }
      else if (type == CONDITIONAL_BLOCK) {
        return new NasmConditionalBlockImpl(node);
      }
      else if (type == CONDITIONAL_DATA) {
        return new NasmConditionalDataImpl(node);
      }
      else if (type == CONDITIONAL_OPERAND) {
        return new NasmConditionalOperandImpl(node);
      }
      else if (type == CONTEXT_DIR) {
        return new NasmContextDirImpl(node);
      }
      else if (type == CONTEXT_NAME) {
        return new NasmContextNameImpl(node);
      }
      else if (type == CONTEXT_REF) {
        return new NasmContextRefImpl(node);
      }
      else if (type == CPU_DIR) {
        return new NasmCpuDirImpl(node);
      }
      else if (type == DATA_DEF) {
        return new NasmDataDefImpl(node);
      }
      else if (type == DATA_ITEM) {
        return new NasmDataItemImpl(node);
      }
      else if (type == DATA_LIST) {
        return new NasmDataListImpl(node);
      }
      else if (type == DECORATOR) {
        return new NasmDecoratorImpl(node);
      }
      else if (type == DECORATOR_ITEM) {
        return new NasmDecoratorItemImpl(node);
      }
      else if (type == DECORATOR_LIST) {
        return new NasmDecoratorListImpl(node);
      }
      else if (type == DEFAULT_DIR) {
        return new NasmDefaultDirImpl(node);
      }
      else if (type == DEFAULT_OPTION) {
        return new NasmDefaultOptionImpl(node);
      }
      else if (type == DEFAULT_VALUE) {
        return new NasmDefaultValueImpl(node);
      }
      else if (type == DEFINED_FUNC) {
        return new NasmDefinedFuncImpl(node);
      }
      else if (type == EA_EXPR) {
        return new NasmEaExprImpl(node);
      }
      else if (type == EA_TERM) {
        return new NasmEaTermImpl(node);
      }
      else if (type == EFFECTIVE_ADDR) {
        return new NasmEffectiveAddrImpl(node);
      }
      else if (type == ELIF_DIR) {
        return new NasmElifDirImpl(node);
      }
      else if (type == ELSE_DIR) {
        return new NasmElseDirImpl(node);
      }
      else if (type == ENDIF_DIR) {
        return new NasmEndifDirImpl(node);
      }
      else if (type == ENV_VAR_REF) {
        return new NasmEnvVarRefImpl(node);
      }
      else if (type == EQU_DEFINITION) {
        return new NasmEquDefinitionImpl(node);
      }
      else if (type == EXIT_DIR) {
        return new NasmExitDirImpl(node);
      }
      else if (type == EXPANDED_OPERAND) {
        return new NasmExpandedOperandImpl(node);
      }
      else if (type == EXPRESSION) {
        return new NasmExpressionImpl(node);
      }
      else if (type == EXTERN_DIR) {
        return new NasmExternDirImpl(node);
      }
      else if (type == FLOAT_DIR) {
        return new NasmFloatDirImpl(node);
      }
      else if (type == FLOAT_FORMAT) {
        return new NasmFloatFormatImpl(node);
      }
      else if (type == FLOAT_VALUE) {
        return new NasmFloatValueImpl(node);
      }
      else if (type == FUNCTION_MACRO_CALL) {
        return new NasmFunctionMacroCallImpl(node);
      }
      else if (type == GLOBAL_DIR) {
        return new NasmGlobalDirImpl(node);
      }
      else if (type == GLOBAL_LABEL) {
        return new NasmGlobalLabelImpl(node);
      }
      else if (type == IF_DIR) {
        return new NasmIfDirImpl(node);
      }
      else if (type == INCLUDE_DIR) {
        return new NasmIncludeDirImpl(node);
      }
      else if (type == INSTRUCTION) {
        return new NasmInstructionImpl(node);
      }
      else if (type == INTEGER_FUNCTION) {
        return new NasmIntegerFunctionImpl(node);
      }
      else if (type == INVOCATION) {
        return new NasmInvocationImpl(node);
      }
      else if (type == ISTRUC_BLOCK) {
        return new NasmIstrucBlockImpl(node);
      }
      else if (type == KEYWORD_AS_NAME) {
        return new NasmKeywordAsNameImpl(node);
      }
      else if (type == LABEL_DEF) {
        return new NasmLabelDefImpl(node);
      }
      else if (type == LINE_DIR) {
        return new NasmLineDirImpl(node);
      }
      else if (type == LOCAL_DIRECTIVE) {
        return new NasmLocalDirectiveImpl(node);
      }
      else if (type == LOCAL_LABEL) {
        return new NasmLocalLabelImpl(node);
      }
      else if (type == LOCK_PREFIX) {
        return new NasmLockPrefixImpl(node);
      }
      else if (type == MACRO_ARG) {
        return new NasmMacroArgImpl(node);
      }
      else if (type == MACRO_ARGS) {
        return new NasmMacroArgsImpl(node);
      }
      else if (type == MACRO_BODY_INLINE) {
        return new NasmMacroBodyInlineImpl(node);
      }
      else if (type == MACRO_CALL) {
        return new NasmMacroCallImpl(node);
      }
      else if (type == MACRO_DIR) {
        return new NasmMacroDirImpl(node);
      }
      else if (type == MACRO_EXPANSION) {
        return new NasmMacroExpansionImpl(node);
      }
      else if (type == MACRO_FLAGS) {
        return new NasmMacroFlagsImpl(node);
      }
      else if (type == MACRO_LINES) {
        return new NasmMacroLinesImpl(node);
      }
      else if (type == MACRO_NAME) {
        return new NasmMacroNameImpl(node);
      }
      else if (type == MACRO_PARAMS) {
        return new NasmMacroParamsImpl(node);
      }
      else if (type == MACRO_PARAM_REF) {
        return new NasmMacroParamRefImpl(node);
      }
      else if (type == MACRO_REF) {
        return new NasmMacroRefImpl(node);
      }
      else if (type == MEMORY_EXPR) {
        return new NasmMemoryExprImpl(node);
      }
      else if (type == MEMORY_REF) {
        return new NasmMemoryRefImpl(node);
      }
      else if (type == MESSAGE_DIR) {
        return new NasmMessageDirImpl(node);
      }
      else if (type == MESSAGE_TEXT) {
        return new NasmMessageTextImpl(node);
      }
      else if (type == MNEMONIC) {
        return new NasmMnemonicImpl(node);
      }
      else if (type == MULTI_LINE_MACRO) {
        return new NasmMultiLineMacroImpl(node);
      }
      else if (type == OPERAND) {
        return new NasmOperandImpl(node);
      }
      else if (type == OPERAND_LIST) {
        return new NasmOperandListImpl(node);
      }
      else if (type == OPERATOR) {
        return new NasmOperatorImpl(node);
      }
      else if (type == ORG_DIR) {
        return new NasmOrgDirImpl(node);
      }
      else if (type == PACKAGE_NAME) {
        return new NasmPackageNameImpl(node);
      }
      else if (type == PARAM_COUNT) {
        return new NasmParamCountImpl(node);
      }
      else if (type == PARAM_LIST) {
        return new NasmParamListImpl(node);
      }
      else if (type == PARAM_NAME) {
        return new NasmParamNameImpl(node);
      }
      else if (type == PARAM_QUALIFIER) {
        return new NasmParamQualifierImpl(node);
      }
      else if (type == PARAM_SPEC) {
        return new NasmParamSpecImpl(node);
      }
      else if (type == PP_ASSIGN_STMT) {
        return new NasmPpAssignStmtImpl(node);
      }
      else if (type == PP_DEFINE_STMT) {
        return new NasmPpDefineStmtImpl(node);
      }
      else if (type == PREPROCESSOR_FUNCTION) {
        return new NasmPreprocessorFunctionImpl(node);
      }
      else if (type == PREPROCESSOR_ID) {
        return new NasmPreprocessorIdImpl(node);
      }
      else if (type == PREPROCESSOR_LINE) {
        return new NasmPreprocessorLineImpl(node);
      }
      else if (type == PREPROCESSOR_TOKEN) {
        return new NasmPreprocessorTokenImpl(node);
      }
      else if (type == PSEUDO_INSTRUCTION) {
        return new NasmPseudoInstructionImpl(node);
      }
      else if (type == QUERY_FUNCTION) {
        return new NasmQueryFunctionImpl(node);
      }
      else if (type == REP_BLOCK) {
        return new NasmRepBlockImpl(node);
      }
      else if (type == SCALE) {
        return new NasmScaleImpl(node);
      }
      else if (type == SECTION_ATTR) {
        return new NasmSectionAttrImpl(node);
      }
      else if (type == SECTION_ATTRS) {
        return new NasmSectionAttrsImpl(node);
      }
      else if (type == SECTION_ATTR_NAME) {
        return new NasmSectionAttrNameImpl(node);
      }
      else if (type == SECTION_DIR) {
        return new NasmSectionDirImpl(node);
      }
      else if (type == SECTION_NAME) {
        return new NasmSectionNameImpl(node);
      }
      else if (type == SEGMENT_OVERRIDE) {
        return new NasmSegmentOverrideImpl(node);
      }
      else if (type == SEGMENT_REG) {
        return new NasmSegmentRegImpl(node);
      }
      else if (type == SEPARATOR) {
        return new NasmSeparatorImpl(node);
      }
      else if (type == SIZE_OVERRIDE) {
        return new NasmSizeOverrideImpl(node);
      }
      else if (type == SMACRO_EXPANSION) {
        return new NasmSmacroExpansionImpl(node);
      }
      else if (type == SOURCE_LINE) {
        return new NasmSourceLineImpl(node);
      }
      else if (type == SOURCE_LINES) {
        return new NasmSourceLinesImpl(node);
      }
      else if (type == SPACE_DEF) {
        return new NasmSpaceDefImpl(node);
      }
      else if (type == SPECIAL_SYMBOL) {
        return new NasmSpecialSymbolImpl(node);
      }
      else if (type == STRINGIZE_OP) {
        return new NasmStringizeOpImpl(node);
      }
      else if (type == STRING_FUNCTION) {
        return new NasmStringFunctionImpl(node);
      }
      else if (type == STRING_OR_ENV) {
        return new NasmStringOrEnvImpl(node);
      }
      else if (type == STRING_TRANSFORM) {
        return new NasmStringTransformImpl(node);
      }
      else if (type == STRUC_BLOCK) {
        return new NasmStrucBlockImpl(node);
      }
      else if (type == SYMBOL_DECL) {
        return new NasmSymbolDeclImpl(node);
      }
      else if (type == SYMBOL_LIST) {
        return new NasmSymbolListImpl(node);
      }
      else if (type == SYMBOL_NAME) {
        return new NasmSymbolNameImpl(node);
      }
      else if (type == SYMBOL_REF) {
        return new NasmSymbolRefImpl(node);
      }
      else if (type == TIMES_EXPR) {
        return new NasmTimesExprImpl(node);
      }
      else if (type == TOKEN_COMPARISON) {
        return new NasmTokenComparisonImpl(node);
      }
      else if (type == TOKEN_SEQUENCE) {
        return new NasmTokenSequenceImpl(node);
      }
      else if (type == USE_PACKAGE) {
        return new NasmUsePackageImpl(node);
      }
      else if (type == VEX_PREFIX) {
        return new NasmVexPrefixImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
