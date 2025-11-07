package dev.agb.nasmplugin.eval

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.psi.*

/**
 * Service for evaluating constant expressions in NASM code.
 *
 * Supports:
 * - All numeric and character literals
 * - All NASM operators (arithmetic, bitwise, logical, relational, shifts)
 * - References to EQU constants
 * - References to single-line macros (%define with constant values)
 *
 * Future extensibility:
 * - Can be extended to support macro function evaluation
 * - Can support additional preprocessor functions
 */
object NasmConstExprEvaluator {

    /**
     * Result of expression evaluation
     */
    sealed class EvalResult {
        /** Successfully evaluated to a constant value */
        data class Value(val value: Long) : EvalResult()

        /** Expression contains unresolvable references or non-constant constructs */
        object NotConstant : EvalResult()

        /** Expression evaluation failed (division by zero, invalid operations, etc.) */
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluation context to track visited symbols for cycle detection
     * and to prevent recursion during preprocessor evaluation
     */
    private data class EvalContext(
        val visitedSymbols: MutableSet<String> = mutableSetOf(),
        var insidePreprocessorEvaluation: Boolean = false
    )

    /**
     * Thread-local evaluation context for cycle detection
     */
    private val evalContext = ThreadLocal.withInitial { EvalContext() }

    /**
     * Check if we're currently inside preprocessor evaluation.
     * This is used to prevent circular dependencies when evaluating conditional directives.
     */
    fun isInsidePreprocessorEvaluation(): Boolean {
        return evalContext.get().insidePreprocessorEvaluation
    }

    /**
     * Evaluate a NASM expression to a constant value.
     *
     * @param expr The expression to evaluate
     * @param macroValues Optional map of macro names to their constant values (for preprocessor context)
     * @return The evaluation result (value, not constant, or error)
     */
    fun evaluate(expr: PsiElement?, macroValues: Map<String, Long>? = null): EvalResult {
        if (expr == null) return EvalResult.NotConstant

        return try {
            // Reset context for top-level evaluation
            evalContext.get().visitedSymbols.clear()
            if (macroValues != null) {
                evaluateWithMacroContext(expr, macroValues)
            } else {
                evaluateExpression(expr)
            }
        } catch (e: Exception) {
            EvalResult.Error("Evaluation failed: ${e.message}")
        } finally {
            // Clean up context
            evalContext.get().visitedSymbols.clear()
        }
    }

    /**
     * Evaluate an expression with a provided macro context.
     * This is used by the preprocessor to evaluate expressions with command-line macros.
     */
    private fun evaluateWithMacroContext(element: PsiElement, macroValues: Map<String, Long>): EvalResult {
        // Mark that we're inside preprocessor evaluation to prevent circular dependencies
        val wasInsideEvaluation = evalContext.get().insidePreprocessorEvaluation
        evalContext.get().insidePreprocessorEvaluation = true
        try {
            return when (element) {
                is NasmExpression -> evaluateNasmExpressionWithMacros(element, macroValues)
                is NasmAtomExpr -> evaluateAtomExprWithMacros(element, macroValues)
                is NasmSymbolRef -> evaluateSymbolRefWithMacros(element, macroValues)
                else -> {
                    val childExpr = PsiTreeUtil.findChildOfType(element, NasmExpression::class.java)
                    if (childExpr != null) {
                        evaluateWithMacroContext(childExpr, macroValues)
                    } else {
                        EvalResult.NotConstant
                    }
                }
            }
        } finally {
            evalContext.get().insidePreprocessorEvaluation = wasInsideEvaluation
        }
    }

    private fun evaluateNasmExpressionWithMacros(expr: NasmExpression, macroValues: Map<String, Long>): EvalResult {
        val allNodes = expr.node.getChildren(null).toList()
        val significantNodes = allNodes.filter { it.elementType != com.intellij.psi.TokenType.WHITE_SPACE }

        if (significantNodes.size == 1 && significantNodes[0].psi != null) {
            return evaluateWithMacroContext(significantNodes[0].psi!!, macroValues)
        }

        val unaryInfo = findUnaryOperator(significantNodes)
        if (unaryInfo != null) {
            return evaluateUnaryOpWithMacros(unaryInfo, macroValues)
        }

        val operatorInfo = findBinaryOperator(significantNodes)
        if (operatorInfo != null) {
            return evaluateBinaryOpWithMacros(operatorInfo, macroValues)
        }

        if (significantNodes.size == 3 &&
            significantNodes[0].elementType == NasmTypes.LPAREN &&
            significantNodes[2].elementType == NasmTypes.RPAREN &&
            significantNodes[1].psi != null) {
            return evaluateWithMacroContext(significantNodes[1].psi!!, macroValues)
        }

        val children = expr.children.toList()
        if (children.size == 1) {
            return evaluateWithMacroContext(children[0], macroValues)
        }

        return EvalResult.NotConstant
    }

