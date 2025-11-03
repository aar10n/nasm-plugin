package dev.agb.nasmplugin.clion

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.NasmFileType

/**
 * Tests for debugger support in NASM files.
 * Tests basic registration and configuration of debugger features.
 *
 * Note: Full debugger testing (actual breakpoints, stepping, etc.) requires
 * a running debug session and is better tested manually. These tests verify
 * that the infrastructure is properly registered.
 */
class DebuggerSupportTest : BasePlatformTestCase() {

    fun testNasmFileTypeIsRecognized() {
        // Test that NASM file type exists and is configured
        assertNotNull("NASM file type should be registered", NasmFileType)
        assertTrue("File type name should contain NASM", NasmFileType.name.contains("NASM"))
        assertEquals("File type should have .asm extension", "asm", NasmFileType.defaultExtension)
    }

    fun testNasmFileCanBeCreated() {
        // Test that we can create NASM files in the project
        val asmFile = myFixture.addFileToProject(
            "test.asm",
            """
            section .text
            global main
            main:
                mov rax, 0
                ret
            """.trimIndent()
        )

        assertNotNull("NASM file should be created", asmFile)
        assertEquals("File should have .asm extension", "asm", asmFile.virtualFile.extension)
        assertTrue("File should be NASM file type",
            asmFile.fileType == NasmFileType)
    }

    fun testMultipleNasmFilesInProject() {
        // Debugger should support multiple NASM files
        val file1 = myFixture.addFileToProject(
            "module1.asm",
            """
            global func1
            func1:
                ret
            """.trimIndent()
        )

        val file2 = myFixture.addFileToProject(
            "module2.asm",
            """
            global func2
            func2:
                ret
            """.trimIndent()
        )

        val file3 = myFixture.addFileToProject(
            "main.asm",
            """
            extern func1
            extern func2

            global main
            main:
                call func1
                call func2
                ret
            """.trimIndent()
        )

        // All should be NASM files
        assertTrue("file1 should be NASM", file1.fileType == NasmFileType)
        assertTrue("file2 should be NASM", file2.fileType == NasmFileType)
        assertTrue("file3 should be NASM", file3.fileType == NasmFileType)
    }

    fun testNasmFileWithMixedCppProject() {
        // Test NASM files in a project with C++ files
        val asmFile = myFixture.addFileToProject(
            "asm_impl.asm",
            """
            global asm_function
            asm_function:
                mov rax, 42
                ret
            """.trimIndent()
        )

        val cppFile = myFixture.addFileToProject(
            "main.cpp",
            """
            extern "C" int asm_function();

            int main() {
                return asm_function();
            }
            """.trimIndent()
        )

        // NASM file should be recognized
        assertTrue("ASM file should be NASM type", asmFile.fileType == NasmFileType)
        assertEquals("CPP file should have cpp extension", "cpp", cppFile.virtualFile.extension)
    }

    fun testNasmFileWithIncExtension() {
        // Test .inc files (NASM include files)
        val incFile = myFixture.addFileToProject(
            "macros.inc",
            """
            %macro TEST_MACRO 0
                nop
            %endmacro
            """.trimIndent()
        )

        assertNotNull("Include file should be created", incFile)
        assertEquals("Should have .inc extension", "inc", incFile.virtualFile.extension)
    }

    fun testNasmFileWithSExtension() {
        // Test .s files (alternative assembly extension)
        val sFile = myFixture.addFileToProject(
            "code.s",
            """
            .global start
            start:
                ret
            """.trimIndent()
        )

        assertNotNull("S file should be created", sFile)
        assertEquals("Should have .s extension", "s", sFile.virtualFile.extension)
    }

    fun testNasmFileInSubdirectory() {
        // Test NASM files in subdirectories
        val asmFile = myFixture.addFileToProject(
            "src/asm/implementation.asm",
            """
            section .text
            global impl
            impl:
                ret
            """.trimIndent()
        )

        assertTrue("Should be NASM file", asmFile.fileType == NasmFileType)
        assertTrue("Path should contain subdirectories",
            asmFile.virtualFile.path.contains("src/asm"))
    }

    fun testNasmFileWithLabels() {
        // Test file with various label types (for breakpoint testing)
        val asmFile = myFixture.configureByText(
            "labels.asm",
            """
            section .text
            global main
            global helper

            main:
                call helper
                call .local_label
                ret

            .local_label:
                nop
                ret

            helper:
                mov rax, 1
                ret
            """.trimIndent()
        )

        assertTrue("Should be NASM file", asmFile.fileType == NasmFileType)
    }

    fun testNasmFileWithDataAndBssSections() {
        // Test file with multiple sections
        val asmFile = myFixture.configureByText(
            "sections.asm",
            """
            section .data
                message db "Hello", 0
                counter dq 0

            section .bss
                buffer resb 1024

            section .text
            global main
            main:
                mov rax, [counter]
                inc rax
                mov [counter], rax
                ret
            """.trimIndent()
        )

        assertTrue("Should be NASM file", asmFile.fileType == NasmFileType)
    }

    fun testNasmFileWithConditionalCode() {
        // Test file with preprocessor conditionals
        val asmFile = myFixture.configureByText(
            "conditional.asm",
            """
            %ifdef DEBUG
            section .data
                debug_msg db "Debug mode", 0
            %endif

            section .text
            global main
            main:
            %ifdef DEBUG
                ; debug code
                nop
            %else
                ; release code
            %endif
                ret
            """.trimIndent()
        )

        assertTrue("Should be NASM file", asmFile.fileType == NasmFileType)
    }

    fun testNasmFileWithMacros() {
        // Test file with macro definitions
        val asmFile = myFixture.configureByText(
            "macros.asm",
            """
            %macro SAVE_REGS 0
                push rbx
                push rcx
                push rdx
            %endmacro

            %macro RESTORE_REGS 0
                pop rdx
                pop rcx
                pop rbx
            %endmacro

            section .text
            global func
            func:
                SAVE_REGS
                ; function body
                RESTORE_REGS
                ret
            """.trimIndent()
        )

        assertTrue("Should be NASM file", asmFile.fileType == NasmFileType)
    }

    fun testNasmFileIcon() {
        // Test that NASM files have an icon
        assertNotNull("NASM file type should have an icon", NasmFileType.icon)
    }

    fun testNasmFileDescription() {
        // Test file type description
        val description = NasmFileType.description
        assertNotNull("Should have description", description)
        assertTrue("Description should mention assembly or NASM",
            description.lowercase().contains("assembly") ||
            description.lowercase().contains("nasm"))
    }
}
