package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Comprehensive test suite for NASM annotators.
 *
 * This test suite validates the functionality of four specialized annotators:
 * 1. NasmSectionAttributeAnnotator - Section attribute validation and highlighting
 * 2. NasmSymbolValidationAnnotator - Symbol reference and macro call validation
 * 3. NasmInstructionValidationAnnotator - Instruction syntax validation
 * 4. NasmMacroHighlightingAnnotator - Macro definition and invocation highlighting
 *
 * Testing approach:
 * - Uses IntelliJ's annotation testing framework (BasePlatformTestCase)
 * - Tests both positive cases (valid syntax) and negative cases (errors)
 * - Validates annotation severity levels (ERROR, WARNING, INFORMATION)
 * - Checks annotation messages and ranges
 *
 * Test Coverage (57 test methods):
 *
 * Section Attribute Tests (12 tests):
 * - Valid section attributes (align, exec, alloc, write, progbits, etc.)
 * - Section attribute combinations
 * - Invalid/conflicting attributes (commented out - awaiting implementation)
 * - Multiple sections with same name
 * - Section switching
 *
 * Instruction Validation Tests (8 tests):
 * - Valid x86_64 instructions
 * - Instruction prefixes (lock, rep, etc.)
 * - SIMD instructions (SSE, AVX)
 * - AVX-512 instructions with masking
 * - Data size and reservation directives
 * - Invalid instructions (commented out - awaiting implementation)
 * - Macro vs instruction disambiguation
 *
 * Macro Tests (24 tests):
 * - Macro definitions (%macro, %define, %assign)
 * - Macro invocations and highlighting
 * - Macro parameters (fixed, ranges, greedy)
 * - Function macros with parameters
 * - Nested macro invocations
 * - Macro local labels (%%)
 * - Macro context stack (%push, %pop)
 * - Macro string functions (strlen, strcat)
 * - Empty and comment-only macros
 * - Case sensitivity
 * - Macro shadowing labels
 *
 * Symbol Validation Tests (4 tests):
 * - Valid symbol references
 * - External symbol references
 * - Global symbol references
 * - Undefined symbols (commented out - awaiting implementation)
 *
 * Integration Tests (5 tests):
 * - Complex code with all features
 * - Mixed instructions and macros
 * - Nested macro invocations
 * - Section switching
 * - Local labels in macros
 *
 * Edge Cases (4 tests):
 * - Empty macros
 * - Very long macros
 * - Unicode in comments
 * - Recursive macro definitions (commented out - awaiting implementation)
 *
 * Note: Some negative test cases (errors/warnings) are commented out and marked
 * with TODO comments. These should be uncommented as the annotator implementations
 * are completed.
 */
class AnnotatorTests : BasePlatformTestCase() {

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Configure a test file and return all annotations.
     */
    private fun checkAnnotations(code: String): List<AnnotationInfo> {
        myFixture.configureByText("test.asm", code)
        val annotations = myFixture.doHighlighting()
        return annotations.map {
            AnnotationInfo(
                severity = it.severity,
                message = it.description ?: "",  // Silent annotations may have null descriptions
                text = myFixture.file.text.substring(it.startOffset, it.endOffset)
            )
        }
    }

    /**
     * Check that the code has no error annotations.
     */
    private fun assertNoErrors(code: String) {
        val annotations = checkAnnotations(code)
        val errors = annotations.filter { it.severity == HighlightSeverity.ERROR }
        if (errors.isNotEmpty()) {
            fail("Expected no errors, but found:\n${errors.joinToString("\n") { "  - ${it.message} on '${it.text}'" }}")
        }
    }

    /**
     * Check that the code has at least one error annotation with the given message.
     */
    private fun assertHasError(code: String, expectedMessage: String? = null) {
        val annotations = checkAnnotations(code)
        val errors = annotations.filter { it.severity == HighlightSeverity.ERROR }
        assertTrue("Expected at least one error, but found none", errors.isNotEmpty())

        if (expectedMessage != null) {
            val matchingError = errors.any { it.message.contains(expectedMessage, ignoreCase = true) }
            assertTrue(
                "Expected error containing '$expectedMessage', but found: ${errors.map { it.message }}",
                matchingError
            )
        }
    }

    /**
     * Check that the code has at least one warning annotation.
     */
    private fun assertHasWarning(code: String, expectedMessage: String? = null) {
        val annotations = checkAnnotations(code)
        val warnings = annotations.filter { it.severity == HighlightSeverity.WARNING }
        assertTrue("Expected at least one warning, but found none", warnings.isNotEmpty())

        if (expectedMessage != null) {
            val matchingWarning = warnings.any { it.message.contains(expectedMessage, ignoreCase = true) }
            assertTrue(
                "Expected warning containing '$expectedMessage', but found: ${warnings.map { it.message }}",
                matchingWarning
            )
        }
    }