    private fun evaluateUnaryOpWithMacros(info: UnaryOpInfo, macroValues: Map<String, Long>): EvalResult {
        val operandResult = evaluateNodesWithMacros(info.operandNodes, macroValues)
        return applyUnaryOperator(info.operator, operandResult)
    }

    private fun evaluateBinaryOpWithMacros(info: BinaryOpInfo, macroValues: Map<String, Long>): EvalResult {
        val leftResult = evaluateNodesWithMacros(info.leftNodes, macroValues)
        val rightResult = evaluateNodesWithMacros(info.rightNodes, macroValues)

        if (leftResult !is EvalResult.Value || rightResult !is EvalResult.Value) {
            return leftResult as? EvalResult.Error
                ?: (rightResult as? EvalResult.Error ?: EvalResult.NotConstant)
        }

        return applyBinaryOperator(info.operator, leftResult.value, rightResult.value)
    }

    private fun evaluateNodesWithMacros(nodes: List<com.intellij.lang.ASTNode>, macroValues: Map<String, Long>): EvalResult {
        return evaluateNodesCore(
            nodes = nodes,
            unaryEvaluator = { evaluateUnaryOpWithMacros(it, macroValues) },
            binaryEvaluator = { evaluateBinaryOpWithMacros(it, macroValues) },
            psiEvaluator = { evaluateWithMacroContext(it, macroValues) }
        )
    }

    private fun evaluateAtomExprWithMacros(atomExpr: NasmAtomExpr, macroValues: Map<String, Long>): EvalResult {
        return evaluateAtomExprCore(
            symbolRef = atomExpr.symbolRef,
            expression = atomExpr.expression,
            functionMacroCall = atomExpr.functionMacroCall,
            builtinFunction = atomExpr.builtinFunction,
            macroExpansion = atomExpr.macroExpansion,
            envVarRef = atomExpr.envVarRef,
            floatFormat = atomExpr.floatFormat,
            firstChild = atomExpr.firstChild,
            symbolRefEvaluator = { evaluateSymbolRefWithMacros(it!!, macroValues) },
            expressionEvaluator = { evaluateWithMacroContext(it, macroValues) },
            functionMacroCallEvaluator = { evaluateFunctionMacroCall(it) } // Function macros use normal evaluation
        )
    }

    private fun evaluateSymbolRefWithMacros(symbolRef: NasmSymbolRef, macroValues: Map<String, Long>): EvalResult {
        val symbolName = symbolRef.text

        // First, check if this symbol is in the provided macro context
        val macroValue = macroValues[symbolName]
        if (macroValue != null) {
            return EvalResult.Value(macroValue)
        }

        // Otherwise, fall back to normal symbol resolution
        val resolved = symbolRef.reference?.resolve()
        return evaluateResolvedSymbol(symbolName, resolved, symbolRef.containingFile)
    }

    private fun evaluateExpression(element: PsiElement): EvalResult {
        return when (element) {
            is NasmExpression -> evaluateNasmExpression(element)
            is NasmAtomExpr -> evaluateAtomExpr(element)
            else -> evaluateAtomicElement(element)
        }
    }

    private fun evaluateNasmExpression(expr: NasmExpression): EvalResult {
        // Get all nodes including leaf tokens
        val allNodes = expr.node.getChildren(null).toList()

        // Filter out whitespace
        val significantNodes = allNodes.filter { it.elementType != com.intellij.psi.TokenType.WHITE_SPACE }

        // Single child - recurse
        if (significantNodes.size == 1 && significantNodes[0].psi != null) {
            return evaluateExpression(significantNodes[0].psi!!)
        }

        // Check for unary operators FIRST (before binary, since +/- can be both)
        val unaryInfo = findUnaryOperator(significantNodes)
        if (unaryInfo != null) {
            return evaluateUnaryOp(unaryInfo)
        }

        // Look for binary operators
        val operatorInfo = findBinaryOperator(significantNodes)
        if (operatorInfo != null) {
            return evaluateBinaryOp(operatorInfo)
        }

        // Parenthesized expression - recurse on inner expression
        if (significantNodes.size == 3 &&
            significantNodes[0].elementType == NasmTypes.LPAREN &&
            significantNodes[2].elementType == NasmTypes.RPAREN &&
            significantNodes[1].psi != null) {
            return evaluateExpression(significantNodes[1].psi!!)
        }

        // Try just evaluating the children
        val children = expr.children.toList()
        if (children.size == 1) {
            return evaluateExpression(children[0])
        }

        // Default to not constant
        return EvalResult.NotConstant
    }

    data class BinaryOpInfo(
        val operator: com.intellij.psi.tree.IElementType,
        val operatorIndex: Int,
        val leftNodes: List<com.intellij.lang.ASTNode>,
        val rightNodes: List<com.intellij.lang.ASTNode>
    )

    data class UnaryOpInfo(
        val operator: com.intellij.psi.tree.IElementType,
        val operandNodes: List<com.intellij.lang.ASTNode>
    )

