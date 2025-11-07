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

        // The condition cannot be evaluated (SOME_CONSTANT is undefined),
        // so NO branches should be marked
        assertTrue("No branches should be marked when condition is unevaluable",
            branches.isEmpty())
    }

    @Test
    fun testIfWithConstantExpressionTrue() {
        // Test that %if with evaluable true expression marks branches correctly
        val code = """
            %if 1 + 1 == 2
              call correct_branch
            %else
              call wrong_branch
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when condition is true", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when condition is true", elseBranch!!.isActive)
    }

    @Test
    fun testIfWithConstantExpressionFalse() {
        // Test that %if with evaluable false expression marks branches correctly
        val code = """
            %if 2 + 2 == 5
              call wrong_branch
            %else
              call correct_branch
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertFalse("IF branch should be inactive when condition is false", ifBranch!!.isActive)
        assertTrue("ELSE branch should be active when condition is false", elseBranch!!.isActive)
    }

    @Test
    fun testIfWithEquConstant() {
        // Test that %if can evaluate EQU constants
        val code = """
            BUFFER_SIZE equ 1024

            %if BUFFER_SIZE > 512
              call large_buffer_handler
            %else
              call small_buffer_handler
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when BUFFER_SIZE > 512", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when BUFFER_SIZE > 512", elseBranch!!.isActive)
    }

    @Test
    fun testIfWithAssignConstant() {
        // Test that %if can evaluate %assign constants
        val code = """
            %assign COUNTER 10

            %if COUNTER < 20
              call within_limit
            %else
              call exceeded_limit
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when COUNTER < 20", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when COUNTER < 20", elseBranch!!.isActive)
    }

    @Test
    fun testIfWithComplexExpression() {
        // Test that %if can evaluate complex expressions
        val code = """
            BASE equ 100
            OFFSET equ 25

            %if (BASE + OFFSET) * 2 == 250
              call calculation_correct
            %else
              call calculation_wrong
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when (100 + 25) * 2 == 250", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when (100 + 25) * 2 == 250", elseBranch!!.isActive)
    }

    @Test
    fun testIfWithCommandLineMacro() {
        // Test that %if can evaluate command-line defined macros
        val code = """
            %if DEBUG
              call debug_handler
            %else
              call release_handler
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)

        // Add a command-line macro DEBUG=1
        val settings = dev.agb.nasmplugin.settings.NasmProjectSettings.getInstance(project)
        val originalMacros = settings.commandLineMacros
        settings.commandLineMacros = "DEBUG=1"

        try {
            val evaluator = PreprocessorStateEvaluator.getInstance(project)
            val branches = evaluator.evaluateFile(file)

            assertEquals("Should have 2 branches (if and else)", 2, branches.size)

            val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
            val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

            assertNotNull("IF branch should exist", ifBranch)
            assertNotNull("ELSE branch should exist", elseBranch)

            assertTrue("IF branch should be active when DEBUG=1", ifBranch!!.isActive)
            assertFalse("ELSE branch should be inactive when DEBUG=1", elseBranch!!.isActive)
        } finally {
            // Restore original settings
            settings.commandLineMacros = originalMacros
        }
    }

    @Test
    fun testIfWithCommandLineMacroZero() {
        // Test that %if treats 0 as false
        val code = """
            %if DEBUG
              call debug_handler
            %else
              call release_handler
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)

        // Add a command-line macro DEBUG=0
        val settings = dev.agb.nasmplugin.settings.NasmProjectSettings.getInstance(project)
        val originalMacros = settings.commandLineMacros
        settings.commandLineMacros = "DEBUG=0"

        try {
            val evaluator = PreprocessorStateEvaluator.getInstance(project)
            val branches = evaluator.evaluateFile(file)

            assertEquals("Should have 2 branches (if and else)", 2, branches.size)

            val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
            val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

            assertNotNull("IF branch should exist", ifBranch)
            assertNotNull("ELSE branch should exist", elseBranch)

            assertFalse("IF branch should be inactive when DEBUG=0", ifBranch!!.isActive)
            assertTrue("ELSE branch should be active when DEBUG=0", elseBranch!!.isActive)
        } finally {
            // Restore original settings
            settings.commandLineMacros = originalMacros
        }
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

    @Test
    fun testCircularDependencyPrevention() {
        // Test that circular dependencies are prevented when evaluating conditionals
        // This was causing a StackOverflowError when symbol resolution tried to check
        // if a symbol was in an inactive branch while evaluating that branch's condition
        val code = """
            SOME_VALUE equ 42

            %if SOME_VALUE == 42
              call correct_handler
            %else
              call wrong_handler
            %endif
        """.trimIndent()

        val file = myFixture.configureByText("test.asm", code)
        val evaluator = PreprocessorStateEvaluator.getInstance(project)

        // This should not throw StackOverflowError
        val branches = evaluator.evaluateFile(file)

        assertEquals("Should have 2 branches (if and else)", 2, branches.size)

        val ifBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.IF }
        val elseBranch = branches.find { it.branchType == dev.agb.nasmplugin.preprocessor.ConditionalBranch.BranchType.ELSE }

        assertNotNull("IF branch should exist", ifBranch)
        assertNotNull("ELSE branch should exist", elseBranch)

        assertTrue("IF branch should be active when SOME_VALUE == 42", ifBranch!!.isActive)
        assertFalse("ELSE branch should be inactive when SOME_VALUE == 42", elseBranch!!.isActive)
    }
}
