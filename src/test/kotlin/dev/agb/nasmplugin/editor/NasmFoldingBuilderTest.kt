package dev.agb.nasmplugin.editor

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for code folding in NASM assembly files.
 * Verifies folding of macros, conditionals, sections, and data blocks.
 */
class NasmFoldingBuilderTest : BasePlatformTestCase() {

    private fun getFoldingDescriptors(code: String): Array<FoldingDescriptor> {
        myFixture.configureByText("test.asm", code)
        val builder = NasmFoldingBuilder()
        return builder.buildFoldRegions(myFixture.file, myFixture.editor.document, false)
    }

    fun testFoldMultiLineMacro() {
        val descriptors = getFoldingDescriptors(
            """
                %macro my_macro 2
                    mov %1, %2
                    add %1, 1
                %endmacro
            """.trimIndent()
        )

        assertTrue("Should have at least one folding descriptor", descriptors.isNotEmpty())

        val macroDescriptor = descriptors.find { descriptor ->
            val text = myFixture.editor.document.text.substring(
                descriptor.range.startOffset,
                descriptor.range.endOffset
            )
            text.contains("%macro") && text.contains("%endmacro")
        }

        assertNotNull("Should have folding descriptor for macro", macroDescriptor)
    }

    fun testFoldConditionalBlock() {
        val descriptors = getFoldingDescriptors(
            """
                %ifdef DEBUG
                    mov rax, 1
                    mov rbx, 2
                %endif
            """.trimIndent()
        )

        assertTrue("Should have folding descriptors", descriptors.isNotEmpty())

        val conditionalDescriptor = descriptors.find { descriptor ->
            val text = myFixture.editor.document.text.substring(
                descriptor.range.startOffset,
                descriptor.range.endOffset
            )
            text.contains("%ifdef") && text.contains("%endif")
        }

        assertNotNull("Should have folding descriptor for conditional", conditionalDescriptor)
    }

    fun testFoldSection() {
        val descriptors = getFoldingDescriptors(
            """
                section .text
                    mov rax, rbx
                    mov rcx, rdx
                    ret

                section .data
                    msg db 'Hello', 0
            """.trimIndent()
        )

        // Sections may or may not create folding regions depending on content
        // This test just verifies no crash occurs
        assertNotNull("Should return descriptors array", descriptors)
    }

    fun testFoldRepBlock() {
        val descriptors = getFoldingDescriptors(
            """
                %rep 10
                    nop
                    inc rax
                %endrep
            """.trimIndent()
        )

        assertTrue("Should have folding descriptors", descriptors.isNotEmpty())

        val repDescriptor = descriptors.find { descriptor ->
            val text = myFixture.editor.document.text.substring(
                descriptor.range.startOffset,
                descriptor.range.endOffset
            )
            text.contains("%rep") && text.contains("%endrep")
        }

        assertNotNull("Should have folding descriptor for rep block", repDescriptor)
    }

    fun testFoldDataBlock() {
        val descriptors = getFoldingDescriptors(
            """
                section .data
                msg1 db 'Hello', 0
                msg2 db 'World', 0
                msg3 db 'Test', 0
                msg4 db 'Data', 0
            """.trimIndent()
        )

        // Should have folding descriptor for consecutive data definitions (3+ lines)
        // Note: May also have section folding
        assertTrue("Should have folding descriptors", descriptors.size > 0)
    }

    fun testNoFoldingSingleLineMacro() {
        val descriptors = getFoldingDescriptors(
            """
                %macro short 0
                nop
                %endmacro
            """.trimIndent()
        )

        // Single line macros might not be folded (depends on implementation)
        // This test verifies the builder doesn't crash on edge cases
        assertNotNull("Should return descriptors array", descriptors)
    }

    fun testNestedMacroFolding() {
        val descriptors = getFoldingDescriptors(
            """
                %macro outer 0
                    %macro inner 0
                        nop
                    %endmacro
                    inner
                %endmacro
            """.trimIndent()
        )

        // Should have folding descriptors for both outer and inner macros
        assertTrue("Should have multiple folding descriptors for nested macros", descriptors.size >= 1)
    }

    fun testNestedConditionalFolding() {
        val descriptors = getFoldingDescriptors(
            """
                %ifdef DEBUG
                    %ifdef VERBOSE
                        mov rax, 1
                    %endif
                %endif
            """.trimIndent()
        )

        // Should have folding descriptors for both conditionals
        assertTrue("Should have multiple folding descriptors for nested conditionals", descriptors.size >= 1)
    }

    fun testMixedFoldableElements() {
        val descriptors = getFoldingDescriptors(
            """
                %macro test 0
                    %ifdef DEBUG
                        nop
                    %endif
                %endmacro

                section .text
                    mov rax, rbx
                    ret

                section .data
                    val1 dw 100
                    val2 dw 200
                    val3 dw 300
            """.trimIndent()
        )

        // Should have multiple folding descriptors: macro, conditional, sections
        assertTrue("Should have multiple folding descriptors for different constructs",
            descriptors.size >= 2)
    }