    private fun findBinaryOperator(nodes: List<com.intellij.lang.ASTNode>): BinaryOpInfo? {
        // Binary operators - check in precedence order (LOWEST to HIGHEST)
        // Find the LOWEST precedence operator to split at the top level
        // Check from lowest to highest, and for each precedence level, find the LAST (rightmost) occurrence
        val operatorsByPrecedence = listOf(
            // Logical OR (lowest precedence)
            listOf(NasmTypes.BOOLEAN_OR),
            // Logical XOR
            listOf(NasmTypes.BOOLEAN_XOR),
            // Logical AND
            listOf(NasmTypes.BOOLEAN_AND),
            // Bitwise OR
            listOf(NasmTypes.PIPE),
            // Bitwise XOR
            listOf(NasmTypes.CARET),
            // Bitwise AND
            listOf(NasmTypes.AMP),
            // Comparison operators
            listOf(NasmTypes.EQ_EQ, NasmTypes.NOT_EQUAL_1, NasmTypes.NOT_EQUAL_2,
                   NasmTypes.LT, NasmTypes.LTE, NasmTypes.GT, NasmTypes.GTE, NasmTypes.SPACESHIP),
            // Shift operators
            listOf(NasmTypes.LSHIFT, NasmTypes.LSHIFT_COMPLETE, NasmTypes.RSHIFT, NasmTypes.RSHIFT_COMPLETE),
            // Additive operators
            listOf(NasmTypes.PLUS, NasmTypes.MINUS),
            // Multiplicative operators
            listOf(NasmTypes.MUL, NasmTypes.DIV, NasmTypes.SIGNED_DIV, NasmTypes.MOD, NasmTypes.SIGNED_MOD),
            // Paste operator (highest precedence for binary ops)
            listOf(NasmTypes.PASTE_OP)
        )

        // For each precedence level (from lowest to highest)
        for (precedenceLevel in operatorsByPrecedence) {
            // Find the LAST occurrence of an operator at this precedence level (rightmost for left-to-right associativity)
            var lastIndex = -1
            for ((index, node) in nodes.withIndex()) {
                if (node.elementType in precedenceLevel && index > 0) {
                    lastIndex = index
                }
            }

            if (lastIndex >= 0) {
                return BinaryOpInfo(
                    operator = nodes[lastIndex].elementType,
                    operatorIndex = lastIndex,
                    leftNodes = nodes.subList(0, lastIndex),
                    rightNodes = nodes.subList(lastIndex + 1, nodes.size)
                )
            }
        }

        return null
    }

    private fun findUnaryOperator(nodes: List<com.intellij.lang.ASTNode>): UnaryOpInfo? {
        if (nodes.isEmpty()) return null

        val unaryOps = listOf(NasmTypes.PLUS, NasmTypes.MINUS, NasmTypes.TILDE, NasmTypes.EXCLAIM, NasmTypes.SEG)

        if (nodes[0].elementType in unaryOps && nodes.size >= 2) {
            return UnaryOpInfo(
                operator = nodes[0].elementType,
                operandNodes = nodes.subList(1, nodes.size)
            )
        }

        return null
    }

    private fun evaluateBinaryOp(info: BinaryOpInfo): EvalResult {
        // Evaluate left and right sides
        val leftResult = evaluateNodes(info.leftNodes)
        val rightResult = evaluateNodes(info.rightNodes)

        if (leftResult !is EvalResult.Value || rightResult !is EvalResult.Value) {
            return leftResult as? EvalResult.Error
                ?: (rightResult as? EvalResult.Error ?: EvalResult.NotConstant)
        }

        return applyBinaryOperator(info.operator, leftResult.value, rightResult.value)
    }

    private fun evaluateUnaryOp(info: UnaryOpInfo): EvalResult {
        val operandResult = evaluateNodes(info.operandNodes)
        return applyUnaryOperator(info.operator, operandResult)
    }

    private fun applyUnaryOperator(operator: com.intellij.psi.tree.IElementType, operandResult: EvalResult): EvalResult {
        if (operandResult !is EvalResult.Value) {
            return operandResult
        }

        return when (operator) {
            NasmTypes.PLUS -> EvalResult.Value(operandResult.value)
            NasmTypes.MINUS -> EvalResult.Value(-operandResult.value)
            NasmTypes.TILDE -> EvalResult.Value(operandResult.value.inv())
            NasmTypes.EXCLAIM -> EvalResult.Value(if (operandResult.value == 0L) 1L else 0L)
            NasmTypes.SEG -> EvalResult.NotConstant // SEG operator requires runtime information
            else -> EvalResult.NotConstant
        }
    }

    private fun evaluateNodes(nodes: List<com.intellij.lang.ASTNode>): EvalResult {
        return evaluateNodesCore(
            nodes = nodes,
            unaryEvaluator = ::evaluateUnaryOp,
            binaryEvaluator = ::evaluateBinaryOp,
            psiEvaluator = ::evaluateExpression
        )
    }

