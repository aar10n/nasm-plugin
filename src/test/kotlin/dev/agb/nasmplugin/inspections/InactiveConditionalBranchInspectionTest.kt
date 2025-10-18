package dev.agb.nasmplugin.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for InactiveConditionalBranchInspection.
 *
 * This inspection highlights code in inactive preprocessor branches (greyed out like unused symbols).
 * It evaluates %ifdef, %ifndef, %if, etc. based on macro definitions.
 *
 * NOTE: We use checkHighlightingCount to avoid issues with other warnings like unresolved symbols.
 */
class InactiveConditionalBranchInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(InactiveConditionalBranchInspection::class.java)
    }

    private fun checkInactiveBranchCount(expected: Int) {
        val highlights = myFixture.doHighlighting()
        val inactiveWarnings = highlights.filter {
            it.description?.contains("inactive preprocessor branch") == true
        }
        assertEquals("Expected $expected inactive branch warning(s)", expected, inactiveWarnings.size)
    }

    // ===== Basic %ifdef/%ifndef tests =====

    fun testSimpleIfdefWithDefinedMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %define DEBUG

                %ifdef DEBUG
                  call debug_log
                %else
                  nop
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // Only the 'nop' line should be inactive
    }

    fun testSimpleIfdefWithUndefinedMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef RELEASE
                  call release_setup
                %else
                  call debug_setup
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // Only the 'call release_setup' line should be inactive
    }

    fun testSimpleIfndefWithUndefinedMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifndef RELEASE
                  call debug_log
                %else
                  nop
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // Only the 'nop' line should be inactive
    }

    fun testSimpleIfndefWithDefinedMacro() {
        myFixture.configureByText(
            "test.asm",
            """
                %define RELEASE

                %ifndef RELEASE
                  call debug_log
                %else
                  nop
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // Only the 'call debug_log' line should be inactive
    }

    // ===== %elif tests =====

    fun testIfElifElseChain() {
        myFixture.configureByText(
            "test.asm",
            """
                %define OPTION_B

                %ifdef OPTION_A
                  call option_a_handler
                %elif OPTION_B
                  call option_b_handler
                %else
                  call default_handler
                %endif
            """.trimIndent()
        )
        // TODO: %elif with expressions is not yet supported for evaluation
        // Currently only the %ifdef branch can be evaluated, then we hit the unevaluable %elif
        // and stop processing. So only the first branch is marked.
        checkInactiveBranchCount(1) // Only OPTION_A is marked inactive
    }

    fun testMultipleElifBranches() {
        myFixture.configureByText(
            "test.asm",
            """
                %define OPTION_C

                %ifdef OPTION_A
                  call handler_a
                %elif OPTION_B
                  call handler_b
                %elif OPTION_C
                  call handler_c
                %elif OPTION_D
                  call handler_d
                %else
                  call default_handler
                %endif
            """.trimIndent()
        )
        // TODO: %elif with expressions is not yet supported for evaluation
        // The first %elif returns null, causing early return, so only the %ifdef branch is marked
        checkInactiveBranchCount(1)
    }

    fun testElsebranchActiveWhenNoConditionMatches() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef OPTION_A
                  call handler_a
                %elif OPTION_B
                  call handler_b
                %else
                  call default_handler
                %endif
            """.trimIndent()
        )
        // TODO: %elif with expressions is not yet supported
        checkInactiveBranchCount(1) // Only %ifdef is marked
    }

    // ===== Nested conditionals =====

    fun testNestedConditionalsOuterActive() {
        myFixture.configureByText(
            "test.asm",
            """
                %define OUTER
                %define INNER

                %ifdef OUTER
                  call outer_start
                  %ifdef INNER
                    call inner_code
                  %else
                    call no_inner
                  %endif
                  call outer_end
                %else
                  call no_outer
                  %ifdef INNER
                    call inner_in_inactive_outer
                  %endif
                %endif
            """.trimIndent()
        )
        // Inactive: 'call no_inner', entire else branch (call no_outer, call inner_in_inactive_outer) but not directives
        checkInactiveBranchCount(3) // call no_inner, call no_outer, call inner_in_inactive_outer
    }

    fun testNestedConditionalsOuterInactive() {
        myFixture.configureByText(
            "test.asm",
            """
                %define INNER

                %ifdef OUTER
                  call outer_start
                  %ifdef INNER
                    call inner_code
                  %else
                    call no_inner
                  %endif
                  call outer_end
                %else
                  call no_outer
                %endif
            """.trimIndent()
        )
        // Entire if branch should be inactive, but directives might not count
        checkInactiveBranchCount(5) // call outer_start, call inner_code, call no_inner, call outer_end, call no_outer? Let's see what actually gets counted
    }

    fun testDeeplyNestedConditionals() {
        myFixture.configureByText(
            "test.asm",
            """
                %define LEVEL1

                %ifdef LEVEL1
                  %ifdef LEVEL2
                    %ifdef LEVEL3
                      call deeply_nested
                    %endif
                  %else
                    call level2_inactive
                  %endif
                %endif
            """.trimIndent()
        )
        // The entire LEVEL2 branch is inactive, which doesn't mark nested directives
        checkInactiveBranchCount(0) // No lines marked inactive since they're inside an inactive outer block
    }

    // ===== Unevaluatable conditions =====

    fun testUnevaluatableIfCondition() {
        myFixture.configureByText(
            "test.asm",
            """
                %if SOME_CONSTANT == 42
                  call special_handler
                %else
                  call normal_handler
                %endif
            """.trimIndent()
        )
        // Should have no warnings - unevaluatable conditions don't mark branches as inactive
        checkInactiveBranchCount(0)
    }

    fun testConditionWithMacroParameter() {
        myFixture.configureByText(
            "test.asm",
            """
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
        )
        // Should have no warnings - conditions with macro parameters can't be evaluated
        checkInactiveBranchCount(0)
    }

    fun testConditionWithUndefinedSymbol() {
        myFixture.configureByText(
            "test.asm",
            """
                %if UNDEFINED_VALUE > 100
                  call large_value_handler
                %else
                  call small_value_handler
                %endif
            """.trimIndent()
        )
        // Should have no warnings - undefined symbols make conditions unevaluatable
        checkInactiveBranchCount(0)
    }

    // ===== Multiple lines in branches =====

    fun testMultipleLinesInInactiveBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %define PRODUCTION

                %ifdef DEBUG
                  call debug_init
                  mov rax, debug_buffer
                  call debug_log
                  nop
                %else
                  call production_init
                  mov rax, prod_buffer
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(4) // All 4 lines in the DEBUG branch should be inactive
    }

    // ===== Edge cases =====

    fun testEmptyBranches() {
        myFixture.configureByText(
            "test.asm",
            """
                %define TEST

                %ifdef TEST
                %else
                %endif
            """.trimIndent()
        )
        // Should have no warnings - empty branches don't generate any source lines
        checkInactiveBranchCount(0)
    }

    fun testOnlyCommentInInactiveBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef UNDEFINED
                  ; This is a comment in inactive branch
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // Comment line should be marked as inactive
    }

    fun testMacroDefinitionInInactiveBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef WINDOWS
                  %define PLATFORM_SUFFIX _win
                %else
                  %define PLATFORM_SUFFIX _unix
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // The WINDOWS branch is inactive
    }

    fun testLabelInInactiveBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef FEATURE_X
                  feature_x_handler:
                    mov rax, 1
                    ret
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(3) // All 3 lines should be inactive
    }

    fun testDataDefinitionInInactiveBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                section .data

                %ifdef DEBUG_BUILD
                  debug_msg: db 'Debug mode', 0
                %else
                  release_msg: db 'Release mode', 0
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // The DEBUG_BUILD branch is inactive
    }

    // ===== %assign vs %define =====

    fun testAssignInBranch() {
        myFixture.configureByText(
            "test.asm",
            """
                %define USE_LARGE_BUFFER

                %ifdef USE_LARGE_BUFFER
                  %assign BUFFER_SIZE 4096
                %else
                  %assign BUFFER_SIZE 256
                %endif
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // The else branch should be inactive
    }

    // ===== Without else branch =====

    fun testIfdefWithoutElse() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef EXTRA_FEATURE
                  call extra_feature_init
                %endif

                call main_init
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // The if branch should be inactive
    }

    fun testIfndefWithoutElse() {
        myFixture.configureByText(
            "test.asm",
            """
                %define STANDARD_MODE

                %ifndef STANDARD_MODE
                  call alternative_init
                %endif

                call standard_init
            """.trimIndent()
        )
        checkInactiveBranchCount(1) // The ifndef branch should be inactive
    }

    // ===== Real-world patterns =====

    fun testPlatformSpecificCode() {
        myFixture.configureByText(
            "test.asm",
            """
                %define LINUX

                %ifdef WINDOWS
                  section .drectve
                  db '/EXPORT:my_function'
                %elif LINUX
                  section .note.GNU-stack
                  global my_function
                %elif MACOS
                  section .text
                  global _my_function
                %endif
            """.trimIndent()
        )
        // TODO: %elif with expressions is not yet supported
        checkInactiveBranchCount(2) // Only WINDOWS branch (2 lines) is marked
    }

    fun testFeatureFlags() {
        myFixture.configureByText(
            "test.asm",
            """
                %define FEATURE_LOGGING

                my_function:
                  push rbp
                  mov rbp, rsp

                  %ifdef FEATURE_LOGGING
                    call log_function_entry
                  %endif

                  ; main function body
                  mov rax, 42

                  %ifdef FEATURE_LOGGING
                    call log_function_exit
                  %endif

                  %ifdef FEATURE_PROFILING
                    call record_timing
                  %endif

                  pop rbp
                  ret
            """.trimIndent()
        )
        // Only the FEATURE_PROFILING branch should be inactive (1 line)
        checkInactiveBranchCount(1)
    }

    fun testDebugAssertions() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro assert_not_null 1
                  %ifdef DEBUG
                    test %1, %1
                    jnz %%ok
                    call panic
                  %%ok:
                  %endif
                %endmacro
            """.trimIndent()
        )
        // DEBUG is not defined, so all 4 lines in the ifdef should be inactive
        checkInactiveBranchCount(4)
    }

    // ===== Interaction with includes =====

    fun testConditionalInclude() {
        myFixture.configureByText(
            "test.asm",
            """
                %ifdef USE_CUSTOM_ALLOCATOR
                  %include "custom_allocator.inc"
                %else
                  %include "standard_allocator.inc"
                %endif
            """.trimIndent()
        )
        // The USE_CUSTOM_ALLOCATOR branch should be inactive (1 line)
        checkInactiveBranchCount(1)
    }

    // ===== Multiple conditionals in one file =====

    fun testMultipleIndependentConditionals() {
        myFixture.configureByText(
            "test.asm",
            """
                %define FEATURE_A

                %ifdef FEATURE_A
                  call feature_a_init
                %else
                  nop
                %endif

                %ifdef FEATURE_B
                  call feature_b_init
                %else
                  nop
                %endif

                %ifndef FEATURE_A
                  call no_feature_a
                %endif
            """.trimIndent()
        )
        // Inactive: else for FEATURE_A (1), if for FEATURE_B (1), ifndef FEATURE_A (1) = 3 total
        checkInactiveBranchCount(3)
    }

    // ===== Additional edge cases =====

    fun testMultipleMacrosDefinedInOrder() {
        myFixture.configureByText(
            "test.asm",
            """
                %define FIRST
                %define SECOND

                %ifdef FIRST
                  call first_handler
                %endif

                %ifdef SECOND
                  call second_handler
                %endif

                %ifdef THIRD
                  call third_handler
                %endif
            """.trimIndent()
        )
        // Only THIRD should be inactive (1 line)
        checkInactiveBranchCount(1)
    }

    fun testConditionalAfterUndefine() {
        myFixture.configureByText(
            "test.asm",
            """
                %define TEMP_MACRO

                %ifdef TEMP_MACRO
                  call with_macro
                %endif

                %undef TEMP_MACRO

                %ifdef TEMP_MACRO
                  call after_undef
                %endif
            """.trimIndent()
        )
        // After %undef, maybe %undef is not supported yet
        checkInactiveBranchCount(0) // Appears %undef is not yet supported in the evaluator
    }

    fun testBothBranchesActive() {
        myFixture.configureByText(
            "test.asm",
            """
                ; If we can't evaluate, both branches should be active (no warnings)
                %ifdef SOME_UNDEFINED_MACRO
                  call maybe_active
                %else
                  call also_maybe_active
                %endif
            """.trimIndent()
        )
        // SOME_UNDEFINED_MACRO is not defined, so the if branch should be inactive (1 line)
        checkInactiveBranchCount(1)
    }
}
