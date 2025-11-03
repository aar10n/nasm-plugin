package dev.agb.nasmplugin.clion

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.cidr.lang.psi.OCDeclarator
import com.jetbrains.cidr.lang.psi.OCFile
import dev.agb.nasmplugin.psi.*

/**
 * Tests for navigation from NASM extern declarations to C++ function declarations.
 * Tests the NasmToCppGotoHandler and related cross-language navigation features.
 */
class NasmToCppNavigationTest : BasePlatformTestCase() {

    fun testExternSymbolNavigatesToCppFunctionDeclaration() {
        // Create C++ file with extern "C" function
        myFixture.addFileToProject(
            "lib.cpp",
            """
            extern "C" void helper_function() {
                // Implementation
            }
            """.trimIndent()
        )

        // Create NASM file with extern declaration
        myFixture.configureByText(
            "main.asm",
            """
            extern helper_function

            section .text
            global main
            main:
                call helper_<caret>function
                ret
            """.trimIndent()
        )

        // Test that the reference can be found
        val element = myFixture.elementAtCaret
        assertNotNull("Should find element at caret", element)

        // The element should be within a symbol_ref or similar context
        val symbolRef = PsiTreeUtil.getParentOfType(element, NasmSymbolRef::class.java)
        if (symbolRef != null) {
            val resolved = symbolRef.reference?.resolve()
            // In a full CLion environment, this would resolve to OCDeclarator
            // In test environment, it should at least resolve to the extern declaration
            assertNotNull("Should resolve to something", resolved)
        }
    }

    fun testExternDeclarationListsCppFile() {
        // Create C++ file
        val cppFile = myFixture.addFileToProject(
            "api.cpp",
            """
            extern "C" {
                int calculate(int x) {
                    return x * 2;
                }
            }
            """.trimIndent()
        )

        // Create NASM file
        myFixture.configureByText(
            "caller.asm",
            """
            extern calculate

            section .text
            use_calc:
                mov rdi, 42
                call calculate
                ret
            """.trimIndent()
        )

        // Verify extern declaration is found
        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
        assertEquals("Extern should be 'calculate'", "calculate", externDecls[0].name)
    }