    private fun evaluateNodesCore(
        nodes: List<com.intellij.lang.ASTNode>,
        unaryEvaluator: (UnaryOpInfo) -> EvalResult,
        binaryEvaluator: (BinaryOpInfo) -> EvalResult,
        psiEvaluator: (PsiElement) -> EvalResult
    ): EvalResult {
        if (nodes.isEmpty()) return EvalResult.NotConstant

        // Filter whitespace
        val significantNodes = nodes.filter { it.elementType != com.intellij.psi.TokenType.WHITE_SPACE }

        // Single node - evaluate it
        if (significantNodes.size == 1) {
            val psi = significantNodes[0].psi
            return if (psi != null) psiEvaluator(psi) else EvalResult.NotConstant
        }

        // Multiple nodes - check for operators
        // Try unary first
        val unaryInfo = findUnaryOperator(significantNodes)
        if (unaryInfo != null) {
            return unaryEvaluator(unaryInfo)
        }

        // Try binary
        val binaryInfo = findBinaryOperator(significantNodes)
        if (binaryInfo != null) {
            return binaryEvaluator(binaryInfo)
        }

        // Just evaluate the first PSI element as fallback
        val psiNode = significantNodes.firstOrNull { it.psi != null }
        if (psiNode != null && psiNode.psi != null) {
            return psiEvaluator(psiNode.psi!!)
        }

        return EvalResult.NotConstant
    }


    private fun applyBinaryOperator(operatorType: com.intellij.psi.tree.IElementType, left: Long, right: Long): EvalResult {
        return try {
            val result = when (operatorType) {
                // Arithmetic
                NasmTypes.PLUS -> left + right
                NasmTypes.MINUS -> left - right
                NasmTypes.MUL -> left * right
                NasmTypes.DIV -> if (right == 0L) return EvalResult.Error("Division by zero") else left / right
                NasmTypes.SIGNED_DIV -> if (right == 0L) return EvalResult.Error("Division by zero") else left / right
                NasmTypes.MOD -> if (right == 0L) return EvalResult.Error("Division by zero") else left % right
                NasmTypes.SIGNED_MOD -> if (right == 0L) return EvalResult.Error("Division by zero") else left % right

                // Bitwise
                NasmTypes.AMP -> left and right
                NasmTypes.PIPE -> left or right
                NasmTypes.CARET -> left xor right

                // Shifts
                NasmTypes.LSHIFT, NasmTypes.LSHIFT_COMPLETE -> left shl right.toInt()
                NasmTypes.RSHIFT, NasmTypes.RSHIFT_COMPLETE -> left ushr right.toInt()

                // Comparison
                NasmTypes.EQ_EQ -> if (left == right) 1L else 0L
                NasmTypes.NOT_EQUAL_1, NasmTypes.NOT_EQUAL_2 -> if (left != right) 1L else 0L
                NasmTypes.LT -> if (left < right) 1L else 0L
                NasmTypes.LTE -> if (left <= right) 1L else 0L
                NasmTypes.GT -> if (left > right) 1L else 0L
                NasmTypes.GTE -> if (left >= right) 1L else 0L
                NasmTypes.SPACESHIP -> when {
                    left < right -> -1L
                    left > right -> 1L
                    else -> 0L
                }

                // Logical (treat non-zero as true)
                NasmTypes.BOOLEAN_AND -> if (left != 0L && right != 0L) 1L else 0L
                NasmTypes.BOOLEAN_OR -> if (left != 0L || right != 0L) 1L else 0L
                NasmTypes.BOOLEAN_XOR -> if ((left != 0L) xor (right != 0L)) 1L else 0L

                else -> return EvalResult.NotConstant
            }

            EvalResult.Value(result)
        } catch (e: ArithmeticException) {
            EvalResult.Error("Arithmetic error: ${e.message}")
        }
    }


    private fun evaluateAtomExpr(atomExpr: NasmAtomExpr): EvalResult {
        return evaluateAtomExprCore(
            symbolRef = atomExpr.symbolRef,
            expression = atomExpr.expression,
            functionMacroCall = atomExpr.functionMacroCall,
            builtinFunction = atomExpr.builtinFunction,
            macroExpansion = atomExpr.macroExpansion,
            envVarRef = atomExpr.envVarRef,
            floatFormat = atomExpr.floatFormat,
            firstChild = atomExpr.firstChild,
            symbolRefEvaluator = ::evaluateSymbolRef,
            expressionEvaluator = ::evaluateExpression,
            functionMacroCallEvaluator = ::evaluateFunctionMacroCall
        )
    }

