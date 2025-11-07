package dev.agb.nasmplugin.eval

import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.psi.*

class NestedCallAnalysisTest : BasePlatformTestCase() {

    fun testWhatHappensWithNestedCalls() {
        // Let's trace through what happens step by step
        val code = """
            %define DOUBLE(x) ((x) * 2)
            mov rax, DOUBLE(DOUBLE(5))
        """.trimIndent()

        myFixture.configureByText("test.asm", code)
        val file = myFixture.file

        println("\n=== ORIGINAL FILE ===")
        println("Code: $code")

        // Find the expression DOUBLE(DOUBLE(5))
        val expr = PsiTreeUtil.findChildrenOfType(file, NasmExpression::class.java).lastOrNull()
        println("\nExpression found: ${expr?.text}")

        // Find the outer function call
        val outerCall = PsiTreeUtil.findChildOfType(expr, NasmFunctionMacroCall::class.java)
        println("\nOuter call: ${outerCall?.text}")

        if (outerCall != null) {
            val args = outerCall.macroArgList
            println("Number of arguments: ${args.size}")
            args.forEachIndexed { i, arg ->
                println("  Arg $i: '${arg.text}'")
                println("    Token sequence: ${arg.tokenSequence}")
                println("    Has function call child: ${PsiTreeUtil.findChildOfType(arg, NasmFunctionMacroCall::class.java) != null}")

                // Check what children the argument has
                println("    Children:")
                arg.children.forEach { child ->
                    println("      - ${child.javaClass.simpleName}: '${child.text}'")
                }
            }
        }

        println("\n=== SIMULATING MACRO EXPANSION ===")

        // Step 1: Outer DOUBLE call with argument "DOUBLE(5)"
        val argText = "DOUBLE(5)"
        val body = "((x) * 2)"
        val expanded = body.replace("\\bx\\b".toRegex(), argText)
        println("After substituting x with '$argText' in '$body':")
        println("Result: '$expanded'")

        // Step 2: Re-parse this
        println("\n=== RE-PARSING EXPANDED BODY ===")
        val tempCode = "mov rax, $expanded"
        println("Temp code: $tempCode")

        val tempFile = PsiFileFactory.getInstance(project)
            .createFileFromText("temp.asm", NasmFileType, tempCode)

        val tempExpr = PsiTreeUtil.findChildOfType(tempFile, NasmExpression::class.java)
        println("Temp expression: ${tempExpr?.text}")

        // Explore the structure
        println("\nTemp expression structure:")
        printPsiStructure(tempExpr, 0)

        // Look for function calls in the temp expression
        val functionCalls = PsiTreeUtil.findChildrenOfType(tempExpr, NasmFunctionMacroCall::class.java)
        println("\nFunction calls found in temp expression: ${functionCalls.size}")
        functionCalls.forEachIndexed { i, call ->
            println("  Call $i: ${call.text}")
            println("    Can resolve: ${call.reference?.resolve() != null}")
            println("    Resolved to: ${call.reference?.resolve()}")
        }
    }

    fun testWhatParserSees() {
        val expressions = listOf(
            "5",
            "DOUBLE(5)",
            "(DOUBLE(5))",
            "((DOUBLE(5)) * 2)",
            "DOUBLE(DOUBLE(5))"
        )

        expressions.forEach { exprText ->
            println("\n=== PARSING: $exprText ===")
            val code = "mov rax, $exprText"
            val file = PsiFileFactory.getInstance(project)
                .createFileFromText("test.asm", NasmFileType, code)

            val expr = PsiTreeUtil.findChildOfType(file, NasmExpression::class.java)
            printPsiStructure(expr, 0)

            val functionCalls = PsiTreeUtil.findChildrenOfType(expr, NasmFunctionMacroCall::class.java)
            println("Function calls: ${functionCalls.map { it.text }}")
        }
    }

    private fun printPsiStructure(element: com.intellij.psi.PsiElement?, indent: Int) {
        if (element == null) return

        val prefix = "  ".repeat(indent)
        println("$prefix${element.javaClass.simpleName}: '${element.text.take(30)}'")

        element.children.forEach { child ->
            printPsiStructure(child, indent + 1)
        }
    }
}
