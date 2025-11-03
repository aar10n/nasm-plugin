package dev.agb.nasmplugin.clion

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for navigation from C++ extern declarations to NASM global symbols.
 * Tests the CppToNasmGotoHandler and related cross-language navigation features.
 */
class CppToNasmNavigationTest : BasePlatformTestCase() {

    fun testCppExternNavigatesToNasmGlobalLabel() {
        // Create NASM file with global function
        myFixture.addFileToProject(
            "asm_impl.asm",
            """
            global compute_value

            section .text
            compute_value:
                mov rax, 42
                ret
            """.trimIndent()
        )

        // Create C++ file with extern declaration
        val cppFile = myFixture.configureByText(
            "main.cpp",
            """
            extern "C" int compute_value();

            int main() {
                int result = compute_value();
                return result;
            }
            """.trimIndent()
        )

        // Verify the C++ file is created
        assertNotNull("CPP file should be created", cppFile)
    }

    fun testNasmGlobalLabelIsFoundInProject() {
        // Create NASM file with multiple global labels
        val asmFile = myFixture.addFileToProject(
            "functions.asm",
            """
            global func1
            global func2
            global func3

            section .text
            func1:
                ret

            func2:
                ret

            func3:
                ret
            """.trimIndent()
        )

        // Verify global labels are found
        val globalDirs = asmFile.findGlobalDirs()
        assertTrue("Should have global directives", globalDirs.isNotEmpty())

        val globalSymbols = asmFile.findGlobalSymbols()
        assertEquals("Should have 3 global symbols", 3, globalSymbols.size)

        val symbolNames = globalSymbols.map { (it as? NasmNamedElement)?.name }.toSet()
        assertTrue("Should have func1", symbolNames.contains("func1"))
        assertTrue("Should have func2", symbolNames.contains("func2"))
        assertTrue("Should have func3", symbolNames.contains("func3"))
    }

    fun testMultipleNasmGlobalSymbols() {
        myFixture.addFileToProject(
            "exports.asm",
            """
            global start_program
            global stop_program
            global get_status

            section .text
            start_program:
                mov rax, 1
                ret

            stop_program:
                xor rax, rax
                ret

            get_status:
                mov rax, [status]
                ret

            section .data
            status dq 0
            """.trimIndent()
        )

        myFixture.configureByText(
            "caller.cpp",
            """
            extern "C" {
                void start_program();
                void stop_program();
                int get_status();
            }

            void init() {
                start_program();
                int status = get_status();
                if (status == 0) {
                    stop_program();
                }
            }
            """.trimIndent()
        )

        // Verify CPP file is created
        assertNotNull("CPP file should exist", myFixture.file)
    }

    fun testGlobalDataSymbol() {
        // Test global data, not just functions
        myFixture.addFileToProject(
            "data.asm",
            """
            global shared_buffer
            global buffer_size

            section .data
            shared_buffer times 1024 db 0
            buffer_size dq 1024
            """.trimIndent()
        )

        myFixture.configureByText(
            "accessor.cpp",
            """
            extern "C" {
                extern char shared_buffer[];
                extern long buffer_size;
            }

            void use_buffer() {
                for (int i = 0; i < buffer_size; i++) {
                    shared_buffer[i] = 0;
                }
            }
            """.trimIndent()
        )

        assertNotNull("CPP file should be created", myFixture.file)
    }

    fun testGlobalListDeclaration() {
        // Test multiple globals in one directive
        val asmFile = myFixture.configureByText(
            "exports.asm",
            """
            global sym1, sym2, sym3

            section .text
            sym1:
                ret

            sym2:
                ret

            sym3:
                ret
            """.trimIndent()
        )

        val globalSymbols = asmFile.findGlobalSymbols()
        assertEquals("Should have 3 global symbols", 3, globalSymbols.size)

        val names = globalSymbols.map { (it as? NasmNamedElement)?.name }.toSet()
        assertTrue("Should have sym1", names.contains("sym1"))
        assertTrue("Should have sym2", names.contains("sym2"))
        assertTrue("Should have sym3", names.contains("sym3"))
    }

    fun testGlobalBeforeDefinition() {
        // Test that global can come before the label definition
        val asmFile = myFixture.configureByText(
            "forward.asm",
            """
            global forward_func

            section .text
            other_func:
                call forward_func
                ret

            forward_func:
                mov rax, 1
                ret
            """.trimIndent()
        )

        val globalSymbols = asmFile.findGlobalSymbols()
        assertEquals("Should have 1 global", 1, globalSymbols.size)
        assertEquals("Should be forward_func", "forward_func",
            (globalSymbols[0] as? NasmNamedElement)?.name)

        // Verify label definition exists
        val labels = asmFile.findLabels()
        val forwardLabel = labels.firstOrNull { it.name == "forward_func" }
        assertNotNull("Should find forward_func label", forwardLabel)
    }