    private fun evaluateAtomExprCore(
        symbolRef: NasmSymbolRef?,
        expression: NasmExpression?,
        functionMacroCall: NasmFunctionMacroCall?,
        builtinFunction: PsiElement?,
        macroExpansion: PsiElement?,
        envVarRef: PsiElement?,
        floatFormat: PsiElement?,
        firstChild: PsiElement?,
        symbolRefEvaluator: (NasmSymbolRef?) -> EvalResult,
        expressionEvaluator: (PsiElement) -> EvalResult,
        functionMacroCallEvaluator: (NasmFunctionMacroCall) -> EvalResult
    ): EvalResult {
        // Check the structured children first
        symbolRef?.let { return symbolRefEvaluator(it) }
        expression?.let { return expressionEvaluator(it) }
        functionMacroCall?.let { return functionMacroCallEvaluator(it) }
        builtinFunction?.let { return EvalResult.NotConstant } // Not yet supported
        macroExpansion?.let { return EvalResult.NotConstant } // Not yet supported
        envVarRef?.let { return EvalResult.NotConstant } // Not yet supported
        floatFormat?.let { return EvalResult.NotConstant } // Not yet supported

        // Check for leaf tokens (NUMBER, STRING, FLOAT, etc.)
        if (firstChild != null) {
            return when (firstChild.node.elementType) {
                NasmTypes.NUMBER -> evaluateNumber(firstChild.text)
                NasmTypes.STRING -> evaluateString(firstChild.text)
                NasmTypes.FLOAT -> EvalResult.NotConstant // Float literals not supported in const expr
                NasmTypes.SPECIAL_FLOAT -> EvalResult.NotConstant
                NasmTypes.REGISTER, NasmTypes.SEG_REGISTER, NasmTypes.MASK_REG -> EvalResult.NotConstant
                else -> EvalResult.NotConstant
            }
        }

        return EvalResult.NotConstant
    }

    private fun evaluateAtomicElement(element: PsiElement): EvalResult {
        return when (element.node.elementType) {
            NasmTypes.NUMBER -> evaluateNumber(element.text)
            NasmTypes.STRING -> evaluateString(element.text)
            NasmTypes.SYMBOL_REF -> evaluateSymbolRef(element as? NasmSymbolRef)
            NasmTypes.ATOM_EXPR -> evaluateAtomExpr(element as NasmAtomExpr)
            else -> {
                // Check if it contains nested expressions
                val childExpr = PsiTreeUtil.findChildOfType(element, NasmExpression::class.java)
                if (childExpr != null) {
                    evaluateExpression(childExpr)
                } else {
                    EvalResult.NotConstant
                }
            }
        }
    }

    private fun evaluateNumber(text: String): EvalResult {
        return try {
            val value = when {
                // Hexadecimal: 0x1234
                text.startsWith("0x", ignoreCase = true) ->
                    text.substring(2).replace("_", "").toLong(16)

                // Binary: 0b1010
                text.startsWith("0b", ignoreCase = true) ->
                    text.substring(2).replace("_", "").toLong(2)

                // Decimal with suffix: 123d, 0d123
                text.startsWith("0d", ignoreCase = true) ->
                    text.substring(2).replace("_", "").toLong(10)
                text.endsWith("d", ignoreCase = true) && text.length > 1 ->
                    text.dropLast(1).replace("_", "").toLong(10)

                // Plain decimal (including leading zeros which may be intended as octal in NASM)
                // For now, treat all other numbers as decimal
                // TODO: Add proper octal/hex suffix support when lexer supports it
                else -> text.replace("_", "").toLong(10)
            }

            EvalResult.Value(value)
        } catch (e: NumberFormatException) {
            EvalResult.Error("Invalid number format: $text")
        }
    }

    private fun evaluateString(text: String): EvalResult {
        // String literals in expressions are treated as character constants
        // Single character: 'A' -> 65
        // Multiple characters are packed: 'AB' -> depends on endianness, typically 0x4142

        if (text.length < 2) return EvalResult.Error("Invalid string literal")

        val quote = text[0]
        val content = text.substring(1, text.length - 1)

        if (content.isEmpty()) return EvalResult.Value(0)

        // Handle escape sequences
        val chars = parseEscapeSequences(content)

        if (chars.size == 1) {
            return EvalResult.Value(chars[0].code.toLong())
        }

        // Pack multiple characters (little-endian)
        var value = 0L
        for (i in chars.indices.reversed()) {
            value = (value shl 8) or (chars[i].code.toLong() and 0xFF)
        }

        return EvalResult.Value(value)
    }

    private fun parseEscapeSequences(str: String): List<Char> {
        val result = mutableListOf<Char>()
        var i = 0

        while (i < str.length) {
            if (str[i] == '\\' && i + 1 < str.length) {
                when (str[i + 1]) {
                    'n' -> { result.add('\n'); i += 2 }
                    'r' -> { result.add('\r'); i += 2 }
                    't' -> { result.add('\t'); i += 2 }
                    '\\' -> { result.add('\\'); i += 2 }
                    '\'' -> { result.add('\''); i += 2 }
                    '"' -> { result.add('"'); i += 2 }
                    '0' -> { result.add('\u0000'); i += 2 }
                    'x' -> {
                        // Hex escape: \xNN
                        if (i + 3 < str.length) {
                            val hex = str.substring(i + 2, i + 4)
                            try {
                                result.add(hex.toInt(16).toChar())
                                i += 4
                            } catch (e: NumberFormatException) {
                                result.add(str[i])
                                i++
                            }
                        } else {
                            result.add(str[i])
                            i++
                        }
                    }
                    else -> {
                        // Unknown escape, keep the backslash
                        result.add(str[i])
                        i++
                    }
                }
            } else {
                result.add(str[i])
                i++
            }
        }

        return result
    }

