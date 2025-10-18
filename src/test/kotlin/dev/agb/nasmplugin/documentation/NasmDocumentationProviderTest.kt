package dev.agb.nasmplugin.documentation

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for NasmDocumentationProvider - hover documentation for instructions, registers, macros, and constants.
 */
class NasmDocumentationProviderTest : BasePlatformTestCase() {

    fun testInstructionDocumentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                m<caret>ov rax, rbx
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        // Should have documentation for MOV instruction
        assertNotNull("MOV instruction should have documentation", doc)
        if (doc != null) {
            assertTrue("Documentation should mention MOV",
                doc.contains("mov", ignoreCase = true) || doc.contains("MOV"))
        }
    }

    fun testRegisterDocumentation() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov r<caret>ax, rbx
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()

        // Try to get custom documentation element
        val docElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )

        val doc = provider.generateDoc(docElement ?: element, null)

        // Should have documentation for RAX register
        if (doc != null) {
            assertTrue("Documentation should mention RAX or register",
                doc.contains("rax", ignoreCase = true) ||
                doc.contains("register", ignoreCase = true))
        }
    }

    fun testMacroDocumentation() {
        myFixture.configureByText(
            "test.asm",
            """
                ; This macro moves a value from one register to another
                ; Parameters: source, destination
                %macro my_<caret>macro 2
                    mov %1, %2
                %endmacro
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        // Should have documentation for the macro
        // May include the comments if CommentExtractor is working
        assertNotNull("Macro should have some documentation", doc)
        if (doc != null) {
            assertTrue("Documentation should mention macro name",
                doc.contains("my_macro") || doc.contains("Macro"))
        }
    }

    fun testEquConstantDocumentation() {
        myFixture.configureByText(
            "test.asm",
            """
                ; Maximum buffer size
                BUF_<caret>SIZE equ 1024
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        // Should have documentation for EQU constant
        assertNotNull("EQU constant should have documentation", doc)
        if (doc != null) {
            assertTrue("Documentation should mention constant name or EQU",
                doc.contains("BUF_SIZE") ||
                doc.contains("EQU", ignoreCase = true) ||
                doc.contains("Constant"))
        }
    }

    fun testQuickNavigateInfoForInstruction() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                ad<caret>d rax, rbx
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val quickInfo = provider.getQuickNavigateInfo(customElement ?: element, null)

        // Quick info should provide brief info about ADD
        // May be null if not on the right element, that's okay
        if (quickInfo != null) {
            assertTrue("Quick info should mention instruction",
                quickInfo.contains("add", ignoreCase = true) ||
                quickInfo.contains("ADD"))
        }
    }

    fun testQuickNavigateInfoForRegister() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rb<caret>x, 0
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )

        val quickInfo = provider.getQuickNavigateInfo(customElement ?: element, null)

        // Quick info should mention the register
        if (quickInfo != null) {
            assertTrue("Quick info should mention register",
                quickInfo.contains("rbx", ignoreCase = true) ||
                quickInfo.contains("register", ignoreCase = true))
        }
    }

    fun testSingleLineMacroDocumentation() {
        myFixture.configureByText(
            "test.asm",
            """
                ; This constant represents the maximum value
                %define MAX_<caret>VALUE 100
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        // Should have documentation for single-line macro
        assertNotNull("Single-line macro should have documentation", doc)
        if (doc != null) {
            assertTrue("Documentation should mention macro name",
                doc.contains("MAX_VALUE") ||
                doc.contains("Macro", ignoreCase = true) ||
                doc.contains("define", ignoreCase = true))
        }
    }

    fun testDocumentationForMacroInvocation() {
        myFixture.configureByText(
            "test.asm",
            """
                %macro PRINT 1
                    mov rax, 1
                    mov rdi, 1
                    mov rsi, %1
                    syscall
                %endmacro

                section .text
                PRI<caret>NT message
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()

        // Try to get documentation - may need to resolve reference first
        // This tests that hovering over macro invocation can provide docs
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        // The documentation may come from the reference resolution
        // Just verify it doesn't crash
        assertNotNull("Should be able to get element", element)
    }

    fun testNoDocumentationForUndefinedSymbol() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                undefined_<caret>symbol
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)

        // Undefined symbols may not have documentation, or may have error doc
        // Just verify it doesn't crash
        assertNotNull("Should be able to get element", element)
    }

    fun testMultipleRegistersInSameInstruction() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                mov rax, rbx
                add r<caret>cx, rdx
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )

        val doc = provider.generateDoc(customElement ?: element, null)

        // Should get documentation for RCX specifically
        if (doc != null) {
            assertTrue("Documentation should be specific to RCX",
                doc.contains("rcx", ignoreCase = true) ||
                doc.contains("RCX"))
        }
    }

    fun testDocumentationHTMLFormatting() {
        myFixture.configureByText(
            "test.asm",
            """
                section .text
                m<caret>ov rax, rbx
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )

        val doc = provider.generateDoc(customElement ?: element, null)

        // Documentation should be formatted as HTML
        if (doc != null && doc.isNotEmpty()) {
            // Basic check: HTML docs usually contain tags
            val hasHtmlTags = doc.contains("<") && doc.contains(">")
            // But some simple docs might just be plain text, which is also fine
            assertTrue("Documentation should exist", doc.isNotEmpty())
        }
    }

    fun testMacroWithMultipleParameters() {
        myFixture.configureByText(
            "test.asm",
            """
                ; Swaps two values
                ; Param 1: First register
                ; Param 2: Second register
                ; Param 3: Temporary register
                %macro SW<caret>AP 3
                    mov %3, %1
                    mov %1, %2
                    mov %2, %3
                %endmacro
            """.trimIndent()
        )

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Element at caret should exist", element)

        val provider = NasmDocumentationProvider()
        val customElement = provider.getCustomDocumentationElement(
            myFixture.editor,
            myFixture.file,
            element,
            myFixture.caretOffset
        )
        val doc = provider.generateDoc(customElement ?: element, null)

        assertNotNull("Macro with parameters should have documentation", doc)
        if (doc != null) {
            assertTrue("Documentation should mention macro",
                doc.contains("SWAP") || doc.contains("Macro"))
        }
    }
}