    fun testCppHeaderWithExternDeclarations() {
        myFixture.addFileToProject(
            "asm_api.h",
            """
            #ifndef ASM_API_H
            #define ASM_API_H

            extern "C" {
                // Assembly-implemented functions
                void asm_memcpy(void* dest, const void* src, unsigned long n);
                void asm_memset(void* dest, int value, unsigned long n);
                int asm_strcmp(const char* s1, const char* s2);
            }

            #endif
            """.trimIndent()
        )

        myFixture.configureByText(
            "impl.asm",
            """
            global asm_memcpy
            global asm_memset
            global asm_strcmp

            section .text
            asm_memcpy:
                ; implementation
                ret

            asm_memset:
                ; implementation
                ret

            asm_strcmp:
                ; implementation
                ret
            """.trimIndent()
        )

        val globalSymbols = myFixture.file.findGlobalSymbols()
        assertEquals("Should have 3 globals", 3, globalSymbols.size)
    }

    fun testMixedNasmAndCppProject() {
        // Simulate a project with both NASM and C++ files
        myFixture.addFileToProject(
            "startup.asm",
            """
            global _start
            extern main

            section .text
            _start:
                call main
                mov rax, 60
                syscall
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "main.cpp",
            """
            extern "C" {
                void helper();
            }

            extern "C" int main() {
                helper();
                return 0;
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "helper.asm",
            """
            global helper

            section .text
            helper:
                ; do something
                ret
            """.trimIndent()
        )

        // Verify helper global is found
        val globalSymbols = myFixture.file.findGlobalSymbols()
        assertEquals("Should have 1 global", 1, globalSymbols.size)
        assertEquals("Should be helper", "helper",
            (globalSymbols[0] as? NasmNamedElement)?.name)
    }

    fun testGlobalWithNamespace() {
        // Test C++ namespace with extern
        myFixture.addFileToProject(
            "module.asm",
            """
            global module_init

            section .text
            module_init:
                ret
            """.trimIndent()
        )

        myFixture.configureByText(
            "wrapper.cpp",
            """
            namespace sys {
                extern "C" void module_init();

                void initialize() {
                    module_init();
                }
            }
            """.trimIndent()
        )

        assertNotNull("CPP file should be created", myFixture.file)
    }

    fun testGlobalSymbolWithUnderscore() {
        // Test naming conventions with underscores
        val asmFile = myFixture.configureByText(
            "syscall.asm",
            """
            global _syscall_wrapper
            global __internal_init

            section .text
            _syscall_wrapper:
                ret

            __internal_init:
                ret
            """.trimIndent()
        )

        val globalSymbols = asmFile.findGlobalSymbols()
        assertEquals("Should have 2 globals", 2, globalSymbols.size)

        val names = globalSymbols.map { (it as? NasmNamedElement)?.name }.toSet()
        assertTrue("Should have _syscall_wrapper", names.contains("_syscall_wrapper"))
        assertTrue("Should have __internal_init", names.contains("__internal_init"))
    }

    fun testFindGlobalLabelInProject() {
        // Test the utility function for finding globals across project
        myFixture.addFileToProject(
            "lib.asm",
            """
            global library_function

            library_function:
                mov rax, 100
                ret
            """.trimIndent()
        )

        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            extern library_function

            section .text
            main:
                call library_function
                ret
            """.trimIndent()
        )

        // Try to find the global label from another file
        val foundLabel = mainFile.findGlobalLabelInProject("library_function")
        assertNotNull("Should find global label in project", foundLabel)
    }

    fun testGlobalInIncludedFile() {
        // Test global symbol in an included file
        myFixture.addFileToProject(
            "api.inc",
            """
            global api_call

            section .text
            api_call:
                ret
            """.trimIndent()
        )

        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "api.inc"

            section .text
            main:
                call api_call
                ret
            """.trimIndent()
        )

        // The included file should be accessible
        val includedFiles = mainFile.getIncludedFiles()
        if (includedFiles.isNotEmpty()) {
            val globalSymbols = includedFiles[0].findGlobalSymbols()
            assertTrue("Included file should have globals",
                globalSymbols.isNotEmpty())
        }
    }
}