    private fun evaluateSymbolRef(symbolRef: NasmSymbolRef?): EvalResult {
        if (symbolRef == null) return EvalResult.NotConstant

        val resolved = symbolRef.reference?.resolve()
        return evaluateResolvedSymbol(symbolRef.text, resolved, symbolRef.containingFile)
    }

    private fun evaluateResolvedSymbol(symbolName: String, resolved: PsiElement?, contextFile: PsiFile): EvalResult {
        val context = evalContext.get()

        // Check for circular reference
        if (symbolName in context.visitedSymbols) {
            return EvalResult.Error("Circular reference detected: $symbolName")
        }

        // Mark this symbol as being visited
        context.visitedSymbols.add(symbolName)

        try {
            return when (resolved) {
                is NasmEquDefinition -> {
                    // EQU constant - evaluate its expression
                    val equExpr = resolved.expression
                    if (equExpr != null) {
                        evaluateExpression(equExpr)
                    } else {
                        EvalResult.NotConstant
                    }
                }
                is NasmPpDefineStmt -> {
                    // %define single-line macro - evaluate its body if it's a constant
                    val macroBody = resolved.macroBodyInline
                    if (macroBody != null) {
                        evaluateMacroBody(macroBody, contextFile)
                    } else {
                        EvalResult.NotConstant
                    }
                }
                is NasmPpAssignStmt -> {
                    // %assign - similar to %define, evaluate its expression
                    val assignExpr = resolved.expression
                    if (assignExpr != null) {
                        evaluateExpression(assignExpr)
                    } else {
                        EvalResult.NotConstant
                    }
                }
                else -> EvalResult.NotConstant
            }
        } finally {
            // Remove from visited set after evaluation
            context.visitedSymbols.remove(symbolName)
        }
    }

    /**
     * Evaluate a macro body inline (token sequence).
     *
     * This handles both simple cases (single number, single identifier) and complex
     * expressions (like "BASE + 50") by re-parsing the token sequence as an expression.
     *
     * @param macroBody The macro body inline element
     * @param contextFile The file containing the macro definition (for symbol resolution)
     * @return The evaluation result
     */
    private fun evaluateMacroBody(macroBody: NasmMacroBodyInline, contextFile: PsiFile): EvalResult {
        // A macro body is a token sequence - we need to check if it's a simple constant expression
        val tokenSeq = macroBody.tokenSequence

        // Try to find a direct expression in the token sequence first
        val expr = PsiTreeUtil.findChildOfType(tokenSeq, NasmExpression::class.java)
        if (expr != null) {
            return evaluateExpression(expr)
        }

        // Get the text of the macro body
        val bodyText = tokenSeq.text.trim()
        if (bodyText.isEmpty()) {
            return EvalResult.NotConstant
        }

        // If there's only one token, try to evaluate it directly as a simple case
        if (tokenSeq.children.size == 1) {
            val singleToken = tokenSeq.children[0]

            // Check if this token is a NUMBER
            val firstChild = singleToken.firstChild
            if (firstChild != null && firstChild.node.elementType == NasmTypes.NUMBER) {
                return evaluateNumber(firstChild.text)
            }

            // Check if it's an identifier (symbol reference)
            if (firstChild != null && firstChild.node.elementType == NasmTypes.IDENTIFIER) {
                // Try to resolve this as a symbol reference
                val symbolRef = PsiTreeUtil.findChildOfType(singleToken, NasmSymbolRef::class.java)
                if (symbolRef != null) {
                    return evaluateSymbolRef(symbolRef)
                }
            }
        }

        // For complex macro bodies (like "BASE + 50"), we need to re-parse as an expression
        // We create a temporary instruction containing the expression and parse it
        return reparseAndEvaluate(bodyText, contextFile)
    }

    /**
     * Re-parse a token sequence as an expression and evaluate it.
     *
     * This creates a temporary NASM file with the text wrapped in an instruction,
     * parses it to get an expression tree, and evaluates it. Symbol references are
     * resolved in the context of the original file.
     *
     * @param exprText The text to parse as an expression
     * @param contextFile The file to use for symbol resolution
     * @return The evaluation result
     */
    private fun reparseAndEvaluate(exprText: String, contextFile: PsiFile): EvalResult {
        try {
            // Create a temporary instruction operand containing the expression
            // We use "mov rax, <expr>" to ensure it's parsed as an operand expression
            val tempCode = "mov rax, $exprText"
            val project = contextFile.project

            val tempFile = PsiFileFactory.getInstance(project)
                .createFileFromText("temp.asm", NasmFileType, tempCode)

            // Find the expression in the temporary file
            val tempExpr = PsiTreeUtil.findChildOfType(tempFile, NasmExpression::class.java)
                ?: return EvalResult.NotConstant

            // Evaluate the expression, but use a custom resolver for symbol references
            // that searches in the original context file instead of the temp file
            return evaluateWithCustomContext(tempExpr, contextFile)

        } catch (_: Exception) {
            // If reparsing fails, return NotConstant
            return EvalResult.NotConstant
        }
    }

