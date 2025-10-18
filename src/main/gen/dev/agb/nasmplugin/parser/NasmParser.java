// This is a generated file. Not intended for manual editing.
package dev.agb.nasmplugin.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static dev.agb.nasmplugin.psi.NasmTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class NasmParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return program(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // ABSOLUTE_KW const_expr
  public static boolean absolute_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "absolute_dir")) return false;
    if (!nextTokenIs(builder_, ABSOLUTE_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ABSOLUTE_DIR, null);
    result_ = consumeToken(builder_, ABSOLUTE_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // mul_expr ((PLUS | MINUS) mul_expr)*
  public static boolean add_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<add expr>");
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && add_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((PLUS | MINUS) mul_expr)*
  private static boolean add_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!add_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "add_expr_1", pos_)) break;
    }
    return true;
  }

  // (PLUS | MINUS) mul_expr
  private static boolean add_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = add_expr_1_0_0(builder_, level_ + 1);
    result_ = result_ && mul_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS | MINUS
  private static boolean add_expr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    return result_;
  }

  /* ********************************************************** */
  // (ALIGN_KW | ALIGNB_KW) const_expr (COMMA align_fill)?
  //             | SECTALIGN_KW const_expr
  public static boolean align_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALIGN_DIR, "<align dir>");
    result_ = align_dir_0(builder_, level_ + 1);
    if (!result_) result_ = align_dir_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ALIGN_KW | ALIGNB_KW) const_expr (COMMA align_fill)?
  private static boolean align_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = align_dir_0_0(builder_, level_ + 1);
    result_ = result_ && const_expr(builder_, level_ + 1);
    result_ = result_ && align_dir_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ALIGN_KW | ALIGNB_KW
  private static boolean align_dir_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ALIGN_KW);
    if (!result_) result_ = consumeToken(builder_, ALIGNB_KW);
    return result_;
  }

  // (COMMA align_fill)?
  private static boolean align_dir_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir_0_2")) return false;
    align_dir_0_2_0(builder_, level_ + 1);
    return true;
  }

  // COMMA align_fill
  private static boolean align_dir_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && align_fill(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SECTALIGN_KW const_expr
  private static boolean align_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SECTALIGN_KW);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DATA_SIZE data_list | const_expr
  static boolean align_fill(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_fill")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = align_fill_0(builder_, level_ + 1);
    if (!result_) result_ = const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // DATA_SIZE data_list
  private static boolean align_fill_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "align_fill_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DATA_SIZE);
    result_ = result_ && data_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // shift_expr (AMP shift_expr)*
  public static boolean and_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<and expr>");
    result_ = shift_expr(builder_, level_ + 1);
    result_ = result_ && and_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (AMP shift_expr)*
  private static boolean and_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!and_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "and_expr_1", pos_)) break;
    }
    return true;
  }

  // AMP shift_expr
  private static boolean and_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AMP);
    result_ = result_ && shift_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER (COMMA IDENTIFIER)*
  public static boolean arg_directive(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_directive")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && arg_directive_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, ARG_DIRECTIVE, result_);
    return result_;
  }

  // (COMMA IDENTIFIER)*
  private static boolean arg_directive_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_directive_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!arg_directive_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "arg_directive_1", pos_)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean arg_directive_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_directive_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // equ_definition
  //              | pp_assign_stmt
  //              | pp_define_stmt
  public static boolean assignment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignment")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT, "<assignment>");
    result_ = equ_definition(builder_, level_ + 1);
    if (!result_) result_ = pp_assign_stmt(builder_, level_ + 1);
    if (!result_) result_ = pp_define_stmt(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // AT_KW symbol_ref COMMA data_def
  public static boolean at_directive(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "at_directive")) return false;
    if (!nextTokenIs(builder_, AT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, AT_DIRECTIVE, null);
    result_ = consumeToken(builder_, AT_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, symbol_ref(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, COMMA)) && result_;
    result_ = pinned_ && data_def(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // LPAREN expression RPAREN
  //             | LBRACE brace_token_sequence RBRACE  // Brace-wrapped token sequence (for macro arguments)
  //             | function_macro_call     // Function-like macro: THREAD_FRAME(r14)
  //             | builtin_function        // Built-in functions
  //             | macro_expansion         // Macro expansions: %{...}, %[...]
  //             | env_var_ref             // Environment variables
  //             | float_format            // Float format conversions
  //             | register                // Registers
  //             | symbol_ref              // Plain identifiers
  //             | NUMBER
  //             | STRING
  //             | FLOAT
  //             | SPECIAL_FLOAT
  public static boolean atom_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "atom_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ATOM_EXPR, "<atom expr>");
    result_ = atom_expr_0(builder_, level_ + 1);
    if (!result_) result_ = atom_expr_1(builder_, level_ + 1);
    if (!result_) result_ = function_macro_call(builder_, level_ + 1);
    if (!result_) result_ = builtin_function(builder_, level_ + 1);
    if (!result_) result_ = macro_expansion(builder_, level_ + 1);
    if (!result_) result_ = env_var_ref(builder_, level_ + 1);
    if (!result_) result_ = float_format(builder_, level_ + 1);
    if (!result_) result_ = register(builder_, level_ + 1);
    if (!result_) result_ = symbol_ref(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, SPECIAL_FLOAT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LPAREN expression RPAREN
  private static boolean atom_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "atom_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LBRACE brace_token_sequence RBRACE
  private static boolean atom_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "atom_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && brace_token_sequence(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // preprocessor_id
  //                               | macro_expansion
  //                               | macro_param_ref
  //                               | context_ref
  //                               | stringize_op
  //                               | IDENTIFIER
  //                               | NUMBER
  //                               | STRING
  //                               | REGISTER
  //                               | SEG_REGISTER
  //                               | MASK_REG
  //                               | SIZE_SPEC
  //                               | DATA_SIZE
  //                               | SPACE_SIZE
  //                               | BUILTIN_FUNC
  //                               | STRING_FUNC
  //                               | FLOAT
  //                               | SPECIAL_FLOAT
  //                               | operator
  //                               | separator
  static boolean base_token_element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "base_token_element")) return false;
    boolean result_;
    result_ = preprocessor_id(builder_, level_ + 1);
    if (!result_) result_ = macro_expansion(builder_, level_ + 1);
    if (!result_) result_ = macro_param_ref(builder_, level_ + 1);
    if (!result_) result_ = context_ref(builder_, level_ + 1);
    if (!result_) result_ = stringize_op(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, REGISTER);
    if (!result_) result_ = consumeToken(builder_, SEG_REGISTER);
    if (!result_) result_ = consumeToken(builder_, MASK_REG);
    if (!result_) result_ = consumeToken(builder_, SIZE_SPEC);
    if (!result_) result_ = consumeToken(builder_, DATA_SIZE);
    if (!result_) result_ = consumeToken(builder_, SPACE_SIZE);
    if (!result_) result_ = consumeToken(builder_, BUILTIN_FUNC);
    if (!result_) result_ = consumeToken(builder_, STRING_FUNC);
    if (!result_) result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, SPECIAL_FLOAT);
    if (!result_) result_ = operator(builder_, level_ + 1);
    if (!result_) result_ = separator(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // BITS_KW const_expr
  //            | USE16_KW
  //            | USE32_KW
  //            | USE64_KW
  public static boolean bits_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bits_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BITS_DIR, "<bits dir>");
    result_ = bits_dir_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, USE16_KW);
    if (!result_) result_ = consumeToken(builder_, USE32_KW);
    if (!result_) result_ = consumeToken(builder_, USE64_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // BITS_KW const_expr
  private static boolean bits_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bits_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BITS_KW);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !RBRACE preprocessor_token
  static boolean brace_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "brace_token")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = brace_token_0(builder_, level_ + 1);
    result_ = result_ && preprocessor_token(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !RBRACE
  private static boolean brace_token_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "brace_token_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // brace_token+
  static boolean brace_token_sequence(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "brace_token_sequence")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = brace_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!brace_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "brace_token_sequence", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACKET DEFAULT_KW default_option RBRACKET
  //                       | LBRACKET IDENTIFIER IDENTIFIER+ RBRACKET
  public static boolean bracketed_directive(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketed_directive")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = bracketed_directive_0(builder_, level_ + 1);
    if (!result_) result_ = bracketed_directive_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, BRACKETED_DIRECTIVE, result_);
    return result_;
  }

  // LBRACKET DEFAULT_KW default_option RBRACKET
  private static boolean bracketed_directive_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketed_directive_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACKET, DEFAULT_KW);
    result_ = result_ && default_option(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LBRACKET IDENTIFIER IDENTIFIER+ RBRACKET
  private static boolean bracketed_directive_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketed_directive_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACKET, IDENTIFIER);
    result_ = result_ && bracketed_directive_1_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // IDENTIFIER+
  private static boolean bracketed_directive_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketed_directive_1_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, IDENTIFIER)) break;
      if (!empty_element_parsed_guard_(builder_, "bracketed_directive_1_2", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // integer_function
  //                    | preprocessor_function
  //                    | string_transform
  //                    | query_function
  public static boolean builtin_function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "builtin_function")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BUILTIN_FUNCTION, "<builtin function>");
    result_ = integer_function(builder_, level_ + 1);
    if (!result_) result_ = preprocessor_function(builder_, level_ + 1);
    if (!result_) result_ = string_transform(builder_, level_ + 1);
    if (!result_) result_ = query_function(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_CLEAR [clear_target]
  //             | MACRO_UNDEF IDENTIFIER
  //             | MACRO_UNMACRO IDENTIFIER [param_count]
  public static boolean clear_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CLEAR_DIR, "<clear dir>");
    result_ = clear_dir_0(builder_, level_ + 1);
    if (!result_) result_ = parseTokens(builder_, 0, MACRO_UNDEF, IDENTIFIER);
    if (!result_) result_ = clear_dir_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_CLEAR [clear_target]
  private static boolean clear_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_CLEAR);
    result_ = result_ && clear_dir_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [clear_target]
  private static boolean clear_dir_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_dir_0_1")) return false;
    clear_target(builder_, level_ + 1);
    return true;
  }

  // MACRO_UNMACRO IDENTIFIER [param_count]
  private static boolean clear_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_dir_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_UNMACRO, IDENTIFIER);
    result_ = result_ && clear_dir_2_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [param_count]
  private static boolean clear_dir_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_dir_2_2")) return false;
    param_count(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean clear_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "clear_target")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, CLEAR_TARGET, result_);
    return result_;
  }

  /* ********************************************************** */
  // COMMON_KW IDENTIFIER const_expr
  public static boolean common_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "common_dir")) return false;
    if (!nextTokenIs(builder_, COMMON_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMMON_DIR, null);
    result_ = consumeTokens(builder_, 1, COMMON_KW, IDENTIFIER);
    pinned_ = result_; // pin = 1
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // logical_or_expr (comparison_op logical_or_expr)*
  public static boolean comparison_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<comparison expr>");
    result_ = logical_or_expr(builder_, level_ + 1);
    result_ = result_ && comparison_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (comparison_op logical_or_expr)*
  private static boolean comparison_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!comparison_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "comparison_expr_1", pos_)) break;
    }
    return true;
  }

  // comparison_op logical_or_expr
  private static boolean comparison_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comparison_op(builder_, level_ + 1);
    result_ = result_ && logical_or_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // EQ_EQ | NOT_EQUAL_1 | NOT_EQUAL_2 | LT | LTE | GT | GTE | SPACESHIP
  static boolean comparison_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comparison_op")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = consumeToken(builder_, NOT_EQUAL_1);
    if (!result_) result_ = consumeToken(builder_, NOT_EQUAL_2);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, LTE);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, GTE);
    if (!result_) result_ = consumeToken(builder_, SPACESHIP);
    return result_;
  }

  /* ********************************************************** */
  // token_comparison    // Comma-separated token comparison (for %ifidn)
  //             | const_expr
  public static boolean condition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "condition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITION, "<condition>");
    result_ = token_comparison(builder_, level_ + 1);
    if (!result_) result_ = const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // if_dir CRLF conditional_content endif_dir [CRLF]
  public static boolean conditional_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_block")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITIONAL_BLOCK, "<conditional block>");
    result_ = if_dir(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, CRLF));
    result_ = pinned_ && report_error_(builder_, conditional_content(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, endif_dir(builder_, level_ + 1)) && result_;
    result_ = pinned_ && conditional_block_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [CRLF]
  private static boolean conditional_block_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_block_4")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // conditional_source_lines elif_or_else_part?
  static boolean conditional_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = conditional_source_lines(builder_, level_ + 1);
    result_ = result_ && conditional_content_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // elif_or_else_part?
  private static boolean conditional_content_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_content_1")) return false;
    elif_or_else_part(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // MACRO_IF condition CRLF data_item CRLF
  //                      (MACRO_ELIF condition CRLF data_item CRLF)*
  //                      [MACRO_ELSE CRLF data_item CRLF]
  //                      MACRO_ENDIF
  public static boolean conditional_data(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_data")) return false;
    if (!nextTokenIs(builder_, MACRO_IF)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITIONAL_DATA, null);
    result_ = consumeToken(builder_, MACRO_IF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, condition(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, CRLF)) && result_;
    result_ = pinned_ && report_error_(builder_, data_item(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, CRLF)) && result_;
    result_ = pinned_ && report_error_(builder_, conditional_data_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, conditional_data_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, MACRO_ENDIF) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (MACRO_ELIF condition CRLF data_item CRLF)*
  private static boolean conditional_data_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_data_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!conditional_data_5_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "conditional_data_5", pos_)) break;
    }
    return true;
  }

  // MACRO_ELIF condition CRLF data_item CRLF
  private static boolean conditional_data_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_data_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ELIF);
    result_ = result_ && condition(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    result_ = result_ && data_item(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [MACRO_ELSE CRLF data_item CRLF]
  private static boolean conditional_data_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_data_6")) return false;
    conditional_data_6_0(builder_, level_ + 1);
    return true;
  }

  // MACRO_ELSE CRLF data_item CRLF
  private static boolean conditional_data_6_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_data_6_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_ELSE, CRLF);
    result_ = result_ && data_item(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ELIF | MACRO_ELIFMACRO | MACRO_ELSE | MACRO_ENDIF
  static boolean conditional_marker(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_marker")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MACRO_ELIF);
    if (!result_) result_ = consumeToken(builder_, MACRO_ELIFMACRO);
    if (!result_) result_ = consumeToken(builder_, MACRO_ELSE);
    if (!result_) result_ = consumeToken(builder_, MACRO_ENDIF);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_IF condition CRLF operand CRLF
  //                         (MACRO_ELIF condition CRLF operand CRLF)*
  //                         [MACRO_ELSE CRLF operand CRLF]
  //                         MACRO_ENDIF
  public static boolean conditional_operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_operand")) return false;
    if (!nextTokenIs(builder_, MACRO_IF)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONDITIONAL_OPERAND, null);
    result_ = consumeToken(builder_, MACRO_IF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, condition(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, CRLF)) && result_;
    result_ = pinned_ && report_error_(builder_, operand(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, CRLF)) && result_;
    result_ = pinned_ && report_error_(builder_, conditional_operand_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, conditional_operand_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, MACRO_ENDIF) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (MACRO_ELIF condition CRLF operand CRLF)*
  private static boolean conditional_operand_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_operand_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!conditional_operand_5_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "conditional_operand_5", pos_)) break;
    }
    return true;
  }

  // MACRO_ELIF condition CRLF operand CRLF
  private static boolean conditional_operand_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_operand_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ELIF);
    result_ = result_ && condition(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    result_ = result_ && operand(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [MACRO_ELSE CRLF operand CRLF]
  private static boolean conditional_operand_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_operand_6")) return false;
    conditional_operand_6_0(builder_, level_ + 1);
    return true;
  }

  // MACRO_ELSE CRLF operand CRLF
  private static boolean conditional_operand_6_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_operand_6_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_ELSE, CRLF);
    result_ = result_ && operand(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CRLF);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // (!conditional_marker source_line)*
  static boolean conditional_source_lines(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_source_lines")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!conditional_source_lines_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "conditional_source_lines", pos_)) break;
    }
    return true;
  }

  // !conditional_marker source_line
  private static boolean conditional_source_lines_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_source_lines_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = conditional_source_lines_0_0(builder_, level_ + 1);
    result_ = result_ && source_line(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !conditional_marker
  private static boolean conditional_source_lines_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_source_lines_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !conditional_marker(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // expression
  static boolean const_expr(PsiBuilder builder_, int level_) {
    return expression(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // MACRO_PUSH [context_name]
  //               | MACRO_POP
  //               | MACRO_REPL context_name
  //               | MACRO_ARG_DECL arg_directive
  //               | MACRO_STACKSIZE const_expr
  //               | MACRO_LOCAL local_directive
  public static boolean context_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTEXT_DIR, "<context dir>");
    result_ = context_dir_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, MACRO_POP);
    if (!result_) result_ = context_dir_2(builder_, level_ + 1);
    if (!result_) result_ = context_dir_3(builder_, level_ + 1);
    if (!result_) result_ = context_dir_4(builder_, level_ + 1);
    if (!result_) result_ = context_dir_5(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_PUSH [context_name]
  private static boolean context_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_PUSH);
    result_ = result_ && context_dir_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [context_name]
  private static boolean context_dir_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_0_1")) return false;
    context_name(builder_, level_ + 1);
    return true;
  }

  // MACRO_REPL context_name
  private static boolean context_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_REPL);
    result_ = result_ && context_name(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_ARG_DECL arg_directive
  private static boolean context_dir_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ARG_DECL);
    result_ = result_ && arg_directive(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_STACKSIZE const_expr
  private static boolean context_dir_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_STACKSIZE);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_LOCAL local_directive
  private static boolean context_dir_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_dir_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_LOCAL);
    result_ = result_ && local_directive(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // CONTEXT_LOCAL_REF | MACRO_LOCAL_REF
  static boolean context_local_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_local_ref")) return false;
    if (!nextTokenIs(builder_, "", CONTEXT_LOCAL_REF, MACRO_LOCAL_REF)) return false;
    boolean result_;
    result_ = consumeToken(builder_, CONTEXT_LOCAL_REF);
    if (!result_) result_ = consumeToken(builder_, MACRO_LOCAL_REF);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean context_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_name")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, CONTEXT_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_LOCAL_REF | CONTEXT_LOCAL_REF
  public static boolean context_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "context_ref")) return false;
    if (!nextTokenIs(builder_, "<context ref>", CONTEXT_LOCAL_REF, MACRO_LOCAL_REF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTEXT_REF, "<context ref>");
    result_ = consumeToken(builder_, MACRO_LOCAL_REF);
    if (!result_) result_ = consumeToken(builder_, CONTEXT_LOCAL_REF);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // CPU_KW IDENTIFIER
  public static boolean cpu_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cpu_dir")) return false;
    if (!nextTokenIs(builder_, CPU_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CPU_DIR, null);
    result_ = consumeTokens(builder_, 1, CPU_KW, IDENTIFIER);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // [times_expr] DATA_SIZE data_list
  public static boolean data_def(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_def")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DATA_DEF, "<data def>");
    result_ = data_def_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DATA_SIZE);
    pinned_ = result_; // pin = 2
    result_ = result_ && data_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [times_expr]
  private static boolean data_def_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_def_0")) return false;
    times_expr(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // const_expr [DUP LPAREN data_list RPAREN]
  static boolean data_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = const_expr(builder_, level_ + 1);
    result_ = result_ && data_expr_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [DUP LPAREN data_list RPAREN]
  private static boolean data_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_expr_1")) return false;
    data_expr_1_0(builder_, level_ + 1);
    return true;
  }

  // DUP LPAREN data_list RPAREN
  private static boolean data_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DUP, LPAREN);
    result_ = result_ && data_list(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // string_function       // Try specific patterns first
  //             | conditional_data
  //             | QUESTION_MARK
  //             | data_expr                    // Expressions (including optional DUP)
  //             | macro_expansion
  public static boolean data_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DATA_ITEM, "<data item>");
    result_ = string_function(builder_, level_ + 1);
    if (!result_) result_ = conditional_data(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, QUESTION_MARK);
    if (!result_) result_ = data_expr(builder_, level_ + 1);
    if (!result_) result_ = macro_expansion(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // data_item (COMMA data_item)*
  public static boolean data_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DATA_LIST, "<data list>");
    result_ = data_item(builder_, level_ + 1);
    result_ = result_ && data_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA data_item)*
  private static boolean data_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!data_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "data_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA data_item
  private static boolean data_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && data_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE decorator_list RBRACE
  public static boolean decorator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decorator")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECORATOR, null);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && decorator_list(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // MASK_REG | ZEROING | BROADCAST | ROUNDING | SAE
  public static boolean decorator_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decorator_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECORATOR_ITEM, "<decorator item>");
    result_ = consumeToken(builder_, MASK_REG);
    if (!result_) result_ = consumeToken(builder_, ZEROING);
    if (!result_) result_ = consumeToken(builder_, BROADCAST);
    if (!result_) result_ = consumeToken(builder_, ROUNDING);
    if (!result_) result_ = consumeToken(builder_, SAE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // decorator_item (COMMA decorator_item)*
  public static boolean decorator_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decorator_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECORATOR_LIST, "<decorator list>");
    result_ = decorator_item(builder_, level_ + 1);
    result_ = result_ && decorator_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA decorator_item)*
  private static boolean decorator_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decorator_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!decorator_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "decorator_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA decorator_item
  private static boolean decorator_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decorator_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && decorator_item(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DEFAULT_KW default_option
  public static boolean default_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_dir")) return false;
    if (!nextTokenIs(builder_, DEFAULT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFAULT_DIR, null);
    result_ = consumeToken(builder_, DEFAULT_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && default_option(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // REL | ABS | INSTRUCTION_PREFIX
  public static boolean default_option(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_option")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFAULT_OPTION, "<default option>");
    result_ = consumeToken(builder_, REL);
    if (!result_) result_ = consumeToken(builder_, ABS);
    if (!result_) result_ = consumeToken(builder_, INSTRUCTION_PREFIX);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // token_sequence
  public static boolean default_value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFAULT_VALUE, "<default value>");
    result_ = token_sequence(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // DEFINED LPAREN IDENTIFIER RPAREN
  public static boolean defined_func(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defined_func")) return false;
    if (!nextTokenIs(builder_, DEFINED)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DEFINED_FUNC, null);
    result_ = consumeTokens(builder_, 1, DEFINED, LPAREN, IDENTIFIER, RPAREN);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ea_term ((PLUS | MINUS) ea_term)*
  public static boolean ea_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EA_EXPR, "<ea expr>");
    result_ = ea_term(builder_, level_ + 1);
    result_ = result_ && ea_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((PLUS | MINUS) ea_term)*
  private static boolean ea_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ea_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ea_expr_1", pos_)) break;
    }
    return true;
  }

  // (PLUS | MINUS) ea_term
  private static boolean ea_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ea_expr_1_0_0(builder_, level_ + 1);
    result_ = result_ && ea_term(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS | MINUS
  private static boolean ea_expr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_expr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    return result_;
  }

  /* ********************************************************** */
  // REL symbol_ref        // RIP-relative addressing
  //           | register MUL scale     // Scaled register
  //           | register               // Plain register
  //           | const_expr
  //           | symbol_ref
  //           | macro_expansion
  public static boolean ea_term(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_term")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EA_TERM, "<ea term>");
    result_ = ea_term_0(builder_, level_ + 1);
    if (!result_) result_ = ea_term_1(builder_, level_ + 1);
    if (!result_) result_ = register(builder_, level_ + 1);
    if (!result_) result_ = const_expr(builder_, level_ + 1);
    if (!result_) result_ = symbol_ref(builder_, level_ + 1);
    if (!result_) result_ = macro_expansion(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // REL symbol_ref
  private static boolean ea_term_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_term_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, REL);
    result_ = result_ && symbol_ref(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // register MUL scale
  private static boolean ea_term_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ea_term_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = register(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MUL);
    result_ = result_ && scale(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [segment_reg COLON] ea_expr
  public static boolean effective_addr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "effective_addr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EFFECTIVE_ADDR, "<effective addr>");
    result_ = effective_addr_0(builder_, level_ + 1);
    result_ = result_ && ea_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [segment_reg COLON]
  private static boolean effective_addr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "effective_addr_0")) return false;
    effective_addr_0_0(builder_, level_ + 1);
    return true;
  }

  // segment_reg COLON
  private static boolean effective_addr_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "effective_addr_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = segment_reg(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // elif_dir CRLF conditional_source_lines
  static boolean elif_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_block")) return false;
    if (!nextTokenIs(builder_, "", MACRO_ELIF, MACRO_ELIFMACRO)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = elif_dir(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, CRLF));
    result_ = pinned_ && conditional_source_lines(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // elif_block+
  static boolean elif_blocks(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_blocks")) return false;
    if (!nextTokenIs(builder_, "", MACRO_ELIF, MACRO_ELIFMACRO)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = elif_block(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!elif_block(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "elif_blocks", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ELIF condition?
  //            | MACRO_ELIFMACRO macro_ref param_count
  public static boolean elif_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_dir")) return false;
    if (!nextTokenIs(builder_, "<elif dir>", MACRO_ELIF, MACRO_ELIFMACRO)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ELIF_DIR, "<elif dir>");
    result_ = elif_dir_0(builder_, level_ + 1);
    if (!result_) result_ = elif_dir_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_ELIF condition?
  private static boolean elif_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ELIF);
    result_ = result_ && elif_dir_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // condition?
  private static boolean elif_dir_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_dir_0_1")) return false;
    condition(builder_, level_ + 1);
    return true;
  }

  // MACRO_ELIFMACRO macro_ref param_count
  private static boolean elif_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ELIFMACRO);
    result_ = result_ && macro_ref(builder_, level_ + 1);
    result_ = result_ && param_count(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // elif_blocks else_block?
  //                             | else_block
  static boolean elif_or_else_part(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_or_else_part")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = elif_or_else_part_0(builder_, level_ + 1);
    if (!result_) result_ = else_block(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // elif_blocks else_block?
  private static boolean elif_or_else_part_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_or_else_part_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = elif_blocks(builder_, level_ + 1);
    result_ = result_ && elif_or_else_part_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // else_block?
  private static boolean elif_or_else_part_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "elif_or_else_part_0_1")) return false;
    else_block(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // else_dir CRLF conditional_source_lines
  static boolean else_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_block")) return false;
    if (!nextTokenIs(builder_, MACRO_ELSE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = else_dir(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, CRLF));
    result_ = pinned_ && conditional_source_lines(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // MACRO_ELSE
  public static boolean else_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_dir")) return false;
    if (!nextTokenIs(builder_, MACRO_ELSE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ELSE);
    exit_section_(builder_, marker_, ELSE_DIR, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ENDIF
  public static boolean endif_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "endif_dir")) return false;
    if (!nextTokenIs(builder_, MACRO_ENDIF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ENDIF);
    exit_section_(builder_, marker_, ENDIF_DIR, result_);
    return result_;
  }

  /* ********************************************************** */
  // ENV_VAR_PREFIX (IDENTIFIER | STRING)
  public static boolean env_var_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "env_var_ref")) return false;
    if (!nextTokenIs(builder_, ENV_VAR_PREFIX)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ENV_VAR_PREFIX);
    result_ = result_ && env_var_ref_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, ENV_VAR_REF, result_);
    return result_;
  }

  // IDENTIFIER | STRING
  private static boolean env_var_ref_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "env_var_ref_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, STRING);
    return result_;
  }

  /* ********************************************************** */
  // (IDENTIFIER | context_ref | macro_param_concat) EQU const_expr
  public static boolean equ_definition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equ_definition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EQU_DEFINITION, "<equ definition>");
    result_ = equ_definition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQU);
    pinned_ = result_; // pin = 2
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // IDENTIFIER | context_ref | macro_param_concat
  private static boolean equ_definition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equ_definition_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = context_ref(builder_, level_ + 1);
    if (!result_) result_ = macro_param_concat(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // equ_definition [CRLF]
  static boolean equ_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equ_line")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = equ_definition(builder_, level_ + 1);
    result_ = result_ && equ_line_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [CRLF]
  private static boolean equ_line_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equ_line_1")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // MACRO_EXITMACRO | MACRO_EXITREP
  public static boolean exit_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exit_dir")) return false;
    if (!nextTokenIs(builder_, "<exit dir>", MACRO_EXITMACRO, MACRO_EXITREP)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXIT_DIR, "<exit dir>");
    result_ = consumeToken(builder_, MACRO_EXITMACRO);
    if (!result_) result_ = consumeToken(builder_, MACRO_EXITREP);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // SIZE_SPEC const_expr          // Sized immediate: qword 0, byte 10
  //                    | SIZE_SPEC register             // Sized register (unusual but allowed)
  //                    | memory_expr                    // Memory expressions with segment overrides (try before const_expr)
  //                    | conditional_operand            // Conditional operands
  //                    | far_jump_operand               // Far jump (moved to separate rule to avoid ambiguity)
  //                    | register                       // Plain register (try before const_expr to avoid ambiguity)
  //                    | const_expr
  public static boolean expanded_operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expanded_operand")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPANDED_OPERAND, "<expanded operand>");
    result_ = expanded_operand_0(builder_, level_ + 1);
    if (!result_) result_ = expanded_operand_1(builder_, level_ + 1);
    if (!result_) result_ = memory_expr(builder_, level_ + 1);
    if (!result_) result_ = conditional_operand(builder_, level_ + 1);
    if (!result_) result_ = far_jump_operand(builder_, level_ + 1);
    if (!result_) result_ = register(builder_, level_ + 1);
    if (!result_) result_ = const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SIZE_SPEC const_expr
  private static boolean expanded_operand_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expanded_operand_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SIZE_SPEC);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SIZE_SPEC register
  private static boolean expanded_operand_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expanded_operand_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SIZE_SPEC);
    result_ = result_ && register(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // comparison_expr [wrt_suffix]
  public static boolean expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<expression>");
    result_ = comparison_expr(builder_, level_ + 1);
    result_ = result_ && expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [wrt_suffix]
  private static boolean expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_1")) return false;
    wrt_suffix(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // EXTERN_KW symbol_list
  public static boolean extern_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "extern_dir")) return false;
    if (!nextTokenIs(builder_, EXTERN_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXTERN_DIR, null);
    result_ = consumeToken(builder_, EXTERN_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && symbol_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // const_expr COLON const_expr
  static boolean far_jump_operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "far_jump_operand")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = const_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    pinned_ = result_; // pin = 2
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // FLOAT_KW IDENTIFIER
  public static boolean float_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "float_dir")) return false;
    if (!nextTokenIs(builder_, FLOAT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FLOAT_DIR, null);
    result_ = consumeTokens(builder_, 1, FLOAT_KW, IDENTIFIER);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // FLOAT_FUNC LPAREN float_value RPAREN
  public static boolean float_format(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "float_format")) return false;
    if (!nextTokenIs(builder_, FLOAT_FUNC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FLOAT_FORMAT, null);
    result_ = consumeTokens(builder_, 2, FLOAT_FUNC, LPAREN);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, float_value(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // FLOAT | NUMBER | SPECIAL_FLOAT
  public static boolean float_value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "float_value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FLOAT_VALUE, "<float value>");
    result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, SPECIAL_FLOAT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER LPAREN [macro_arg_list] RPAREN
  public static boolean function_macro_call(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_macro_call")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_MACRO_CALL, null);
    result_ = consumeTokens(builder_, 2, IDENTIFIER, LPAREN);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, function_macro_call_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [macro_arg_list]
  private static boolean function_macro_call_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_macro_call_2")) return false;
    macro_arg_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // GLOBAL_KW symbol_list
  //              | MACRO_PUSH symbol_list
  public static boolean global_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "global_dir")) return false;
    if (!nextTokenIs(builder_, "<global dir>", GLOBAL_KW, MACRO_PUSH)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GLOBAL_DIR, "<global dir>");
    result_ = global_dir_0(builder_, level_ + 1);
    if (!result_) result_ = global_dir_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // GLOBAL_KW symbol_list
  private static boolean global_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "global_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, GLOBAL_KW);
    result_ = result_ && symbol_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_PUSH symbol_list
  private static boolean global_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "global_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_PUSH);
    result_ = result_ && symbol_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_param_concat     // %1_end, %2_label, %$foo_end
  //                | context_ref           // %$label, %%label
  //                | IDENTIFIER
  public static boolean global_label(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "global_label")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GLOBAL_LABEL, "<global label>");
    result_ = macro_param_concat(builder_, level_ + 1);
    if (!result_) result_ = context_ref(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_IF condition?
  //          | MACRO_IFDEF macro_ref
  //          | MACRO_IFNDEF macro_ref
  //          | MACRO_IFMACRO macro_ref param_count
  public static boolean if_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_DIR, "<if dir>");
    result_ = if_dir_0(builder_, level_ + 1);
    if (!result_) result_ = if_dir_1(builder_, level_ + 1);
    if (!result_) result_ = if_dir_2(builder_, level_ + 1);
    if (!result_) result_ = if_dir_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_IF condition?
  private static boolean if_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_IF);
    result_ = result_ && if_dir_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // condition?
  private static boolean if_dir_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir_0_1")) return false;
    condition(builder_, level_ + 1);
    return true;
  }

  // MACRO_IFDEF macro_ref
  private static boolean if_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_IFDEF);
    result_ = result_ && macro_ref(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_IFNDEF macro_ref
  private static boolean if_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_IFNDEF);
    result_ = result_ && macro_ref(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_IFMACRO macro_ref param_count
  private static boolean if_dir_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_dir_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_IFMACRO);
    result_ = result_ && macro_ref(builder_, level_ + 1);
    result_ = result_ && param_count(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // INCBIN_KW STRING (COMMA const_expr (COMMA const_expr)?)?
  //               | MACRO_INCLUDE string_or_env
  //               | MACRO_USE package_name
  public static boolean include_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INCLUDE_DIR, "<include dir>");
    result_ = include_dir_0(builder_, level_ + 1);
    if (!result_) result_ = include_dir_1(builder_, level_ + 1);
    if (!result_) result_ = include_dir_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // INCBIN_KW STRING (COMMA const_expr (COMMA const_expr)?)?
  private static boolean include_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, INCBIN_KW, STRING);
    result_ = result_ && include_dir_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA const_expr (COMMA const_expr)?)?
  private static boolean include_dir_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_0_2")) return false;
    include_dir_0_2_0(builder_, level_ + 1);
    return true;
  }

  // COMMA const_expr (COMMA const_expr)?
  private static boolean include_dir_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && const_expr(builder_, level_ + 1);
    result_ = result_ && include_dir_0_2_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA const_expr)?
  private static boolean include_dir_0_2_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_0_2_0_2")) return false;
    include_dir_0_2_0_2_0(builder_, level_ + 1);
    return true;
  }

  // COMMA const_expr
  private static boolean include_dir_0_2_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_0_2_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_INCLUDE string_or_env
  private static boolean include_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_INCLUDE);
    result_ = result_ && string_or_env(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_USE package_name
  private static boolean include_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "include_dir_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_USE);
    result_ = result_ && package_name(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [instruction_prefixes] mnemonic operand_list?
  public static boolean instruction(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INSTRUCTION, "<instruction>");
    result_ = instruction_0(builder_, level_ + 1);
    result_ = result_ && mnemonic(builder_, level_ + 1);
    result_ = result_ && instruction_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [instruction_prefixes]
  private static boolean instruction_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_0")) return false;
    instruction_prefixes(builder_, level_ + 1);
    return true;
  }

  // operand_list?
  private static boolean instruction_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_2")) return false;
    operand_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // [times_expr] [INSTRUCTION_PREFIX] [lock_prefix] [segment_override] [size_override] [vex_prefix]
  static boolean instruction_prefixes(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = instruction_prefixes_0(builder_, level_ + 1);
    result_ = result_ && instruction_prefixes_1(builder_, level_ + 1);
    result_ = result_ && instruction_prefixes_2(builder_, level_ + 1);
    result_ = result_ && instruction_prefixes_3(builder_, level_ + 1);
    result_ = result_ && instruction_prefixes_4(builder_, level_ + 1);
    result_ = result_ && instruction_prefixes_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [times_expr]
  private static boolean instruction_prefixes_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_0")) return false;
    times_expr(builder_, level_ + 1);
    return true;
  }

  // [INSTRUCTION_PREFIX]
  private static boolean instruction_prefixes_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_1")) return false;
    consumeToken(builder_, INSTRUCTION_PREFIX);
    return true;
  }

  // [lock_prefix]
  private static boolean instruction_prefixes_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_2")) return false;
    lock_prefix(builder_, level_ + 1);
    return true;
  }

  // [segment_override]
  private static boolean instruction_prefixes_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_3")) return false;
    segment_override(builder_, level_ + 1);
    return true;
  }

  // [size_override]
  private static boolean instruction_prefixes_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_4")) return false;
    size_override(builder_, level_ + 1);
    return true;
  }

  // [vex_prefix]
  private static boolean instruction_prefixes_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "instruction_prefixes_5")) return false;
    vex_prefix(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // BUILTIN_FUNC LPAREN const_expr RPAREN
  public static boolean integer_function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "integer_function")) return false;
    if (!nextTokenIs(builder_, BUILTIN_FUNC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTEGER_FUNCTION, null);
    result_ = consumeTokens(builder_, 2, BUILTIN_FUNC, LPAREN);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, const_expr(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // macro_call
  //              | smacro_expansion
  //              | builtin_function
  //              | macro_param_invocation
  public static boolean invocation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "invocation")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INVOCATION, "<invocation>");
    result_ = macro_call(builder_, level_ + 1);
    if (!result_) result_ = smacro_expansion(builder_, level_ + 1);
    if (!result_) result_ = builtin_function(builder_, level_ + 1);
    if (!result_) result_ = macro_param_invocation(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ISTRUC_KW IDENTIFIER CRLF source_lines CRLF* IEND_KW
  public static boolean istruc_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "istruc_block")) return false;
    if (!nextTokenIs(builder_, ISTRUC_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ISTRUC_BLOCK, null);
    result_ = consumeTokens(builder_, 1, ISTRUC_KW, IDENTIFIER, CRLF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, source_lines(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, istruc_block_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, IEND_KW) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CRLF*
  private static boolean istruc_block_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "istruc_block_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, CRLF)) break;
      if (!empty_element_parsed_guard_(builder_, "istruc_block_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ABS | REL | SEG | STRICT | DUP | WRT | AT_KW | DEFAULT_KW
  public static boolean keyword_as_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "keyword_as_name")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, KEYWORD_AS_NAME, "<keyword as name>");
    result_ = consumeToken(builder_, ABS);
    if (!result_) result_ = consumeToken(builder_, REL);
    if (!result_) result_ = consumeToken(builder_, SEG);
    if (!result_) result_ = consumeToken(builder_, STRICT);
    if (!result_) result_ = consumeToken(builder_, DUP);
    if (!result_) result_ = consumeToken(builder_, WRT);
    if (!result_) result_ = consumeToken(builder_, AT_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // global_label COLON?
  //             | local_label COLON?
  public static boolean label_def(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_def")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LABEL_DEF, "<label def>");
    result_ = label_def_0(builder_, level_ + 1);
    if (!result_) result_ = label_def_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // global_label COLON?
  private static boolean label_def_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_def_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = global_label(builder_, level_ + 1);
    result_ = result_ && label_def_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COLON?
  private static boolean label_def_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_def_0_1")) return false;
    consumeToken(builder_, COLON);
    return true;
  }

  // local_label COLON?
  private static boolean label_def_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_def_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = local_label(builder_, level_ + 1);
    result_ = result_ && label_def_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COLON?
  private static boolean label_def_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_def_1_1")) return false;
    consumeToken(builder_, COLON);
    return true;
  }

  /* ********************************************************** */
  // global_label COLON
  //                   | local_label COLON
  public static boolean label_with_colon(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LABEL_DEF, "<label with colon>");
    result_ = label_with_colon_0(builder_, level_ + 1);
    if (!result_) result_ = label_with_colon_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // global_label COLON
  private static boolean label_with_colon_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = global_label(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // local_label COLON
  private static boolean label_with_colon_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = local_label(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // label_with_colon [non_assignment_statement] [CRLF]
  static boolean label_with_colon_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon_line")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = label_with_colon(builder_, level_ + 1);
    result_ = result_ && label_with_colon_line_1(builder_, level_ + 1);
    result_ = result_ && label_with_colon_line_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [non_assignment_statement]
  private static boolean label_with_colon_line_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon_line_1")) return false;
    non_assignment_statement(builder_, level_ + 1);
    return true;
  }

  // [CRLF]
  private static boolean label_with_colon_line_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_colon_line_2")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // label_without_colon (data_def | space_def) [CRLF]
  static boolean label_with_data_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_data_line")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = label_without_colon(builder_, level_ + 1);
    result_ = result_ && label_with_data_line_1(builder_, level_ + 1);
    result_ = result_ && label_with_data_line_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // data_def | space_def
  private static boolean label_with_data_line_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_data_line_1")) return false;
    boolean result_;
    result_ = data_def(builder_, level_ + 1);
    if (!result_) result_ = space_def(builder_, level_ + 1);
    return result_;
  }

  // [CRLF]
  private static boolean label_with_data_line_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_with_data_line_2")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // global_label
  //                      | local_label
  public static boolean label_without_colon(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_without_colon")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LABEL_DEF, "<label without colon>");
    result_ = global_label(builder_, level_ + 1);
    if (!result_) result_ = local_label(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_LINE const_expr [STRING]
  public static boolean line_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_dir")) return false;
    if (!nextTokenIs(builder_, MACRO_LINE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LINE_DIR, null);
    result_ = consumeToken(builder_, MACRO_LINE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, const_expr(builder_, level_ + 1));
    result_ = pinned_ && line_dir_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [STRING]
  private static boolean line_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_dir_2")) return false;
    consumeToken(builder_, STRING);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER (COMMA IDENTIFIER)*
  public static boolean local_directive(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_directive")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && local_directive_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, LOCAL_DIRECTIVE, result_);
    return result_;
  }

  // (COMMA IDENTIFIER)*
  private static boolean local_directive_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_directive_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!local_directive_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "local_directive_1", pos_)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean local_directive_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_directive_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DOT_DOT? IDENTIFIER
  //               | MACRO_LOCAL_REF
  public static boolean local_label(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_label")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LOCAL_LABEL, "<local label>");
    result_ = local_label_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, MACRO_LOCAL_REF);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // DOT_DOT? IDENTIFIER
  private static boolean local_label_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_label_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = local_label_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // DOT_DOT?
  private static boolean local_label_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_label_0_0")) return false;
    consumeToken(builder_, DOT_DOT);
    return true;
  }

  /* ********************************************************** */
  // INSTRUCTION_PREFIX
  public static boolean lock_prefix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lock_prefix")) return false;
    if (!nextTokenIs(builder_, INSTRUCTION_PREFIX)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INSTRUCTION_PREFIX);
    exit_section_(builder_, marker_, LOCK_PREFIX, result_);
    return result_;
  }

  /* ********************************************************** */
  // or_expr (BOOLEAN_AND or_expr)*
  public static boolean logical_and_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_and_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<logical and expr>");
    result_ = or_expr(builder_, level_ + 1);
    result_ = result_ && logical_and_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (BOOLEAN_AND or_expr)*
  private static boolean logical_and_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_and_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!logical_and_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logical_and_expr_1", pos_)) break;
    }
    return true;
  }

  // BOOLEAN_AND or_expr
  private static boolean logical_and_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_and_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BOOLEAN_AND);
    result_ = result_ && or_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // logical_xor_expr (BOOLEAN_OR logical_xor_expr)*
  public static boolean logical_or_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_or_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<logical or expr>");
    result_ = logical_xor_expr(builder_, level_ + 1);
    result_ = result_ && logical_or_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (BOOLEAN_OR logical_xor_expr)*
  private static boolean logical_or_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_or_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!logical_or_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logical_or_expr_1", pos_)) break;
    }
    return true;
  }

  // BOOLEAN_OR logical_xor_expr
  private static boolean logical_or_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_or_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BOOLEAN_OR);
    result_ = result_ && logical_xor_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // logical_and_expr (BOOLEAN_XOR logical_and_expr)*
  public static boolean logical_xor_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_xor_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<logical xor expr>");
    result_ = logical_and_expr(builder_, level_ + 1);
    result_ = result_ && logical_xor_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (BOOLEAN_XOR logical_and_expr)*
  private static boolean logical_xor_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_xor_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!logical_xor_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logical_xor_expr_1", pos_)) break;
    }
    return true;
  }

  // BOOLEAN_XOR logical_and_expr
  private static boolean logical_xor_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logical_xor_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BOOLEAN_XOR);
    result_ = result_ && logical_and_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE token_sequence RBRACE
  //             | macro_arg_tokens
  public static boolean macro_arg(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_ARG, "<macro arg>");
    result_ = macro_arg_0(builder_, level_ + 1);
    if (!result_) result_ = macro_arg_tokens(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LBRACE token_sequence RBRACE
  private static boolean macro_arg_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && token_sequence(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_arg (COMMA macro_arg)*
  static boolean macro_arg_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_arg(builder_, level_ + 1);
    result_ = result_ && macro_arg_list_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA macro_arg)*
  private static boolean macro_arg_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!macro_arg_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_arg_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA macro_arg
  private static boolean macro_arg_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && macro_arg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(COMMA | RPAREN) base_token_element
  static boolean macro_arg_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_token")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_arg_token_0(builder_, level_ + 1);
    result_ = result_ && base_token_element(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(COMMA | RPAREN)
  private static boolean macro_arg_token_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_token_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !macro_arg_token_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // COMMA | RPAREN
  private static boolean macro_arg_token_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_token_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    return result_;
  }

  /* ********************************************************** */
  // macro_arg_token+
  static boolean macro_arg_tokens(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_arg_tokens")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_arg_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!macro_arg_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_arg_tokens", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_arg (COMMA macro_arg)*
  public static boolean macro_args(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_args")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_ARGS, "<macro args>");
    result_ = macro_arg(builder_, level_ + 1);
    result_ = result_ && macro_args_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA macro_arg)*
  private static boolean macro_args_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_args_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!macro_args_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_args_1", pos_)) break;
    }
    return true;
  }

  // COMMA macro_arg
  private static boolean macro_args_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_args_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && macro_arg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // token_sequence
  public static boolean macro_body_inline(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_body_inline")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_BODY_INLINE, "<macro body inline>");
    result_ = token_sequence(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER macro_args
  public static boolean macro_call(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_call")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && macro_args(builder_, level_ + 1);
    exit_section_(builder_, marker_, MACRO_CALL, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ROTATE const_expr
  //             | MACRO_STRLEN IDENTIFIER (STRING | IDENTIFIER)
  //             | MACRO_SUBSTR IDENTIFIER (STRING | IDENTIFIER) const_expr [const_expr]
  //             | MACRO_STRCAT IDENTIFIER (STRING | IDENTIFIER)+
  //             | MACRO_PATHSEARCH IDENTIFIER STRING
  //             | MACRO_DEPEND STRING
  //             | MACRO_ALIASES IDENTIFIER
  //             | PREPROCESSOR_DIRECTIVE
  public static boolean macro_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_DIR, "<macro dir>");
    result_ = macro_dir_0(builder_, level_ + 1);
    if (!result_) result_ = macro_dir_1(builder_, level_ + 1);
    if (!result_) result_ = macro_dir_2(builder_, level_ + 1);
    if (!result_) result_ = macro_dir_3(builder_, level_ + 1);
    if (!result_) result_ = parseTokens(builder_, 0, MACRO_PATHSEARCH, IDENTIFIER, STRING);
    if (!result_) result_ = parseTokens(builder_, 0, MACRO_DEPEND, STRING);
    if (!result_) result_ = parseTokens(builder_, 0, MACRO_ALIASES, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, PREPROCESSOR_DIRECTIVE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_ROTATE const_expr
  private static boolean macro_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_ROTATE);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_STRLEN IDENTIFIER (STRING | IDENTIFIER)
  private static boolean macro_dir_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_STRLEN, IDENTIFIER);
    result_ = result_ && macro_dir_1_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // STRING | IDENTIFIER
  private static boolean macro_dir_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_1_2")) return false;
    boolean result_;
    result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    return result_;
  }

  // MACRO_SUBSTR IDENTIFIER (STRING | IDENTIFIER) const_expr [const_expr]
  private static boolean macro_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_SUBSTR, IDENTIFIER);
    result_ = result_ && macro_dir_2_2(builder_, level_ + 1);
    result_ = result_ && const_expr(builder_, level_ + 1);
    result_ = result_ && macro_dir_2_4(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // STRING | IDENTIFIER
  private static boolean macro_dir_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_2_2")) return false;
    boolean result_;
    result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    return result_;
  }

  // [const_expr]
  private static boolean macro_dir_2_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_2_4")) return false;
    const_expr(builder_, level_ + 1);
    return true;
  }

  // MACRO_STRCAT IDENTIFIER (STRING | IDENTIFIER)+
  private static boolean macro_dir_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_STRCAT, IDENTIFIER);
    result_ = result_ && macro_dir_3_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (STRING | IDENTIFIER)+
  private static boolean macro_dir_3_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_3_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_dir_3_2_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!macro_dir_3_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_dir_3_2", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // STRING | IDENTIFIER
  private static boolean macro_dir_3_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_dir_3_2_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_END
  static boolean macro_end_line(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, MACRO_END);
  }

  /* ********************************************************** */
  // MACRO_EXPANSION_START macro_expansion_content RBRACKET
  //                   | MACRO_EXPLICIT_START macro_expansion_brace_content RBRACE
  public static boolean macro_expansion(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion")) return false;
    if (!nextTokenIs(builder_, "<macro expansion>", MACRO_EXPANSION_START, MACRO_EXPLICIT_START)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_EXPANSION, "<macro expansion>");
    result_ = macro_expansion_0(builder_, level_ + 1);
    if (!result_) result_ = macro_expansion_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_EXPANSION_START macro_expansion_content RBRACKET
  private static boolean macro_expansion_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_EXPANSION_START);
    result_ = result_ && macro_expansion_content(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_EXPLICIT_START macro_expansion_brace_content RBRACE
  private static boolean macro_expansion_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_EXPLICIT_START);
    result_ = result_ && macro_expansion_brace_content(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_expansion_brace_token+
  static boolean macro_expansion_brace_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_brace_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_expansion_brace_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!macro_expansion_brace_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_expansion_brace_content", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !RBRACE base_token_element
  static boolean macro_expansion_brace_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_brace_token")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_expansion_brace_token_0(builder_, level_ + 1);
    result_ = result_ && base_token_element(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !RBRACE
  private static boolean macro_expansion_brace_token_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_brace_token_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // macro_expansion_token+
  static boolean macro_expansion_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_expansion_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!macro_expansion_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "macro_expansion_content", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !RBRACKET base_token_element
  static boolean macro_expansion_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_token")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_expansion_token_0(builder_, level_ + 1);
    result_ = result_ && base_token_element(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !RBRACKET
  private static boolean macro_expansion_token_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_expansion_token_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, RBRACKET);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // PLUS | MUL | MINUS MUL
  public static boolean macro_flags(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_flags")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_FLAGS, "<macro flags>");
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = parseTokens(builder_, 0, MINUS, MUL);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // source_lines
  public static boolean macro_lines(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_lines")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_LINES, "<macro lines>");
    result_ = source_lines(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER | preprocessor_id | context_ref | keyword_as_name | SIZE_SPEC
  public static boolean macro_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_name")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_NAME, "<macro name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = preprocessor_id(builder_, level_ + 1);
    if (!result_) result_ = context_ref(builder_, level_ + 1);
    if (!result_) result_ = keyword_as_name(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SIZE_SPEC);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // macro_param_ref IDENTIFIER     // %1_end, %2_label
  //                              | context_local_ref IDENTIFIER   // %$foo_end
  //                              | macro_expansion IDENTIFIER
  static boolean macro_param_concat(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_concat")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_param_concat_0(builder_, level_ + 1);
    if (!result_) result_ = macro_param_concat_1(builder_, level_ + 1);
    if (!result_) result_ = macro_param_concat_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // macro_param_ref IDENTIFIER
  private static boolean macro_param_concat_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_concat_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_param_ref(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // context_local_ref IDENTIFIER
  private static boolean macro_param_concat_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_concat_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = context_local_ref(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // macro_expansion IDENTIFIER
  private static boolean macro_param_concat_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_concat_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_expansion(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_param_ref macro_args?
  static boolean macro_param_invocation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_invocation")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = macro_param_ref(builder_, level_ + 1);
    result_ = result_ && macro_param_invocation_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // macro_args?
  private static boolean macro_param_invocation_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_invocation_1")) return false;
    macro_args(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // MACRO_PARAM
  //                   | MACRO_PARAM_GREEDY
  //                   | MACRO_PARAM_REVERSE
  //                   | MACRO_EXPLICIT_START NUMBER RBRACE
  //                   | MACRO_EXPLICIT_START NUMBER COLON default_value RBRACE
  public static boolean macro_param_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_ref")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MACRO_PARAM_REF, "<macro param ref>");
    result_ = consumeToken(builder_, MACRO_PARAM);
    if (!result_) result_ = consumeToken(builder_, MACRO_PARAM_GREEDY);
    if (!result_) result_ = consumeToken(builder_, MACRO_PARAM_REVERSE);
    if (!result_) result_ = parseTokens(builder_, 0, MACRO_EXPLICIT_START, NUMBER, RBRACE);
    if (!result_) result_ = macro_param_ref_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_EXPLICIT_START NUMBER COLON default_value RBRACE
  private static boolean macro_param_ref_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_param_ref_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MACRO_EXPLICIT_START, NUMBER, COLON);
    result_ = result_ && default_value(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN param_list RPAREN
  public static boolean macro_params(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_params")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && param_list(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, MACRO_PARAMS, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean macro_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_ref")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, MACRO_REF, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_START macro_name param_count [param_defaults] [macro_flags]
  static boolean macro_start_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_start_line")) return false;
    if (!nextTokenIs(builder_, MACRO_START)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_START);
    result_ = result_ && macro_name(builder_, level_ + 1);
    result_ = result_ && param_count(builder_, level_ + 1);
    result_ = result_ && macro_start_line_3(builder_, level_ + 1);
    result_ = result_ && macro_start_line_4(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [param_defaults]
  private static boolean macro_start_line_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_start_line_3")) return false;
    param_defaults(builder_, level_ + 1);
    return true;
  }

  // [macro_flags]
  private static boolean macro_start_line_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "macro_start_line_4")) return false;
    macro_flags(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // size_spec memory_ref
  //               | segment_override memory_ref
  //               | memory_ref
  public static boolean memory_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memory_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MEMORY_EXPR, "<memory expr>");
    result_ = memory_expr_0(builder_, level_ + 1);
    if (!result_) result_ = memory_expr_1(builder_, level_ + 1);
    if (!result_) result_ = memory_ref(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // size_spec memory_ref
  private static boolean memory_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memory_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = size_spec(builder_, level_ + 1);
    result_ = result_ && memory_ref(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // segment_override memory_ref
  private static boolean memory_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memory_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = segment_override(builder_, level_ + 1);
    result_ = result_ && memory_ref(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACKET effective_addr RBRACKET
  public static boolean memory_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memory_ref")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACKET);
    result_ = result_ && effective_addr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, MEMORY_REF, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ERROR message_text
  public static boolean message_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "message_dir")) return false;
    if (!nextTokenIs(builder_, MACRO_ERROR)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MESSAGE_DIR, null);
    result_ = consumeToken(builder_, MACRO_ERROR);
    pinned_ = result_; // pin = 1
    result_ = result_ && message_text(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // STRING | token_sequence
  public static boolean message_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "message_text")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MESSAGE_TEXT, "<message text>");
    result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = token_sequence(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // macro_param_concat     // Implicit concat: %1_op, %$op_name
  //            | IDENTIFIER
  //            | macro_expansion
  public static boolean mnemonic(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mnemonic")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MNEMONIC, "<mnemonic>");
    result_ = macro_param_concat(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = macro_expansion(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // unary_expr (mul_op unary_expr)*
  public static boolean mul_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<mul expr>");
    result_ = unary_expr(builder_, level_ + 1);
    result_ = result_ && mul_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (mul_op unary_expr)*
  private static boolean mul_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!mul_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "mul_expr_1", pos_)) break;
    }
    return true;
  }

  // mul_op unary_expr
  private static boolean mul_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_op(builder_, level_ + 1);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MUL | DIV | SIGNED_DIV | MOD | SIGNED_MOD
  static boolean mul_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_op")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, SIGNED_DIV);
    if (!result_) result_ = consumeToken(builder_, MOD);
    if (!result_) result_ = consumeToken(builder_, SIGNED_MOD);
    return result_;
  }

  /* ********************************************************** */
  // macro_start_line macro_lines macro_end_line
  public static boolean multi_line_macro(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multi_line_macro")) return false;
    if (!nextTokenIs(builder_, MACRO_START)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MULTI_LINE_MACRO, null);
    result_ = macro_start_line(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, macro_lines(builder_, level_ + 1));
    result_ = pinned_ && macro_end_line(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // instruction | pseudo_instruction | bracketed_directive | invocation | at_directive
  static boolean non_assignment_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "non_assignment_statement")) return false;
    boolean result_;
    result_ = instruction(builder_, level_ + 1);
    if (!result_) result_ = pseudo_instruction(builder_, level_ + 1);
    if (!result_) result_ = bracketed_directive(builder_, level_ + 1);
    if (!result_) result_ = invocation(builder_, level_ + 1);
    if (!result_) result_ = at_directive(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // expanded_operand decorator* | decorator+
  public static boolean operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERAND, "<operand>");
    result_ = operand_0(builder_, level_ + 1);
    if (!result_) result_ = operand_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expanded_operand decorator*
  private static boolean operand_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expanded_operand(builder_, level_ + 1);
    result_ = result_ && operand_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // decorator*
  private static boolean operand_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!decorator(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "operand_0_1", pos_)) break;
    }
    return true;
  }

  // decorator+
  private static boolean operand_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = decorator(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!decorator(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "operand_1", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // operand operand_tail*
  public static boolean operand_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_list")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERAND_LIST, "<operand list>");
    result_ = operand(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && operand_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // operand_tail*
  private static boolean operand_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!operand_tail(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "operand_list_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(CRLF | MACRO_START | MACRO_END | MACRO_IF | MACRO_IFDEF | MACRO_IFNDEF | MACRO_IFMACRO | MACRO_ENDIF)
  static boolean operand_list_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_list_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !operand_list_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // CRLF | MACRO_START | MACRO_END | MACRO_IF | MACRO_IFDEF | MACRO_IFNDEF | MACRO_IFMACRO | MACRO_ENDIF
  private static boolean operand_list_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_list_recover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, CRLF);
    if (!result_) result_ = consumeToken(builder_, MACRO_START);
    if (!result_) result_ = consumeToken(builder_, MACRO_END);
    if (!result_) result_ = consumeToken(builder_, MACRO_IF);
    if (!result_) result_ = consumeToken(builder_, MACRO_IFDEF);
    if (!result_) result_ = consumeToken(builder_, MACRO_IFNDEF);
    if (!result_) result_ = consumeToken(builder_, MACRO_IFMACRO);
    if (!result_) result_ = consumeToken(builder_, MACRO_ENDIF);
    return result_;
  }

  /* ********************************************************** */
  // COMMA operand?
  static boolean operand_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_tail")) return false;
    if (!nextTokenIs(builder_, COMMA)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && operand_tail_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // operand?
  private static boolean operand_tail_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operand_tail_1")) return false;
    operand(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // PLUS | MINUS | MUL | DIV | MOD | SIGNED_DIV | SIGNED_MOD
  //            | AMP | PIPE | CARET | TILDE | EXCLAIM
  //            | LSHIFT | LSHIFT_COMPLETE | RSHIFT | RSHIFT_COMPLETE
  //            | BOOLEAN_AND | BOOLEAN_OR | BOOLEAN_XOR
  //            | EQ_EQ | NOT_EQUAL_1 | NOT_EQUAL_2 | LT | LTE | GT | GTE | SPACESHIP
  //            | QUESTION_MARK
  //            | PASTE_OP
  //            | SEG | WRT
  public static boolean operator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERATOR, "<operator>");
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, MOD);
    if (!result_) result_ = consumeToken(builder_, SIGNED_DIV);
    if (!result_) result_ = consumeToken(builder_, SIGNED_MOD);
    if (!result_) result_ = consumeToken(builder_, AMP);
    if (!result_) result_ = consumeToken(builder_, PIPE);
    if (!result_) result_ = consumeToken(builder_, CARET);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    if (!result_) result_ = consumeToken(builder_, EXCLAIM);
    if (!result_) result_ = consumeToken(builder_, LSHIFT);
    if (!result_) result_ = consumeToken(builder_, LSHIFT_COMPLETE);
    if (!result_) result_ = consumeToken(builder_, RSHIFT);
    if (!result_) result_ = consumeToken(builder_, RSHIFT_COMPLETE);
    if (!result_) result_ = consumeToken(builder_, BOOLEAN_AND);
    if (!result_) result_ = consumeToken(builder_, BOOLEAN_OR);
    if (!result_) result_ = consumeToken(builder_, BOOLEAN_XOR);
    if (!result_) result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = consumeToken(builder_, NOT_EQUAL_1);
    if (!result_) result_ = consumeToken(builder_, NOT_EQUAL_2);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, LTE);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, GTE);
    if (!result_) result_ = consumeToken(builder_, SPACESHIP);
    if (!result_) result_ = consumeToken(builder_, QUESTION_MARK);
    if (!result_) result_ = consumeToken(builder_, PASTE_OP);
    if (!result_) result_ = consumeToken(builder_, SEG);
    if (!result_) result_ = consumeToken(builder_, WRT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // xor_expr (PIPE xor_expr)*
  public static boolean or_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<or expr>");
    result_ = xor_expr(builder_, level_ + 1);
    result_ = result_ && or_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (PIPE xor_expr)*
  private static boolean or_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!or_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "or_expr_1", pos_)) break;
    }
    return true;
  }

  // PIPE xor_expr
  private static boolean or_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PIPE);
    result_ = result_ && xor_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ORG_KW const_expr
  public static boolean org_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "org_dir")) return false;
    if (!nextTokenIs(builder_, ORG_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ORG_DIR, null);
    result_ = consumeToken(builder_, ORG_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean package_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "package_name")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, PACKAGE_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // NUMBER [MINUS NUMBER]
  public static boolean param_count(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_count")) return false;
    if (!nextTokenIs(builder_, NUMBER)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAM_COUNT, null);
    result_ = consumeToken(builder_, NUMBER);
    pinned_ = result_; // pin = 1
    result_ = result_ && param_count_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [MINUS NUMBER]
  private static boolean param_count_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_count_1")) return false;
    parseTokens(builder_, 0, MINUS, NUMBER);
    return true;
  }

  /* ********************************************************** */
  // NUMBER (COMMA NUMBER)*
  static boolean param_defaults(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_defaults")) return false;
    if (!nextTokenIs(builder_, NUMBER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NUMBER);
    result_ = result_ && param_defaults_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA NUMBER)*
  private static boolean param_defaults_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_defaults_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!param_defaults_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "param_defaults_1", pos_)) break;
    }
    return true;
  }

  // COMMA NUMBER
  private static boolean param_defaults_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_defaults_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, NUMBER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // param_spec (COMMA param_spec)*
  public static boolean param_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_list")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = param_spec(builder_, level_ + 1);
    result_ = result_ && param_list_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, PARAM_LIST, result_);
    return result_;
  }

  // (COMMA param_spec)*
  private static boolean param_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!param_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "param_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA param_spec
  private static boolean param_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && param_spec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean param_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_name")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, PARAM_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // EQ default_value
  //                   | PLUS [default_value]
  //                   | MUL [default_value]
  //                   | EXCLAIM [default_value]
  public static boolean param_qualifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAM_QUALIFIER, "<param qualifier>");
    result_ = param_qualifier_0(builder_, level_ + 1);
    if (!result_) result_ = param_qualifier_1(builder_, level_ + 1);
    if (!result_) result_ = param_qualifier_2(builder_, level_ + 1);
    if (!result_) result_ = param_qualifier_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // EQ default_value
  private static boolean param_qualifier_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && default_value(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS [default_value]
  private static boolean param_qualifier_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PLUS);
    result_ = result_ && param_qualifier_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [default_value]
  private static boolean param_qualifier_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_1_1")) return false;
    default_value(builder_, level_ + 1);
    return true;
  }

  // MUL [default_value]
  private static boolean param_qualifier_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MUL);
    result_ = result_ && param_qualifier_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [default_value]
  private static boolean param_qualifier_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_2_1")) return false;
    default_value(builder_, level_ + 1);
    return true;
  }

  // EXCLAIM [default_value]
  private static boolean param_qualifier_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXCLAIM);
    result_ = result_ && param_qualifier_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [default_value]
  private static boolean param_qualifier_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_qualifier_3_1")) return false;
    default_value(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // param_name [param_qualifier]
  public static boolean param_spec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_spec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = param_name(builder_, level_ + 1);
    result_ = result_ && param_spec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, PARAM_SPEC, result_);
    return result_;
  }

  // [param_qualifier]
  private static boolean param_spec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_spec_1")) return false;
    param_qualifier(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // atom_expr (PASTE_OP atom_expr)*
  public static boolean postfix_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION, "<postfix expr>");
    result_ = atom_expr(builder_, level_ + 1);
    result_ = result_ && postfix_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (PASTE_OP atom_expr)*
  private static boolean postfix_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!postfix_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "postfix_expr_1", pos_)) break;
    }
    return true;
  }

  // PASTE_OP atom_expr
  private static boolean postfix_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PASTE_OP);
    result_ = result_ && atom_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_ASSIGN IDENTIFIER const_expr
  public static boolean pp_assign_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assign_stmt")) return false;
    if (!nextTokenIs(builder_, MACRO_ASSIGN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_ASSIGN_STMT, null);
    result_ = consumeTokens(builder_, 1, MACRO_ASSIGN, IDENTIFIER);
    pinned_ = result_; // pin = 1
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // pp_assign_stmt [CRLF]
  //                              | pp_define_stmt [CRLF]
  static boolean pp_assignment_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assignment_line")) return false;
    if (!nextTokenIs(builder_, "", MACRO_ASSIGN, MACRO_DEFINE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = pp_assignment_line_0(builder_, level_ + 1);
    if (!result_) result_ = pp_assignment_line_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // pp_assign_stmt [CRLF]
  private static boolean pp_assignment_line_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assignment_line_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = pp_assign_stmt(builder_, level_ + 1);
    result_ = result_ && pp_assignment_line_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [CRLF]
  private static boolean pp_assignment_line_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assignment_line_0_1")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  // pp_define_stmt [CRLF]
  private static boolean pp_assignment_line_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assignment_line_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = pp_define_stmt(builder_, level_ + 1);
    result_ = result_ && pp_assignment_line_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [CRLF]
  private static boolean pp_assignment_line_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_assignment_line_1_1")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // MACRO_DEFINE macro_name [macro_params] [macro_body_inline]
  public static boolean pp_define_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_define_stmt")) return false;
    if (!nextTokenIs(builder_, MACRO_DEFINE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_DEFINE_STMT, null);
    result_ = consumeToken(builder_, MACRO_DEFINE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, macro_name(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, pp_define_stmt_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && pp_define_stmt_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [macro_params]
  private static boolean pp_define_stmt_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_define_stmt_2")) return false;
    macro_params(builder_, level_ + 1);
    return true;
  }

  // [macro_body_inline]
  private static boolean pp_define_stmt_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_define_stmt_3")) return false;
    macro_body_inline(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // (MACRO_STRLEN | MACRO_SUBSTR | MACRO_STRCAT | PREPROCESSOR_DIRECTIVE)
  //                           LPAREN preprocessor_function_args RPAREN
  public static boolean preprocessor_function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_function")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PREPROCESSOR_FUNCTION, "<preprocessor function>");
    result_ = preprocessor_function_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, preprocessor_function_args(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // MACRO_STRLEN | MACRO_SUBSTR | MACRO_STRCAT | PREPROCESSOR_DIRECTIVE
  private static boolean preprocessor_function_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_function_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MACRO_STRLEN);
    if (!result_) result_ = consumeToken(builder_, MACRO_SUBSTR);
    if (!result_) result_ = consumeToken(builder_, MACRO_STRCAT);
    if (!result_) result_ = consumeToken(builder_, PREPROCESSOR_DIRECTIVE);
    return result_;
  }

  /* ********************************************************** */
  // const_expr
  static boolean preprocessor_function_arg(PsiBuilder builder_, int level_) {
    return const_expr(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // preprocessor_function_arg (COMMA preprocessor_function_arg)*
  static boolean preprocessor_function_args(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_function_args")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = preprocessor_function_arg(builder_, level_ + 1);
    result_ = result_ && preprocessor_function_args_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA preprocessor_function_arg)*
  private static boolean preprocessor_function_args_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_function_args_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!preprocessor_function_args_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "preprocessor_function_args_1", pos_)) break;
    }
    return true;
  }

  // COMMA preprocessor_function_arg
  private static boolean preprocessor_function_args_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_function_args_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && preprocessor_function_arg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PREPROCESSOR_DIRECTIVE
  public static boolean preprocessor_id(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_id")) return false;
    if (!nextTokenIs(builder_, PREPROCESSOR_DIRECTIVE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PREPROCESSOR_DIRECTIVE);
    exit_section_(builder_, marker_, PREPROCESSOR_ID, result_);
    return result_;
  }

  /* ********************************************************** */
  // macro_dir
  //                     | context_dir
  //                     | message_dir
  //                     | line_dir
  //                     | clear_dir
  //                     | exit_dir
  public static boolean preprocessor_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_line")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PREPROCESSOR_LINE, "<preprocessor line>");
    result_ = macro_dir(builder_, level_ + 1);
    if (!result_) result_ = context_dir(builder_, level_ + 1);
    if (!result_) result_ = message_dir(builder_, level_ + 1);
    if (!result_) result_ = line_dir(builder_, level_ + 1);
    if (!result_) result_ = clear_dir(builder_, level_ + 1);
    if (!result_) result_ = exit_dir(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // preprocessor_function  // %strlen(...), %strcat(...), etc.
  //                      | base_token_element
  public static boolean preprocessor_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "preprocessor_token")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PREPROCESSOR_TOKEN, "<preprocessor token>");
    result_ = preprocessor_function(builder_, level_ + 1);
    if (!result_) result_ = base_token_element(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // source_lines
  static boolean program(PsiBuilder builder_, int level_) {
    return source_lines(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // data_def
  //                      | space_def
  //                      | include_dir
  //                      | section_dir
  //                      | global_dir
  //                      | extern_dir
  //                      | common_dir
  //                      | align_dir
  //                      | org_dir
  //                      | bits_dir
  //                      | cpu_dir
  //                      | float_dir
  //                      | default_dir
  //                      | absolute_dir
  //                      | use_package
  public static boolean pseudo_instruction(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pseudo_instruction")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PSEUDO_INSTRUCTION, "<pseudo instruction>");
    result_ = data_def(builder_, level_ + 1);
    if (!result_) result_ = space_def(builder_, level_ + 1);
    if (!result_) result_ = include_dir(builder_, level_ + 1);
    if (!result_) result_ = section_dir(builder_, level_ + 1);
    if (!result_) result_ = global_dir(builder_, level_ + 1);
    if (!result_) result_ = extern_dir(builder_, level_ + 1);
    if (!result_) result_ = common_dir(builder_, level_ + 1);
    if (!result_) result_ = align_dir(builder_, level_ + 1);
    if (!result_) result_ = org_dir(builder_, level_ + 1);
    if (!result_) result_ = bits_dir(builder_, level_ + 1);
    if (!result_) result_ = cpu_dir(builder_, level_ + 1);
    if (!result_) result_ = float_dir(builder_, level_ + 1);
    if (!result_) result_ = default_dir(builder_, level_ + 1);
    if (!result_) result_ = absolute_dir(builder_, level_ + 1);
    if (!result_) result_ = use_package(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // QUERY IDENTIFIER
  //                  | QUERY_EXPAND IDENTIFIER
  //                  | defined_func
  public static boolean query_function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "query_function")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUERY_FUNCTION, "<query function>");
    result_ = parseTokens(builder_, 0, QUERY, IDENTIFIER);
    if (!result_) result_ = parseTokens(builder_, 0, QUERY_EXPAND, IDENTIFIER);
    if (!result_) result_ = defined_func(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // REGISTER | SEG_REGISTER | MASK_REG
  static boolean register(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "register")) return false;
    boolean result_;
    result_ = consumeToken(builder_, REGISTER);
    if (!result_) result_ = consumeToken(builder_, SEG_REGISTER);
    if (!result_) result_ = consumeToken(builder_, MASK_REG);
    return result_;
  }

  /* ********************************************************** */
  // MACRO_REP const_expr CRLF source_lines CRLF* MACRO_ENDREP
  public static boolean rep_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rep_block")) return false;
    if (!nextTokenIs(builder_, MACRO_REP)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REP_BLOCK, null);
    result_ = consumeToken(builder_, MACRO_REP);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, const_expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, CRLF)) && result_;
    result_ = pinned_ && report_error_(builder_, source_lines(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, rep_block_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, MACRO_ENDREP) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CRLF*
  private static boolean rep_block_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rep_block_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, CRLF)) break;
      if (!empty_element_parsed_guard_(builder_, "rep_block_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean scale(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "scale")) return false;
    if (!nextTokenIs(builder_, NUMBER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NUMBER);
    exit_section_(builder_, marker_, SCALE, result_);
    return result_;
  }

  /* ********************************************************** */
  // section_attr_name EQ const_expr  // align=16, start=0x1000
  //                | section_attr_name
  public static boolean section_attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_attr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_ATTR, "<section attr>");
    result_ = section_attr_0(builder_, level_ + 1);
    if (!result_) result_ = section_attr_name(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // section_attr_name EQ const_expr
  private static boolean section_attr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_attr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = section_attr_name(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER | SECTION_ATTR_KW | ALIGN_KW | BITS_KW | ORG_KW
  public static boolean section_attr_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_attr_name")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_ATTR_NAME, "<section attr name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, SECTION_ATTR_KW);
    if (!result_) result_ = consumeToken(builder_, ALIGN_KW);
    if (!result_) result_ = consumeToken(builder_, BITS_KW);
    if (!result_) result_ = consumeToken(builder_, ORG_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // section_attr+
  public static boolean section_attrs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_attrs")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_ATTRS, "<section attrs>");
    result_ = section_attr(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!section_attr(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "section_attrs", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (SECTION_KW | SEGMENT_KW) section_name [section_attrs]
  public static boolean section_dir(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_dir")) return false;
    if (!nextTokenIs(builder_, "<section dir>", SECTION_KW, SEGMENT_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_DIR, "<section dir>");
    result_ = section_dir_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, section_name(builder_, level_ + 1));
    result_ = pinned_ && section_dir_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // SECTION_KW | SEGMENT_KW
  private static boolean section_dir_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_dir_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SECTION_KW);
    if (!result_) result_ = consumeToken(builder_, SEGMENT_KW);
    return result_;
  }

  // [section_attrs]
  private static boolean section_dir_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_dir_2")) return false;
    section_attrs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER | STRING
  public static boolean section_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_name")) return false;
    if (!nextTokenIs(builder_, "<section name>", IDENTIFIER, STRING)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_NAME, "<section name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, STRING);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // segment_reg COLON
  public static boolean segment_override(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "segment_override")) return false;
    if (!nextTokenIs(builder_, SEG_REGISTER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = segment_reg(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, SEGMENT_OVERRIDE, result_);
    return result_;
  }

  /* ********************************************************** */
  // SEG_REGISTER
  public static boolean segment_reg(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "segment_reg")) return false;
    if (!nextTokenIs(builder_, SEG_REGISTER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEG_REGISTER);
    exit_section_(builder_, marker_, SEGMENT_REG, result_);
    return result_;
  }

  /* ********************************************************** */
  // COMMA | COLON | LPAREN | RPAREN | LBRACKET | RBRACKET | LBRACE | RBRACE
  public static boolean separator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "separator")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SEPARATOR, "<separator>");
    result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, LBRACKET);
    if (!result_) result_ = consumeToken(builder_, RBRACKET);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // add_expr (shift_op add_expr)*
  public static boolean shift_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shift_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<shift expr>");
    result_ = add_expr(builder_, level_ + 1);
    result_ = result_ && shift_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (shift_op add_expr)*
  private static boolean shift_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shift_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!shift_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "shift_expr_1", pos_)) break;
    }
    return true;
  }

  // shift_op add_expr
  private static boolean shift_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shift_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = shift_op(builder_, level_ + 1);
    result_ = result_ && add_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LSHIFT | LSHIFT_COMPLETE | RSHIFT | RSHIFT_COMPLETE
  static boolean shift_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shift_op")) return false;
    boolean result_;
    result_ = consumeToken(builder_, LSHIFT);
    if (!result_) result_ = consumeToken(builder_, LSHIFT_COMPLETE);
    if (!result_) result_ = consumeToken(builder_, RSHIFT);
    if (!result_) result_ = consumeToken(builder_, RSHIFT_COMPLETE);
    return result_;
  }

  /* ********************************************************** */
  // INSTRUCTION_PREFIX
  public static boolean size_override(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "size_override")) return false;
    if (!nextTokenIs(builder_, INSTRUCTION_PREFIX)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INSTRUCTION_PREFIX);
    exit_section_(builder_, marker_, SIZE_OVERRIDE, result_);
    return result_;
  }

  /* ********************************************************** */
  // SIZE_SPEC SIZE_SPEC*
  static boolean size_spec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "size_spec")) return false;
    if (!nextTokenIs(builder_, SIZE_SPEC)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SIZE_SPEC);
    result_ = result_ && size_spec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SIZE_SPEC*
  private static boolean size_spec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "size_spec_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, SIZE_SPEC)) break;
      if (!empty_element_parsed_guard_(builder_, "size_spec_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  //                    | MACRO_EXPANSION_START token_sequence RBRACKET
  //                    | preprocessor_id
  public static boolean smacro_expansion(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "smacro_expansion")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SMACRO_EXPANSION, "<smacro expansion>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = smacro_expansion_1(builder_, level_ + 1);
    if (!result_) result_ = preprocessor_id(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MACRO_EXPANSION_START token_sequence RBRACKET
  private static boolean smacro_expansion_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "smacro_expansion_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_EXPANSION_START);
    result_ = result_ && token_sequence(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // equ_line                       // IDENTIFIER EQU ... (pinned on EQU)
  //               | label_with_colon_line          // IDENTIFIER: ... (requires colon)
  //               | pp_assignment_line              // %assign, %define (pinned on directive)
  //               | times_prefixed_line             // TIMES-prefixed statements (with or without CRLF)
  //               | label_with_data_line           // IDENTIFIER db/dw/dd/etc ... (label without colon before data) - must come before unlabeled_content
  //               | unlabeled_content              // Instructions and other statements
  //               | preprocessor_line
  //               | conditional_block
  //               | multi_line_macro
  //               | rep_block
  //               | struc_block
  //               | istruc_block
  //               | CRLF
  public static boolean source_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "source_line")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SOURCE_LINE, "<source line>");
    result_ = equ_line(builder_, level_ + 1);
    if (!result_) result_ = label_with_colon_line(builder_, level_ + 1);
    if (!result_) result_ = pp_assignment_line(builder_, level_ + 1);
    if (!result_) result_ = times_prefixed_line(builder_, level_ + 1);
    if (!result_) result_ = label_with_data_line(builder_, level_ + 1);
    if (!result_) result_ = unlabeled_content(builder_, level_ + 1);
    if (!result_) result_ = preprocessor_line(builder_, level_ + 1);
    if (!result_) result_ = conditional_block(builder_, level_ + 1);
    if (!result_) result_ = multi_line_macro(builder_, level_ + 1);
    if (!result_) result_ = rep_block(builder_, level_ + 1);
    if (!result_) result_ = struc_block(builder_, level_ + 1);
    if (!result_) result_ = istruc_block(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, CRLF);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // source_line*
  public static boolean source_lines(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "source_lines")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SOURCE_LINES, "<source lines>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!source_line(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "source_lines", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // SPACE_SIZE const_expr
  public static boolean space_def(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "space_def")) return false;
    if (!nextTokenIs(builder_, SPACE_SIZE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SPACE_DEF, null);
    result_ = consumeToken(builder_, SPACE_SIZE);
    pinned_ = result_; // pin = 1
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // DOLLAR | DOUBLE_DOLLAR | CONTEXT_LOCAL_REF
  public static boolean special_symbol(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "special_symbol")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SPECIAL_SYMBOL, "<special symbol>");
    result_ = consumeToken(builder_, DOLLAR);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_DOLLAR);
    if (!result_) result_ = consumeToken(builder_, CONTEXT_LOCAL_REF);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // STRING_FUNC LPAREN STRING RPAREN
  public static boolean string_function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_function")) return false;
    if (!nextTokenIs(builder_, STRING_FUNC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_FUNCTION, null);
    result_ = consumeTokens(builder_, 2, STRING_FUNC, LPAREN, STRING, RPAREN);
    pinned_ = result_; // pin = 2
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // STRING
  //                 | LT IDENTIFIER GT
  //                 | env_var_ref
  public static boolean string_or_env(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_or_env")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_OR_ENV, "<string or env>");
    result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = parseTokens(builder_, 0, LT, IDENTIFIER, GT);
    if (!result_) result_ = env_var_ref(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // STRING_FUNC LPAREN STRING RPAREN
  public static boolean string_transform(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_transform")) return false;
    if (!nextTokenIs(builder_, STRING_FUNC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_TRANSFORM, null);
    result_ = consumeTokens(builder_, 2, STRING_FUNC, LPAREN, STRING, RPAREN);
    pinned_ = result_; // pin = 2
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ENV_VAR_PREFIX IDENTIFIER
  public static boolean stringize_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringize_op")) return false;
    if (!nextTokenIs(builder_, ENV_VAR_PREFIX)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, ENV_VAR_PREFIX, IDENTIFIER);
    exit_section_(builder_, marker_, STRINGIZE_OP, result_);
    return result_;
  }

  /* ********************************************************** */
  // STRUC_KW IDENTIFIER CRLF source_lines CRLF* ENDSTRUC_KW
  public static boolean struc_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struc_block")) return false;
    if (!nextTokenIs(builder_, STRUC_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRUC_BLOCK, null);
    result_ = consumeTokens(builder_, 1, STRUC_KW, IDENTIFIER, CRLF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, source_lines(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, struc_block_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDSTRUC_KW) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CRLF*
  private static boolean struc_block_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "struc_block_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, CRLF)) break;
      if (!empty_element_parsed_guard_(builder_, "struc_block_4", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // symbol_name [COLON symbol_name]
  public static boolean symbol_decl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_decl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SYMBOL_DECL, "<symbol decl>");
    result_ = symbol_name(builder_, level_ + 1);
    result_ = result_ && symbol_decl_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [COLON symbol_name]
  private static boolean symbol_decl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_decl_1")) return false;
    symbol_decl_1_0(builder_, level_ + 1);
    return true;
  }

  // COLON symbol_name
  private static boolean symbol_decl_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_decl_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && symbol_name(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // symbol_decl (COMMA symbol_decl)*
  public static boolean symbol_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SYMBOL_LIST, "<symbol list>");
    result_ = symbol_decl(builder_, level_ + 1);
    result_ = result_ && symbol_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA symbol_decl)*
  private static boolean symbol_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!symbol_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "symbol_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA symbol_decl
  private static boolean symbol_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && symbol_decl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER | SECTION_ATTR_KW | SIZE_SPEC | keyword_as_name
  public static boolean symbol_name(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_name")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SYMBOL_NAME, "<symbol name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, SECTION_ATTR_KW);
    if (!result_) result_ = consumeToken(builder_, SIZE_SPEC);
    if (!result_) result_ = keyword_as_name(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // macro_param_concat      // Implicit concat: %1_end, %$foo_end
  //              | IDENTIFIER
  //              | local_label
  //              | special_symbol
  //              | macro_param_ref
  //              | context_local_ref
  public static boolean symbol_ref(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_ref")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SYMBOL_REF, "<symbol ref>");
    result_ = macro_param_concat(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = local_label(builder_, level_ + 1);
    if (!result_) result_ = special_symbol(builder_, level_ + 1);
    if (!result_) result_ = macro_param_ref(builder_, level_ + 1);
    if (!result_) result_ = context_local_ref(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // times_instruction          // TIMES-prefixed instruction
  //                         | DATA_SIZE data_list        // TIMES-prefixed data
  //                         | invocation
  static boolean times_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = times_instruction(builder_, level_ + 1);
    if (!result_) result_ = times_content_1(builder_, level_ + 1);
    if (!result_) result_ = invocation(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // DATA_SIZE data_list
  private static boolean times_content_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_content_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DATA_SIZE);
    result_ = result_ && data_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TIMES const_expr
  //              | MACRO_REP const_expr
  public static boolean times_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_expr")) return false;
    if (!nextTokenIs(builder_, "<times expr>", MACRO_REP, TIMES)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TIMES_EXPR, "<times expr>");
    result_ = times_expr_0(builder_, level_ + 1);
    if (!result_) result_ = times_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // TIMES const_expr
  private static boolean times_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TIMES);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MACRO_REP const_expr
  private static boolean times_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MACRO_REP);
    result_ = result_ && const_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // mnemonic operand_list?
  public static boolean times_instruction(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_instruction")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INSTRUCTION, "<times instruction>");
    result_ = mnemonic(builder_, level_ + 1);
    result_ = result_ && times_instruction_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // operand_list?
  private static boolean times_instruction_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_instruction_1")) return false;
    operand_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // TIMES times_repeat_count times_content [CRLF]
  static boolean times_prefixed_line(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_prefixed_line")) return false;
    if (!nextTokenIs(builder_, TIMES)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, TIMES);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, times_repeat_count(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, times_content(builder_, level_ + 1)) && result_;
    result_ = pinned_ && times_prefixed_line_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [CRLF]
  private static boolean times_prefixed_line_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_prefixed_line_3")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // macro_param_ref     // Direct macro parameter like %1, %0
  //                               | const_expr
  static boolean times_repeat_count(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "times_repeat_count")) return false;
    boolean result_;
    result_ = macro_param_ref(builder_, level_ + 1);
    if (!result_) result_ = const_expr(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // token_operand COMMA token_operand
  public static boolean token_comparison(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_comparison")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOKEN_COMPARISON, "<token comparison>");
    result_ = token_operand(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && token_operand(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // token_operand_element+
  static boolean token_operand(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_operand")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = token_operand_element(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!token_operand_element(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "token_operand", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(comparison_op | COMMA) base_token_element
  static boolean token_operand_element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_operand_element")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = token_operand_element_0(builder_, level_ + 1);
    result_ = result_ && base_token_element(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(comparison_op | COMMA)
  private static boolean token_operand_element_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_operand_element_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !token_operand_element_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // comparison_op | COMMA
  private static boolean token_operand_element_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_operand_element_0_0")) return false;
    boolean result_;
    result_ = comparison_op(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    return result_;
  }

  /* ********************************************************** */
  // preprocessor_token+
  public static boolean token_sequence(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "token_sequence")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOKEN_SEQUENCE, "<token sequence>");
    result_ = preprocessor_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!preprocessor_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "token_sequence", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // unary_op unary_expr
  //              | postfix_expr
  public static boolean unary_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<unary expr>");
    result_ = unary_expr_0(builder_, level_ + 1);
    if (!result_) result_ = postfix_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // unary_op unary_expr
  private static boolean unary_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unary_op(builder_, level_ + 1);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PLUS | MINUS | TILDE | EXCLAIM | SEG
  static boolean unary_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_op")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    if (!result_) result_ = consumeToken(builder_, EXCLAIM);
    if (!result_) result_ = consumeToken(builder_, SEG);
    return result_;
  }

  /* ********************************************************** */
  // non_assignment_statement [CRLF]
  static boolean unlabeled_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unlabeled_content")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = non_assignment_statement(builder_, level_ + 1);
    result_ = result_ && unlabeled_content_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [CRLF]
  private static boolean unlabeled_content_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unlabeled_content_1")) return false;
    consumeToken(builder_, CRLF);
    return true;
  }

  /* ********************************************************** */
  // MACRO_USE package_name
  public static boolean use_package(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "use_package")) return false;
    if (!nextTokenIs(builder_, MACRO_USE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, USE_PACKAGE, null);
    result_ = consumeToken(builder_, MACRO_USE);
    pinned_ = result_; // pin = 1
    result_ = result_ && package_name(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // INSTRUCTION_PREFIX
  public static boolean vex_prefix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "vex_prefix")) return false;
    if (!nextTokenIs(builder_, INSTRUCTION_PREFIX)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INSTRUCTION_PREFIX);
    exit_section_(builder_, marker_, VEX_PREFIX, result_);
    return result_;
  }

  /* ********************************************************** */
  // WRT (symbol_ref | WRT_SUFFIX)
  static boolean wrt_suffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "wrt_suffix")) return false;
    if (!nextTokenIs(builder_, WRT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, WRT);
    pinned_ = result_; // pin = 1
    result_ = result_ && wrt_suffix_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // symbol_ref | WRT_SUFFIX
  private static boolean wrt_suffix_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "wrt_suffix_1")) return false;
    boolean result_;
    result_ = symbol_ref(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, WRT_SUFFIX);
    return result_;
  }

  /* ********************************************************** */
  // and_expr (CARET and_expr)*
  public static boolean xor_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "xor_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<xor expr>");
    result_ = and_expr(builder_, level_ + 1);
    result_ = result_ && xor_expr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (CARET and_expr)*
  private static boolean xor_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "xor_expr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!xor_expr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "xor_expr_1", pos_)) break;
    }
    return true;
  }

  // CARET and_expr
  private static boolean xor_expr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "xor_expr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CARET);
    result_ = result_ && and_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

}
