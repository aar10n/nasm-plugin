package dev.agb.nasmplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.preprocessor.PreprocessorStateEvaluator
import dev.agb.nasmplugin.psi.isInInactiveConditionalBranch
import org.junit.Test

/**
 * Tests for the PreprocessorStateEvaluator service.
 * Verifies that conditional branch detection works correctly.
 */
class PreprocessorStateEvaluatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "examples/test_cases"

    @Test
    fun testConditionalWithMacroParameter() {
        // Test that conditions with macro parameters don't mark branches as inactive
        val code = """
            %define DF_VECTOR 8

            %macro isr_stub 1
            %%start:
              %if %1 == DF_VECTOR
                call double_fault_handler
              %else
                push qword 0
                push qword %1
              %endif
            %endmacro
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        // When the condition contains macro parameters (%1), we cannot evaluate it
        // so NO branches should be added (both branches are potentially active)
        assertTrue("No branches should be marked as inactive when condition contains macro parameters",
            branches.isEmpty())
    }

    @Test
    fun testSimpleIfdef() {
        // Test that %ifdef works correctly
        val code = """
            %define DEBUG

            %ifdef DEBUG
              call debug_log
            %else
              nop
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        // DEBUG is defined, so the if branch should be active and else branch inactive
        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when DEBUG is defined", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when DEBUG is defined", elseBranch!!.isActive)
    }

    @Test
    fun testSimpleIfndef() {
        // Test that %ifndef works correctly
        val code = """
            %ifndef RELEASE
              call debug_log
            %else
              nop
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        // RELEASE is not defined, so the if branch should be active and else branch inactive
        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when RELEASE is not defined", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when RELEASE is not defined", elseBranch!!.isActive)
    }

    @Test
    fun testUnevaluableIfCondition() {
        // Test that %if with unevaluable expressions doesn't mark branches as inactive
        val code = """
            %if SOME_CONSTANT == 42
              call special_handler
            %else
              call normal_handler
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        // The condition cannot be evaluated (SOME_CONSTANT is not defined and expression
        // evaluation is not implemented), so NO branches should be marked
        assertTrue("No branches should be marked when condition is unevaluable",
            branches.isEmpty())
    }

    @Test
    fun testNestedConditionals() {
        // Test that nested conditionals work correctly
        val code = """
            %define OUTER
            %define INNER

            %ifdef OUTER
              %ifdef INNER
                call both_defined
              %else
                call only_outer
              %endif
            %else
              call outer_not_defined
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        // Should have at least 2 branches from outer conditional
        assertTrue("Should have at least 2 branches", branches.size >= 2)

        // Check that there are active and inactive branches
        val activeBranches = branches.filter { it.isActive }
        val inactiveBranches = branches.filter { !it.isActive }

        assertTrue("Should have some active branches", activeBranches.isNotEmpty())
        assertTrue("Should have some inactive branches", inactiveBranches.isNotEmpty())
    }
}