    /**
     * Evaluate an expression with custom symbol resolution context.
     *
     * This recursively evaluates an expression tree, but when encountering symbol
     * references, it resolves them in the provided context file instead of using
     * the normal PSI reference resolution.
     *
     * @param element The element to evaluate
     * @param contextFile The file to search for symbol definitions
     * @return The evaluation result
     */
    private fun evaluateWithCustomContext(element: PsiElement, contextFile: PsiFile): EvalResult {
        return when (element) {
            is NasmExpression -> evaluateNasmExpressionWithContext(element, contextFile)
            is NasmAtomExpr -> evaluateAtomExprWithContext(element, contextFile)
            is NasmSymbolRef -> evaluateSymbolRefInContext(element, contextFile)
            else -> {
                // Check if it contains nested expressions
                val childExpr = PsiTreeUtil.findChildOfType(element, NasmExpression::class.java)
                if (childExpr != null) {
                    evaluateWithCustomContext(childExpr, contextFile)
                } else {
                    EvalResult.NotConstant
                }
            }
        }
    }

    private fun evaluateNasmExpressionWithContext(expr: NasmExpression, contextFile: PsiFile): EvalResult {
        val allNodes = expr.node.getChildren(null).toList()
        val significantNodes = allNodes.filter { it.elementType != com.intellij.psi.TokenType.WHITE_SPACE }

        // Single child - recurse
        if (significantNodes.size == 1 && significantNodes[0].psi != null) {
            return evaluateWithCustomContext(significantNodes[0].psi!!, contextFile)
        }

        // Check for unary operators
        val unaryInfo = findUnaryOperator(significantNodes)
        if (unaryInfo != null) {
            return evaluateUnaryOpWithContext(unaryInfo, contextFile)
        }

        // Look for binary operators
        val operatorInfo = findBinaryOperator(significantNodes)
        if (operatorInfo != null) {
            return evaluateBinaryOpWithContext(operatorInfo, contextFile)
        }

        // Parenthesized expression
        if (significantNodes.size == 3 &&
            significantNodes[0].elementType == NasmTypes.LPAREN &&
            significantNodes[2].elementType == NasmTypes.RPAREN &&
            significantNodes[1].psi != null) {
            return evaluateWithCustomContext(significantNodes[1].psi!!, contextFile)
        }

        // Try just evaluating the children
        val children = expr.children.toList()
        if (children.size == 1) {
            return evaluateWithCustomContext(children[0], contextFile)
        }

        return EvalResult.NotConstant
    }

    private fun evaluateUnaryOpWithContext(info: UnaryOpInfo, contextFile: PsiFile): EvalResult {
        val operandResult = evaluateNodesWithContext(info.operandNodes, contextFile)
        return applyUnaryOperator(info.operator, operandResult)
    }

    private fun evaluateBinaryOpWithContext(info: BinaryOpInfo, contextFile: PsiFile): EvalResult {
        val leftResult = evaluateNodesWithContext(info.leftNodes, contextFile)
        val rightResult = evaluateNodesWithContext(info.rightNodes, contextFile)

        if (leftResult !is EvalResult.Value || rightResult !is EvalResult.Value) {
            return leftResult as? EvalResult.Error
                ?: (rightResult as? EvalResult.Error ?: EvalResult.NotConstant)
        }

        return applyBinaryOperator(info.operator, leftResult.value, rightResult.value)
    }

    private fun evaluateNodesWithContext(nodes: List<com.intellij.lang.ASTNode>, contextFile: PsiFile): EvalResult {
        return evaluateNodesCore(
            nodes = nodes,
            unaryEvaluator = { evaluateUnaryOpWithContext(it, contextFile) },
            binaryEvaluator = { evaluateBinaryOpWithContext(it, contextFile) },
            psiEvaluator = { evaluateWithCustomContext(it, contextFile) }
        )
    }

    private fun evaluateAtomExprWithContext(atomExpr: NasmAtomExpr, contextFile: PsiFile): EvalResult {
        return evaluateAtomExprCore(
            symbolRef = atomExpr.symbolRef,
            expression = atomExpr.expression,
            functionMacroCall = atomExpr.functionMacroCall,
            builtinFunction = atomExpr.builtinFunction,
            macroExpansion = atomExpr.macroExpansion,
            envVarRef = atomExpr.envVarRef,
            floatFormat = atomExpr.floatFormat,
            firstChild = atomExpr.firstChild,
            symbolRefEvaluator = { evaluateSymbolRefInContext(it!!, contextFile) },
            expressionEvaluator = { evaluateWithCustomContext(it, contextFile) },
            functionMacroCallEvaluator = { evaluateFunctionMacroCallInContext(it, contextFile) }
        )
    }

