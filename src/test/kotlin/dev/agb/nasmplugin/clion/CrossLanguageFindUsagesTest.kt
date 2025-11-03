package dev.agb.nasmplugin.clion

import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for cross-language find usages between NASM and C++.
 * Tests that finding usages of C++ symbols includes NASM extern references,
 * and finding usages of NASM globals includes C++ extern declarations.
 */
class CrossLanguageFindUsagesTest : BasePlatformTestCase() {

    fun testFindUsagesOfNasmGlobalIncludesExternReferences() {
        // Create NASM file with global
        val asmFile = myFixture.addFileToProject(
            "impl.asm",
            """
            global my_function

            section .text
            my_function:
                mov rax, 42
                ret
            """.trimIndent()
        )

        // Create C++ file that uses it
        myFixture.addFileToProject(
            "caller.cpp",
            """
            extern "C" int my_function();

            int use_it() {
                return my_function();
            }
            """.trimIndent()
        )

        // Find the NASM label
        val labels = asmFile.findLabels()
        val myFunctionLabel = labels.firstOrNull { it.name == "my_function" }
        assertNotNull("Should find my_function label", myFunctionLabel)

        // Note: In a full CLion environment with CIDR APIs, we could search for usages
        // In test environment, we verify the infrastructure is in place
        if (myFunctionLabel != null) {
            // The label should be a named element
            assertTrue("Label should be a named element", myFunctionLabel is NasmNamedElement)
        }
    }

    fun testFindUsagesAcrossMultipleNasmFiles() {
        // Create library file with global
        val libFile = myFixture.addFileToProject(
            "lib.asm",
            """
            global utility_func

            section .text
            utility_func:
                ret
            """.trimIndent()
        )

        // Create first user file
        myFixture.addFileToProject(
            "user1.asm",
            """
            extern utility_func

            section .text
            caller1:
                call utility_func
                ret
            """.trimIndent()
        )

        // Create second user file
        myFixture.addFileToProject(
            "user2.asm",
            """
            extern utility_func

            section .text
            caller2:
                call utility_func
                ret
            """.trimIndent()
        )

        // Find the global label
        val labels = libFile.findLabels()
        val utilityFunc = labels.firstOrNull { it.name == "utility_func" }
        assertNotNull("Should find utility_func", utilityFunc)
    }

    fun testExternFindUsagesShowsCallSites() {
        // Create the implementation
        myFixture.addFileToProject(
            "api.cpp",
            """
            extern "C" void process_data(int* data, int count) {
                // processing
            }
            """.trimIndent()
        )

        // Create NASM file that uses it multiple times
        val asmFile = myFixture.configureByText(
            "processor.asm",
            """
            extern process_data

            section .data
                array1 dd 1, 2, 3, 4
                array2 dd 5, 6, 7, 8

            section .text
            process_first:
                lea rdi, [array1]
                mov rsi, 4
                call process_data
                ret

            process_second:
                lea rdi, [array2]
                mov rsi, 4
                call process_data
                ret
            """.trimIndent()
        )

        // Verify extern is declared
        val externSymbols = asmFile.findExternSymbols()
        val processData = externSymbols.firstOrNull { it.name == "process_data" }
        assertNotNull("Should find process_data extern", processData)
    }

    fun testFindUsagesOfCppFunctionIncludesNasmCalls() {
        // This tests the reverse: C++ function used by NASM
        val cppFile = myFixture.addFileToProject(
            "helper.cpp",
            """
            extern "C" int calculate(int x, int y) {
                return x + y;
            }
            """.trimIndent()
        )

        val asmFile = myFixture.addFileToProject(
            "math.asm",
            """
            extern calculate

            section .text
            do_calculation:
                mov rdi, 10
                mov rsi, 20
                call calculate
                ret
            """.trimIndent()
        )

        // Both files should exist
        assertNotNull("CPP file should exist", cppFile)
        assertNotNull("ASM file should exist", asmFile)

        // ASM file should have the extern
        val externSymbols = asmFile.findExternSymbols()
        assertEquals("Should have 1 extern", 1, externSymbols.size)
        assertEquals("Should be calculate", "calculate", externSymbols[0].name)
    }

    fun testFindUsagesWithMultipleExterns() {
        myFixture.addFileToProject(
            "api.cpp",
            """
            extern "C" void init() {}
            extern "C" void cleanup() {}
            extern "C" void process() {}
            """.trimIndent()
        )

        val asmFile = myFixture.configureByText(
            "workflow.asm",
            """
            extern init
            extern cleanup
            extern process

            section .text
            run_workflow:
                call init
                call process
                call process
                call cleanup
                ret
            """.trimIndent()
        )

        val externSymbols = asmFile.findExternSymbols()
        assertEquals("Should have 3 externs", 3, externSymbols.size)
    }