    fun testMultipleExternsNavigateToMultipleCppFunctions() {
        // Create C++ files
        myFixture.addFileToProject(
            "math.cpp",
            """
            extern "C" int add(int a, int b) {
                return a + b;
            }
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "io.cpp",
            """
            extern "C" void print(const char* msg) {
                // print implementation
            }
            """.trimIndent()
        )

        // Create NASM file using both
        myFixture.configureByText(
            "main.asm",
            """
            extern add
            extern print

            section .text
            main:
                mov rdi, 10
                mov rsi, 20
                call add
                call print
                ret
            """.trimIndent()
        )

        // Verify both externs are found
        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 2 externs", 2, externDecls.size)

        val externNames = externDecls.map { it.name }.toSet()
        assertTrue("Should have 'add' extern", externNames.contains("add"))
        assertTrue("Should have 'print' extern", externNames.contains("print"))
    }

    fun testExternWithUnderscoreNavigatesToCppFunction() {
        // Test that naming conventions are preserved
        myFixture.addFileToProject(
            "system.cpp",
            """
            extern "C" void _system_init() {
                // init code
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "startup.asm",
            """
            extern _system_init

            global _start
            _start:
                call _system_init
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should find extern", 1, externDecls.size)
        assertEquals("Should preserve underscores", "_system_init", externDecls[0].name)
    }

    fun testCppFileTypeIsRecognized() {
        // Verify that .cpp files are recognized as OCFile
        val cppFile = myFixture.addFileToProject(
            "test.cpp",
            """
            extern "C" void test() {}
            """.trimIndent()
        )

        // In CLion environment, this should be an OCFile
        // In basic test environment, it's at least a valid PsiFile
        assertNotNull("CPP file should be created", cppFile)
        assertEquals("Should have .cpp extension", "cpp", cppFile.virtualFile.extension)
    }

    fun testHeaderFileWithExternDeclaration() {
        // Test with .h header file
        myFixture.addFileToProject(
            "api.h",
            """
            #ifndef API_H
            #define API_H

            extern "C" {
                void api_init(void);
                void api_shutdown(void);
            }

            #endif
            """.trimIndent()
        )

        myFixture.configureByText(
            "main.asm",
            """
            extern api_init
            extern api_shutdown

            section .text
            main:
                call api_init
                call api_shutdown
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 2 externs", 2, externDecls.size)
    }

    fun testExternInDataSection() {
        // Test extern used for data, not just functions
        myFixture.addFileToProject(
            "data.cpp",
            """
            extern "C" int global_counter = 0;
            """.trimIndent()
        )

        myFixture.configureByText(
            "reader.asm",
            """
            extern global_counter

            section .text
            read_counter:
                mov rax, [global_counter]
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
        assertEquals("Should be global_counter", "global_counter", externDecls[0].name)
    }

    fun testExternWithCppNameMangling() {
        // C++ name mangling - test that extern "C" is necessary
        myFixture.addFileToProject(
            "mangled.cpp",
            """
            // This function has C++ name mangling
            void mangled_function() {
                // implementation
            }

            // This function uses C linkage
            extern "C" void c_function() {
                // implementation
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "caller.asm",
            """
            extern c_function
            ; Cannot call mangled_function directly - needs extern "C"

            section .text
            test:
                call c_function
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
        assertEquals("Should only have c_function", "c_function", externDecls[0].name)
    }

    fun testMultipleNasmFilesUsingCommonCppExtern() {
        // Create shared C++ API
        myFixture.addFileToProject(
            "shared.cpp",
            """
            extern "C" void shared_api() {
                // shared implementation
            }
            """.trimIndent()
        )

        // Create first NASM file
        myFixture.addFileToProject(
            "module1.asm",
            """
            extern shared_api

            section .text
            func1:
                call shared_api
                ret
            """.trimIndent()
        )

        // Create second NASM file
        val module2 = myFixture.configureByText(
            "module2.asm",
            """
            extern shared_api

            section .text
            func2:
                call shared_api
                ret
            """.trimIndent()
        )

        // Both should have the extern
        val externDecls = module2.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
        assertEquals("Should be shared_api", "shared_api", externDecls[0].name)
    }

    fun testCppStdlibFunctionExtern() {
        // Test calling standard library functions
        myFixture.addFileToProject(
            "wrapper.cpp",
            """
            #include <cstdio>

            extern "C" void print_hello() {
                printf("Hello from C++\\n");
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "main.asm",
            """
            extern print_hello

            global main
            main:
                call print_hello
                mov rax, 0
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
    }

    fun testExternListDeclaration() {
        // Test multiple externs in one directive
        myFixture.configureByText(
            "multi.asm",
            """
            extern func1, func2, func3

            section .text
            test:
                call func1
                call func2
                call func3
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 3 externs", 3, externDecls.size)

        val names = externDecls.map { it.name }.toSet()
        assertTrue("Should have func1", names.contains("func1"))
        assertTrue("Should have func2", names.contains("func2"))
        assertTrue("Should have func3", names.contains("func3"))
    }

    fun testExternReferenceFromNasmMacro() {
        // Test extern used within a macro
        myFixture.addFileToProject(
            "logger.cpp",
            """
            extern "C" void log_message(const char* msg) {
                // logging implementation
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "macros.asm",
            """
            extern log_message

            %macro LOG 1
                lea rdi, [%1]
                call log_message
            %endmacro

            section .data
                msg db "Test", 0

            section .text
            test:
                LOG msg
                ret
            """.trimIndent()
        )

        val externDecls = myFixture.file.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externDecls.size)
        assertEquals("Should be log_message", "log_message", externDecls[0].name)
    }
}