    /**
     * Resolve and evaluate a symbol reference in a custom context file.
     *
     * This searches for the symbol definition in the provided context file
     * instead of using the normal PSI reference resolution.
     *
     * @param symbolRef The symbol reference from the temporary parsed file
     * @param contextFile The original file to search for the symbol definition
     * @return The evaluation result
     */
    private fun evaluateSymbolRefInContext(symbolRef: NasmSymbolRef, contextFile: PsiFile): EvalResult {
        val symbolName = symbolRef.text

        // Search for the symbol definition in the context file
        val definitions = PsiTreeUtil.findChildrenOfType(contextFile, NasmNamedElement::class.java)
        val resolved = definitions.firstOrNull { it.name == symbolName }

        return evaluateResolvedSymbol(symbolName, resolved, contextFile)
    }

    /**
     * Evaluate a function-like macro call by expanding its body with arguments.
     *
     * This handles macros like %define ADD(x, y) ((x) + (y)) when called as ADD(10, 20).
     * It substitutes parameters with argument values and evaluates the result.
     *
     * @param functionCall The function macro call element
     * @return The evaluation result
     */
    private fun evaluateFunctionMacroCall(functionCall: NasmFunctionMacroCall): EvalResult {
        // Resolve the macro definition
        val resolved = functionCall.reference?.resolve() as? NasmPpDefineStmt
            ?: return EvalResult.NotConstant

        return expandAndEvaluateMacroCall(
            macroName = resolved.name ?: return EvalResult.NotConstant,
            macroBody = resolved.macroBodyInline ?: return EvalResult.NotConstant,
            params = resolved.macroParams?.paramList?.paramSpecList?.map { it.paramName.text } ?: return EvalResult.NotConstant,
            args = functionCall.macroArgList,
            contextFile = functionCall.containingFile
        )
    }

    /**
     * Evaluate a function-like macro call in a custom context.
     *
     * This is similar to evaluateFunctionMacroCall but resolves the macro in the context file.
     *
     * @param functionCall The function macro call from the temporary parsed file
     * @param contextFile The file to search for the macro definition
     * @return The evaluation result
     */
    private fun evaluateFunctionMacroCallInContext(functionCall: NasmFunctionMacroCall, contextFile: PsiFile): EvalResult {
        // Get the macro name
        val macroName = functionCall.text.substringBefore('(').trim()

        // Search for the macro definition in the context file
        val definitions = PsiTreeUtil.findChildrenOfType(contextFile, NasmPpDefineStmt::class.java)
        val resolved = definitions.firstOrNull { it.name == macroName }
            ?: return EvalResult.NotConstant

        return expandAndEvaluateMacroCall(
            macroName = macroName,
            macroBody = resolved.macroBodyInline ?: return EvalResult.NotConstant,
            params = resolved.macroParams?.paramList?.paramSpecList?.map { it.paramName.text } ?: return EvalResult.NotConstant,
            args = functionCall.macroArgList,
            contextFile = contextFile
        )
    }

    /**
     * Common logic for expanding and evaluating a function macro call.
     *
     * @param macroName The name of the macro (for error messages)
     * @param macroBody The macro body to expand
     * @param params The parameter names from the macro definition
     * @param args The arguments from the call
     * @param contextFile The file to use for evaluation context
     * @return The evaluation result
     */
    private fun expandAndEvaluateMacroCall(
        macroName: String,
        macroBody: NasmMacroBodyInline,
        params: List<String>,
        args: List<PsiElement>,
        contextFile: PsiFile
    ): EvalResult {
        // Validate argument count
        if (args.size != params.size) {
            return EvalResult.Error("Macro function $macroName expects ${params.size} arguments, got ${args.size}")
        }

        // Build a map of parameter name to argument text
        val paramMap = params.zip(args).associate { (paramName, arg) ->
            paramName to arg.text.trim()
        }

        // Get the macro body text
        val bodyText = macroBody.text.trim()

        // Perform parameter substitution in the body
        val expandedBody = substituteParameters(bodyText, paramMap)

        // Re-parse and evaluate the expanded body
        return reparseAndEvaluate(expandedBody, contextFile)
    }

    /**
     * Substitute macro parameters in a text with their argument values.
     *
     * This performs simple text substitution, replacing parameter names with argument values.
     * It tries to be smart about word boundaries to avoid replacing parts of identifiers.
     *
     * @param text The text containing parameter references
     * @param paramMap Map of parameter names to their values
     * @return The text with parameters substituted
     */
    private fun substituteParameters(text: String, paramMap: Map<String, String>): String {
        var result = text

        // Sort parameters by length (longest first) to avoid partial replacements
        // For example, if we have params "a" and "ab", we want to replace "ab" before "a"
        val sortedParams = paramMap.keys.sortedByDescending { it.length }

        for (paramName in sortedParams) {
            val paramValue = paramMap[paramName] ?: continue

            // Use regex to replace only whole word occurrences
            // This matches the parameter name only when it's surrounded by non-identifier characters
            val pattern = "\\b$paramName\\b".toRegex()
            result = result.replace(pattern, paramValue)
        }

        return result
    }
}
