package dev.agb.nasmplugin.eval

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

class MacroFunctionDebugTest : BasePlatformTestCase() {

    fun testMacroFunctionStructure() {
        val code = """
            %define MULTIPLY(a, b) ((a) * (b))
            VALUE equ MULTIPLY(5, 6)
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        println("\n=== MACRO DEFINITION ===")
        val defineStmt = PsiTreeUtil.findChildOfType(file, NasmPpDefineStmt::class.java)
        if (defineStmt != null) {
            println("Define: ${defineStmt.text}")
            println("Name: ${defineStmt.name}")

            val params = defineStmt.macroParams
            println("Has params: ${params != null}")
            if (params != null) {
                val paramList = params.paramList.paramSpecList
                println("Parameter count: ${paramList.size}")
                paramList.forEachIndexed { index, param ->
                    println("  Param $index: ${param.paramName.text}")
                }
            }

            val body = defineStmt.macroBodyInline
            println("Has body: ${body != null}")
            if (body != null) {
                println("Body text: '${body.text}'")
                val tokenSeq = body.tokenSequence
                println("Token sequence: ${tokenSeq?.text}")
            }
        }

        println("\n=== EQU WITH FUNCTION CALL ===")
        val equDef = PsiTreeUtil.findChildrenOfType(file, NasmEquDefinition::class.java).firstOrNull()
        if (equDef != null) {
            println("EQU: ${equDef.text}")
            val expr = equDef.expression
            println("Expression: ${expr?.text}")

            // Look for function macro call
            val functionCall = PsiTreeUtil.findChildOfType(expr, NasmFunctionMacroCall::class.java)
            println("Function call found: ${functionCall != null}")
            if (functionCall != null) {
                println("Function call text: '${functionCall.text}'")

                val args = functionCall.macroArgList
                println("Argument count: ${args.size}")
                args.forEachIndexed { index, arg ->
                    println("  Arg $index: '${arg.text}'")
                    println("    Token sequence: ${arg.tokenSequence?.text}")
                }

                // Try to resolve the function
                val resolved = functionCall.reference?.resolve()
                println("Resolved to: $resolved")
                if (resolved is NasmPpDefineStmt) {
                    println("  Resolved name: ${resolved.name}")
                    println("  Resolved params: ${resolved.macroParams?.paramList?.paramSpecList?.map { it.paramName.text }}")
                    println("  Resolved body: '${resolved.macroBodyInline?.text}'")
                }
            }
        }
    }

    fun testSimpleMacroFunction() {
        val code = """
            %define ADD(x, y) ((x) + (y))
            mov rax, ADD(10, 20)
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\n=== EXPRESSION ===")
        println("Expression: ${expr?.text}")

        val functionCall = PsiTreeUtil.findChildOfType(expr, NasmFunctionMacroCall::class.java)
        println("Function call: ${functionCall?.text}")

        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Evaluation result: $result")
    }

    fun testNestedMacroFunctionsDebug() {
        val code = """
            %define DOUBLE(x) ((x) * 2)
            %define QUAD(x) DOUBLE(DOUBLE(x))
            mov rax, QUAD(5)
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\n=== EXPRESSION ===")
        println("Expression: ${expr?.text}")

        val functionCall = PsiTreeUtil.findChildOfType(expr, NasmFunctionMacroCall::class.java)
        println("Function call: ${functionCall?.text}")

        if (functionCall != null) {
            val resolved = functionCall.reference?.resolve() as? NasmPpDefineStmt
            println("Resolved: ${resolved?.name}")
            println("Body: ${resolved?.macroBodyInline?.text}")
        }

        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Evaluation result: $result")
    }

    fun testSingleLevelMacroFunction() {
        val code = """
            %define DOUBLE(x) ((x) * 2)
            mov rax, DOUBLE(10)
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\n=== SINGLE LEVEL ===")
        println("Expression: ${expr?.text}")

        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Evaluation result: $result")
        println("Expected: 20")
    }

    fun testTwoLevelMacroFunction() {
        val code = """
            %define DOUBLE(x) ((x) * 2)
            mov rax, DOUBLE(DOUBLE(5))
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\n=== TWO LEVEL ===")
        println("Expression: ${expr?.text}")

        val result = NasmConstExprEvaluator.evaluate(expr)
        println("Evaluation result: $result")
        println("Expected: 20")
    }
}