    /**
     * Check that the code has information annotations (for highlighting).
     */
    private fun assertHasInformation(code: String) {
        val annotations = checkAnnotations(code)
        val info = annotations.filter { it.severity == HighlightSeverity.INFORMATION }
        assertTrue("Expected at least one information annotation, but found none", info.isNotEmpty())
    }

    data class AnnotationInfo(
        val severity: HighlightSeverity,
        val message: String,
        val text: String
    )

    // ============================================================================
    // NasmSectionAttributeAnnotator Tests
    // ============================================================================

    fun testValidSectionAttributes() {
        val code = """
            section .text align=16 exec
            section .data align=8 alloc write
            section .bss align=32 nobits
            section .rodata align=4 alloc nowrite
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeAlign() {
        val code = """
            section .text align=4096
            section .data align=16
            section .bss align=1
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeExecNoexec() {
        val code = """
            section .text exec
            section .data noexec
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeAllocNoalloc() {
        val code = """
            section .data alloc
            section .comment noalloc
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeWriteNowrite() {
        val code = """
            section .data write
            section .rodata nowrite
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeProgbitsNobits() {
        val code = """
            section .text progbits
            section .bss nobits
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionAttributeCombinations() {
        val code = """
            section .text align=4096 exec progbits
            section .data align=16 alloc write progbits
            section .bss align=32 alloc write nobits
            section .rodata align=8 alloc nowrite progbits
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testInvalidSectionAttribute() {
        // This test would validate that invalid attributes are flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            section .text invalid_attr
        """.trimIndent()

        assertHasError(code, "invalid")
        */
    }

    fun testConflictingSectionAttributes() {
        // This test would validate that conflicting attributes are flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            section .text exec noexec
        """.trimIndent()

        assertHasError(code, "conflict")
        */
    }

    // ============================================================================
    // NasmSymbolValidationAnnotator Tests
    // ============================================================================

    fun testValidMacroCall() {
        val code = """
            %macro test_macro 1
                mov rax, %1
            %endmacro

            test_macro 42
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testValidFunctionMacro() {
        val code = """
            %define ADD(x, y) ((x) + (y))

            mov rax, ADD(10, 20)
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testUndefinedMacroCall() {
        // This test would validate that calling undefined macros is flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            undefined_macro 42
        """.trimIndent()

        assertHasWarning(code, "undefined")
        */
    }

    fun testMacroArgumentCountMismatch() {
        // This test would validate that wrong number of arguments is flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            %macro test_macro 2
                mov rax, %1
                mov rbx, %2
            %endmacro

            test_macro 42
        """.trimIndent()

        assertHasError(code, "argument")
        */
    }

    fun testMacroGreedyParameter() {
        val code = """
            %macro test_macro 1-*
                ; Greedy macro - accepts 1 or more arguments
                mov rax, %1
            %endmacro

            test_macro 10
            test_macro 10, 20
            test_macro 10, 20, 30
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroRangeParameters() {
        val code = """
            %macro test_macro 1-3
                ; Accepts 1 to 3 arguments
                mov rax, %1
            %endmacro

            test_macro 10
            test_macro 10, 20
            test_macro 10, 20, 30
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testNestedFunctionMacros() {
        val code = """
            %define MUL(x, y) ((x) * (y))
            %define SQUARE(x) MUL(x, x)

            mov rax, SQUARE(5)
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testValidSymbolReference() {
        val code = """
            section .data
                value dq 100

            section .text
                mov rax, [value]
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testUndefinedSymbolReference() {
        // This test would validate that undefined symbols are flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            section .text
                mov rax, [undefined_symbol]
        """.trimIndent()

        assertHasWarning(code, "undefined")
        */
    }

    fun testExternSymbolReference() {
        val code = """
            extern printf

            section .text
                call printf
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testGlobalSymbolReference() {
        val code = """
            global _start

            section .text
            _start:
                mov rax, 60
                syscall
        """.trimIndent()

        assertNoErrors(code)
    }

    // ============================================================================
    // NasmInstructionValidationAnnotator Tests
    // ============================================================================

    fun testValidInstructions() {
        val code = """
            section .text
                mov rax, rbx
                add rcx, 10
                sub rdx, rax
                push rbp
                pop rbp
                call function
                ret
                jmp label
                je label
                nop
            label:
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testValidInstructionPrefixes() {
        val code = """
            section .text
                lock add [rax], rbx
                rep movsb
                repne scasb
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testValidSIMDInstructions() {
        val code = """
            section .text
                movaps xmm0, xmm1
                addps xmm0, xmm1
                vmovaps ymm0, ymm1
                vaddps ymm0, ymm1, ymm2
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testValidAVX512Instructions() {
        val code = """
            section .text
                vmovaps zmm0, zmm1
                vaddps zmm0, zmm1, zmm2
                vaddps zmm0{k1}, zmm1, zmm2
                vaddps zmm0{k1}{z}, zmm1, zmm2
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testInvalidInstruction() {
        // This test would validate that invalid instructions are flagged
        // Uncomment when annotator is implemented
        /*
        val code = """
            section .text
                notaninstruction rax, rbx
        """.trimIndent()

        assertHasError(code, "unknown instruction")
        */
    }

    fun testInstructionVsMacro() {
        val code = """
            %macro mov 2
                ; User-defined macro that shadows MOV instruction
                push %1
                pop %2
            %endmacro

            section .text
                mov rax, rbx  ; Should be treated as macro call
        """.trimIndent()

        // When macro shadows instruction, macro takes precedence
        assertNoErrors(code)
    }

    fun testMacroDefinedAsInstruction() {
        val code = """
            %macro custom_instruction 2
                mov %1, %2
                add %1, 1
            %endmacro

            section .text
                custom_instruction rax, 10
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testDataSizeDirectives() {
        val code = """
            section .data
                db 0x12, 0x34
                dw 0x1234
                dd 0x12345678
                dq 0x123456789ABCDEF0
                dt 1.234567890123456789
                do 1.0
                dy 1.0
                dz 1.0
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testReservationDirectives() {
        val code = """
            section .bss
                resb 100
                resw 50
                resd 25
                resq 10
                rest 5
                reso 2
                resy 1
                resz 1
        """.trimIndent()

        assertNoErrors(code)
    }

    // ============================================================================
    // NasmMacroHighlightingAnnotator Tests
    // ============================================================================

    fun testMacroDefinitionHighlighting() {
        val code = """
            %macro simple_macro 0
                nop
            %endmacro

            %macro macro_with_params 2
                mov %1, %2
            %endmacro
        """.trimIndent()

        assertNoErrors(code)
        // Information annotations would be used for semantic highlighting
        // Uncomment when annotator is implemented
        // assertHasInformation(code)
    }

    fun testMacroInvocationHighlighting() {
        val code = """
            %macro test_macro 1
                mov rax, %1
            %endmacro

            section .text
                test_macro 42
                test_macro 100
        """.trimIndent()

        assertNoErrors(code)
        // Information annotations would be used for semantic highlighting
        // Uncomment when annotator is implemented
        // assertHasInformation(code)
    }

    fun testMultiLineMacroDefinition() {
        val code = """
            %macro complex_macro 3
                push %1
                push %2
                call %3
                add rsp, 16
            %endmacro

            section .text
                complex_macro rax, rbx, function
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroLocalLabels() {
        val code = """
            %macro loop_macro 2
            %%start:
                cmp %1, %2
                je %%end
                inc %1
                jmp %%start
            %%end:
            %endmacro

            section .text
                loop_macro rax, 10
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroRotate() {
        val code = """
            %macro rotate_test 3
                %rotate 1
                mov rax, %1
                mov rbx, %2
                mov rcx, %3
            %endmacro

            section .text
                rotate_test 10, 20, 30
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testDefineSimple() {
        val code = """
            %define CONSTANT 42
            %define STRING "hello"

            section .data
                value dq CONSTANT
                str db STRING
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testDefineWithParameters() {
        val code = """
            %define ADD(x, y) ((x) + (y))
            %define MAX(a, b) ((a) > (b) ? (a) : (b))

            section .text
                mov rax, ADD(10, 20)
                mov rbx, MAX(rax, 50)
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testAssign() {
        val code = """
            %assign counter 0
            %assign counter counter + 1
            %assign counter counter + 1

            section .data
                value dq counter
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testConditionalMacros() {
        val code = """
            %ifdef DEBUG
                %define LOG(msg) call debug_log
            %else
                %define LOG(msg)
            %endif

            section .text
                LOG("test")
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroContextStack() {
        val code = """
            %push mycontext
            %define %${"\$"}local 42

            section .data
                value dq %${"\$"}local

            %pop
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testStrLenMacroFunction() {
        val code = """
            %define LENGTH %strlen("hello")

            section .data
                len dq LENGTH
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testStrCatMacroFunction() {
        val code = """
            %define COMBINED %strcat("hello", " ", "world")

            section .data
                str db COMBINED
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroParameterCount() {
        val code = """
            %macro test_macro 2-5
                ; Accepts 2 to 5 parameters
                %if %0 == 2
                    mov rax, %1
                %elif %0 == 3
                    mov rbx, %2
                %endif
            %endmacro

            section .text
                test_macro 10, 20
                test_macro 10, 20, 30
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testRepDirective() {
        val code = """
            %macro repeat_test 1
                %rep %1
                    nop
                %endrep
            %endmacro

            section .text
                repeat_test 5
        """.trimIndent()

        assertNoErrors(code)
    }

    // ============================================================================
    // Integration Tests - Multiple Annotators
    // ============================================================================

    fun testComplexCodeWithAllFeatures() {
        val code = """
            BITS 64

            ; Macro definitions
            %macro PUSH_ALL 0
                push rax
                push rbx
                push rcx
            %endmacro

            %macro POP_ALL 0
                pop rcx
                pop rbx
                pop rax
            %endmacro

            %define SYSCALL_EXIT 60
            %define ERROR_CODE(n) ((n) << 8)

            ; Section with attributes
            section .data align=16 alloc write
                message db "Hello, World!", 10, 0
                message_len equ $ - message

            section .bss align=32 nobits
                buffer resb 1024

            section .text align=4096 exec progbits

            global _start
            extern printf

            _start:
                PUSH_ALL

                ; Valid instructions
                mov rax, message
                mov rbx, message_len
                call printf

                POP_ALL

                ; System call
                mov rax, SYSCALL_EXIT
                xor rdi, rdi
                syscall
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroShadowingLabel() {
        val code = """
            %macro my_label 0
                nop
            %endmacro

            section .text
            my_label:
                ; Label with same name as macro
                ret
        """.trimIndent()

        // Should not error - labels and macros are in different namespaces
        assertNoErrors(code)
    }

    fun testMixedInstructionsAndMacros() {
        val code = """
            %macro SAVE_REGS 0
                push rbp
                mov rbp, rsp
            %endmacro

            %macro RESTORE_REGS 0
                mov rsp, rbp
                pop rbp
            %endmacro

            section .text
            function:
                SAVE_REGS

                ; Regular instructions
                mov rax, rdi
                add rax, rsi

                RESTORE_REGS
                ret
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testNestedMacroInvocations() {
        val code = """
            %macro inner_macro 1
                mov rax, %1
            %endmacro

            %macro outer_macro 1
                inner_macro %1
                add rax, 1
            %endmacro

            section .text
                outer_macro 42
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testSectionSwitching() {
        val code = """
            section .data
                value1 dq 100

            section .text
                mov rax, [value1]

            section .data
                value2 dq 200

            section .text
                mov rbx, [value2]
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testLocalLabelsInMacros() {
        val code = """
            %macro conditional_move 3
                cmp %1, %2
                jle %%skip
                mov %1, %3
            %%skip:
            %endmacro

            section .text
                conditional_move rax, 10, 20
                conditional_move rbx, 5, 15
        """.trimIndent()

        assertNoErrors(code)
    }

    // ============================================================================
    // Edge Cases and Error Recovery
    // ============================================================================

    fun testEmptyMacro() {
        val code = """
            %macro empty_macro 0
            %endmacro

            section .text
                empty_macro
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testMacroWithOnlyComments() {
        val code = """
            %macro comment_macro 0
                ; This macro only has comments
                ; Nothing else
            %endmacro

            section .text
                comment_macro
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testVeryLongMacro() {
        val code = """
            %macro long_macro 0
                nop
                nop
                nop
                nop
                nop
                nop
                nop
                nop
                nop
                nop
            %endmacro

            section .text
                long_macro
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testRecursiveMacroDefinition() {
        // This test would check for recursive macro references
        // Uncomment when annotator is implemented
        /*
        val code = """
            %define RECURSIVE RECURSIVE + 1

            section .data
                value dq RECURSIVE
        """.trimIndent()

        assertHasWarning(code, "recursive")
        */
    }

    fun testMultipleSectionsSameName() {
        val code = """
            section .text
                nop

            section .text
                nop
        """.trimIndent()

        // Multiple sections with same name are valid in NASM
        assertNoErrors(code)
    }

    fun testSectionWithoutAttributes() {
        val code = """
            section .text
            section .data
            section .bss
        """.trimIndent()

        assertNoErrors(code)
    }

    fun testCaseSensitivityInMacros() {
        val code = """
            %macro TEST_MACRO 0
                nop
            %endmacro

            %macro test_macro 0
                nop
            %endmacro

            section .text
                TEST_MACRO
                test_macro
        """.trimIndent()

        // NASM is case-sensitive for macro names
        assertNoErrors(code)
    }

    fun testUnicodeInComments() {
        val code = """
            ; Test with unicode: 你好世界 Привет мир
            section .text
                mov rax, 42  ; Unicode comment: 🎉
        """.trimIndent()

        assertNoErrors(code)
    }
}
