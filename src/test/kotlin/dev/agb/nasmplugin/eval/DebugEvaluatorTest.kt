package dev.agb.nasmplugin.eval

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.NasmExpression
import dev.agb.nasmplugin.psi.NasmSymbolRef
import dev.agb.nasmplugin.psi.NasmPpDefineStmt

class DebugEvaluatorTest : BasePlatformTestCase() {

    fun testDefineChained() {
        val code = """
            %define BASE 100
            %define OFFSET BASE + 50
            mov rax, OFFSET
        """.trimIndent()
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\n=== FOUND EXPRESSION ===")
        if (expr != null) {
            println("Expression: '${expr.text}'")
            val symbolRef = PsiTreeUtil.findChildOfType(expr, NasmSymbolRef::class.java)
            println("Symbol ref: $symbolRef")
            if (symbolRef != null) {
                println("Symbol ref text: '${symbolRef.text}'")
                val reference = symbolRef.reference
                println("Reference: $reference")
                val resolved = reference?.resolve()
                println("Resolved: $resolved (${resolved?.javaClass?.simpleName})")
                if (resolved is NasmPpDefineStmt) {
                    println("Macro body inline: ${resolved.macroBodyInline}")
                    println("Macro body text: '${resolved.macroBodyInline?.text}'")
                    val tokenSeq = resolved.macroBodyInline?.tokenSequence
                    println("Token sequence: $tokenSeq")
                    println("Token seq children:")
                    tokenSeq?.children?.forEach {
                        println("  - ${it.javaClass.simpleName}: '${it.text}' [${it.node.elementType}]")
                        it.children.forEach { child ->
                            println("    - ${child.javaClass.simpleName}: '${child.text}' [${child.node.elementType}]")
                        }
                    }
                    // Look for expressions
                    val exprInBody = PsiTreeUtil.findChildOfType(tokenSeq, NasmExpression::class.java)
                    println("Expression in body: $exprInBody")
                    if (exprInBody != null) {
                        println("Expression text: '${exprInBody.text}'")
                    }
                }
            }
        }

        println("\n=== EVALUATING ===")
        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Result: $result")
    }

    fun testDefine() {
        val code = """
            %define MAGIC 0xDEADBEEF
            mov rax, MAGIC
        """.trimIndent()
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
        println("\n=== FOUND EXPRESSION ===")
        if (expr != null) {
            println("Expression: '${expr.text}'")
            val symbolRef = PsiTreeUtil.findChildOfType(expr, NasmSymbolRef::class.java)
            println("Symbol ref: $symbolRef")
            if (symbolRef != null) {
                println("Symbol ref text: '${symbolRef.text}'")
                val reference = symbolRef.reference
                println("Reference: $reference")
                val resolved = reference?.resolve()
                println("Resolved: $resolved (${resolved?.javaClass?.simpleName})")
                if (resolved is NasmPpDefineStmt) {
                    println("Macro body inline: ${resolved.macroBodyInline}")
                    println("Macro body text: '${resolved.macroBodyInline?.text}'")
                    val tokenSeq = resolved.macroBodyInline?.tokenSequence
                    println("Token sequence: $tokenSeq")
                    println("Token seq children:")
                    tokenSeq?.children?.forEach {
                        println("  - ${it.javaClass.simpleName}: '${it.text}' [${it.node.elementType}]")
                    }
                }
            }
        }

        println("\n=== EVALUATING ===")
        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Result: $result")
    }

    fun testBitwiseChain() {
        val code = "mov rax, (1 << 4) | (1 << 2) | (1 << 0)"
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        printPsiTree(file, 0)

        val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
        println("\n=== EVALUATING ===")
        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Result: $result")
    }

    fun testUnaryMinus() {
        val code = "mov rax, -42"
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        printPsiTree(file, 0)

        val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
        println("\n=== EVALUATING ===")
        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Result: $result")
    }

    fun testBinaryOp() {
        val code = "mov rax, 5 + 3"
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        printPsiTree(file, 0)

        val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
        println("\n=== EVALUATING ===")
        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Result: $result")
    }

    fun testHexLiteral() {
        val code = "mov rax, 0h10"
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        printPsiTree(file, 0)
    }

    fun testPrintPsiTree() {
        val code = "mov rax, 42"
        myFixture.configureByText("test.asm", code)

        val file = myFixture.file
        println("=== FILE PSI TREE ===")
        printPsiTree(file, 0)

        val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
        println("\n=== FOUND EXPRESSION ===")
        if (expr != null) {
            println("Expression text: '${expr.text}'")
            println("Expression class: ${expr.javaClass.name}")
            println("Expression node type: ${expr.node.elementType}")
            println("\nExpression children:")
            for (child in expr.children) {
                println("  - ${child.javaClass.simpleName}: '${child.text}' [${child.node.elementType}]")
                if (child.children.isEmpty()) {
                    // Print leaf node info
                    println("    Leaf node - first child: ${child.firstChild?.javaClass?.simpleName} [${child.firstChild?.node?.elementType}]")
                } else {
                    for (grandchild in child.children) {
                        println("    - ${grandchild.javaClass.simpleName}: '${grandchild.text}' [${grandchild.node.elementType}]")
                    }
                }
            }
        } else {
            println("No expression found!")
        }

        val result = NasmConstExprEvaluator.evaluate(expr)
        println("\n=== EVALUATION RESULT ===")
        println("Result: $result")
    }

    private fun printPsiTree(element: com.intellij.psi.PsiElement, indent: Int) {
        val prefix = "  ".repeat(indent)
        println("$prefix${element.javaClass.simpleName}: '${element.text.take(50)}' [${element.node.elementType}]")

        // Print ALL child nodes including leaf tokens
        element.node.getChildren(null).forEach { childNode ->
            val childPsi = childNode.psi
            if (childPsi != null) {
                printPsiTree(childPsi, indent + 1)
            } else {
                println("$prefix  LeafElement: '${childNode.text}' [${childNode.elementType}]")
            }
        }
    }
}