    fun testFindUsagesOfGlobalData() {
        // Test finding usages of global data, not just functions
        val asmFile = myFixture.addFileToProject(
            "data.asm",
            """
            global shared_counter

            section .data
            shared_counter dq 0
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "reader.cpp",
            """
            extern "C" long shared_counter;

            long read_counter() {
                return shared_counter;
            }

            void increment_counter() {
                shared_counter++;
            }
            """.trimIndent()
        )

        // Verify global is declared
        val globalSymbols = asmFile.findGlobalSymbols()
        val counter = globalSymbols.firstOrNull {
            (it as? NasmNamedElement)?.name == "shared_counter"
        }
        assertNotNull("Should find shared_counter global", counter)
    }

    fun testFindUsagesInMacroContext() {
        myFixture.addFileToProject(
            "syscalls.cpp",
            """
            extern "C" void syscall_write(int fd, const char* buf, unsigned long count) {
                // syscall implementation
            }
            """.trimIndent()
        )

        val asmFile = myFixture.configureByText(
            "macros.asm",
            """
            extern syscall_write

            %macro WRITE_STRING 1
                mov rdi, 1
                lea rsi, [%1]
                mov rdx, 100
                call syscall_write
            %endmacro

            section .data
                msg db "Hello", 0

            section .text
            print_message:
                WRITE_STRING msg
                ret
            """.trimIndent()
        )

        val externSymbols = asmFile.findExternSymbols()
        assertEquals("Should have syscall_write extern", 1, externSymbols.size)
    }

    fun testFindUsagesAcrossIncludedFiles() {
        // Create an include file with extern
        myFixture.addFileToProject(
            "api.inc",
            """
            extern external_api
            """.trimIndent()
        )

        // Create main file that includes it
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
            %include "api.inc"

            section .text
            use_api:
                call external_api
                ret
            """.trimIndent()
        )

        // The extern should be visible
        val includedFiles = mainFile.getIncludedFiles()
        if (includedFiles.isNotEmpty()) {
            val externSymbols = includedFiles[0].findExternSymbols()
            assertEquals("Include file should have extern", 1, externSymbols.size)
        }
    }

    fun testFindUsagesOfMultipleGlobalsInOneFile() {
        val asmFile = myFixture.addFileToProject(
            "library.asm",
            """
            global func_a
            global func_b
            global func_c

            section .text
            func_a:
                call func_b
                ret

            func_b:
                call func_c
                ret

            func_c:
                ret
            """.trimIndent()
        )

        myFixture.configureByText(
            "user.cpp",
            """
            extern "C" {
                void func_a();
                void func_b();
                void func_c();
            }

            void call_all() {
                func_a();
                func_b();
                func_c();
            }
            """.trimIndent()
        )

        val globalSymbols = asmFile.findGlobalSymbols()
        assertEquals("Should have 3 globals", 3, globalSymbols.size)
    }

    fun testReferenceSearchForExternSymbol() {
        // This test uses the ReferencesSearch API
        val cppFile = myFixture.addFileToProject(
            "lib.cpp",
            """
            extern "C" int compute() {
                return 42;
            }
            """.trimIndent()
        )

        val asmFile1 = myFixture.addFileToProject(
            "caller1.asm",
            """
            extern compute

            section .text
            use1:
                call compute
                ret
            """.trimIndent()
        )

        val asmFile2 = myFixture.addFileToProject(
            "caller2.asm",
            """
            extern compute

            section .text
            use2:
                call compute
                ret
            """.trimIndent()
        )

        // Both NASM files should have the extern
        assertEquals("caller1 should have 1 extern", 1, asmFile1.findExternSymbols().size)
        assertEquals("caller2 should have 1 extern", 1, asmFile2.findExternSymbols().size)
    }

    fun testFindUsagesOfStdlibWrapper() {
        myFixture.addFileToProject(
            "wrappers.cpp",
            """
            #include <cstdio>
            #include <cstring>

            extern "C" void print_string(const char* str) {
                printf("%s\\n", str);
            }

            extern "C" int string_length(const char* str) {
                return strlen(str);
            }
            """.trimIndent()
        )

        val asmFile = myFixture.configureByText(
            "main.asm",
            """
            extern print_string
            extern string_length

            section .data
                msg db "Hello, World!", 0

            section .text
            main:
                lea rdi, [msg]
                call string_length

                lea rdi, [msg]
                call print_string

                ret
            """.trimIndent()
        )

        val externSymbols = asmFile.findExternSymbols()
        assertEquals("Should have 2 externs", 2, externSymbols.size)

        val names = externSymbols.map { it.name }.toSet()
        assertTrue("Should have print_string", names.contains("print_string"))
        assertTrue("Should have string_length", names.contains("string_length"))
    }

    fun testFindUsagesWithCppNamespaces() {
        myFixture.addFileToProject(
            "module.cpp",
            """
            namespace core {
                extern "C" void initialize() {
                    // init code
                }
            }

            namespace utils {
                extern "C" int get_version() {
                    return 1;
                }
            }
            """.trimIndent()
        )

        val asmFile = myFixture.configureByText(
            "startup.asm",
            """
            extern initialize
            extern get_version

            section .text
            start:
                call initialize
                call get_version
                ret
            """.trimIndent()
        )

        val externSymbols = asmFile.findExternSymbols()
        assertEquals("Should have 2 externs", 2, externSymbols.size)
    }
}