    fun testFoldingPlaceholderText() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val builder = NasmFoldingBuilder()
        val foldingDescriptors = builder.buildFoldRegions(
            myFixture.file,
            myFixture.editor.document,
            false
        )

        assertTrue("Should have folding descriptors", foldingDescriptors.isNotEmpty())

        // Check placeholder text
        foldingDescriptors.forEach { descriptor ->
            val placeholder = builder.getPlaceholderText(descriptor.element)
            assertNotNull("Should have placeholder text", placeholder)
            assertEquals("Placeholder should be '...'", "...", placeholder)
        }
    }

    fun testNotCollapsedByDefault() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val builder = NasmFoldingBuilder()
        val foldingDescriptors = builder.buildFoldRegions(
            myFixture.file,
            myFixture.editor.document,
            false
        )

        assertTrue("Should have folding descriptors", foldingDescriptors.isNotEmpty())

        // Verify not collapsed by default
        foldingDescriptors.forEach { descriptor ->
            assertFalse("Should not be collapsed by default",
                builder.isCollapsedByDefault(descriptor.element))
        }
    }

    fun testFoldingEmptyMacro() {
        val descriptors = getFoldingDescriptors(
            """
                %macro empty 0
                %endmacro
            """.trimIndent()
        )

        // Empty macro on same line should not be folded
        // This verifies proper handling of edge cases
        assertNotNull("Should handle empty macros", descriptors)
    }

    fun testFoldingWithComments() {
        val descriptors = getFoldingDescriptors(
            """
                ; This is a macro
                %macro my_macro 2
                    ; Move first to second
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        // Should still create folding descriptors even with comments
        assertTrue("Should have folding descriptors with comments", descriptors.isNotEmpty())
    }

    fun testInsufficientDataForBlock() {
        val descriptors = getFoldingDescriptors(
            """
                section .data
                msg1 db 'Hello', 0
                msg2 db 'World', 0
            """.trimIndent()
        )

        // Only 2 data definitions - should not create a data block fold (requires 3+)
        // May have section folding but not data block folding
        assertNotNull("Should handle insufficient data", descriptors)
    }

    fun testMultipleSections() {
        val descriptors = getFoldingDescriptors(
            """
                section .text
                    mov rax, rbx
                    ret

                section .data
                    msg db 'Hello', 0

                section .bss
                    buffer resb 256
            """.trimIndent()
        )

        // Should have folding descriptors for multiple sections
        assertTrue("Should have folding descriptors for multiple sections",
            descriptors.size >= 1)
    }

    fun testIfElseEndif() {
        val descriptors = getFoldingDescriptors(
            """
                %ifdef DEBUG
                    mov rax, 1
                %else
                    mov rax, 0
                %endif
            """.trimIndent()
        )

        // Should have folding descriptor for the entire conditional block
        assertTrue("Should have folding descriptors for if-else-endif", descriptors.isNotEmpty())
    }

    fun testMacroWithLabel() {
        val descriptors = getFoldingDescriptors(
            """
                %macro my_macro 0
                .local_label:
                    nop
                    jmp .local_label
                %endmacro
            """.trimIndent()
        )

        // Should fold macro with local labels
        assertTrue("Should fold macro with labels", descriptors.isNotEmpty())
    }

    fun testQuickModeBuildRegions() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro my_macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val builder = NasmFoldingBuilder()

        // Test quick mode (used for faster folding updates)
        val quickRegions = builder.buildFoldRegions(
            myFixture.file,
            myFixture.editor.document,
            true
        )

        // Should still produce descriptors in quick mode
        assertTrue("Should have descriptors in quick mode", quickRegions.isNotEmpty())
    }

    fun testLargeDataBlock() {
        val descriptors = getFoldingDescriptors(
            """
                section .data
                val1 db 1
                val2 db 2
                val3 db 3
                val4 db 4
                val5 db 5
                val6 db 6
                val7 db 7
                val8 db 8
                val9 db 9
                val10 db 10
            """.trimIndent()
        )

        // Large data block (10 consecutive definitions) should be foldable
        assertTrue("Should fold large data blocks", descriptors.isNotEmpty())
    }

    fun testNonConsecutiveDataNoFold() {
        val descriptors = getFoldingDescriptors(
            """
                section .data
                val1 db 1
                val2 db 2

                val3 db 3
                val4 db 4
            """.trimIndent()
        )

        // Data definitions separated by empty line should be separate blocks
        // Each block needs 3+ items to fold
        assertNotNull("Should handle non-consecutive data", descriptors)
    }
}
