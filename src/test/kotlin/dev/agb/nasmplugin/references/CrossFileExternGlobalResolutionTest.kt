package dev.agb.nasmplugin.references

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests cross-file resolution of extern declarations to global labels.
 *
 * Scenario:
 * - File A declares "extern my_function" and calls it
 * - File B declares "global my_function" and defines my_function:
 * - Cmd+Click on "extern my_function" should navigate to the label in File B
 * - Cmd+Click on "global my_function" should navigate to "my_function:" in the same file
 */
class CrossFileExternGlobalResolutionTest : BasePlatformTestCase() {

    /**
     * Test that an extern declaration resolves to a global label in another file.
     */
    fun testExternResolvesToGlobalLabelInAnotherFile() {
        // File B: Defines and exports my_function
        val fileB = myFixture.addFileToProject(
            "file_b.asm",
            """
global my_function
global shared_data

section .text
my_function:
    push rbp
    mov rbp, rsp
    mov rax, 42
    pop rbp
    ret

section .data
shared_data:
    dq 12345
            """.trimIndent()
        )

        // File A: Imports and uses my_function
        val fileA = myFixture.configureByText(
            "file_a.asm",
            """
extern my_function
extern shared_data

section .text
global _start
_start:
    call my_function
    mov rax, [shared_data]
    ret
            """.trimIndent()
        )

        // Find the extern declaration for my_function in file A
        val externSymbols = fileA.findExternSymbols()
        val myFunctionExtern = externSymbols.firstOrNull {
            (it as? NasmNamedElement)?.name == "my_function"
        }
        assertNotNull("Should find 'my_function' extern declaration", myFunctionExtern)

        // Get the reference from the extern declaration
        val reference = myFunctionExtern?.reference
        assertNotNull("Extern declaration should have a reference", reference)

        // Resolve the reference - it should point to the label definition in file B
        val resolved = reference?.resolve()
        assertNotNull("Extern 'my_function' should resolve to a target", resolved)

        // Verify the resolved element is a label definition
        assertTrue(
            "Resolved element should be a NasmLabelDef, but was ${resolved?.javaClass?.simpleName}",
            resolved is NasmLabelDef
        )

        // CRITICAL: Verify it's in file B (not file A!)
        assertSame(
            "Resolved label must be in file_b.asm (the file we created with addFileToProject)",
            fileB,
            resolved?.containingFile
        )

        assertEquals(
            "Resolved label should be in file_b.asm",
            "file_b.asm",
            resolved?.containingFile?.name
        )

        // Verify the name matches
        assertEquals(
            "Resolved label should be named 'my_function'",
            "my_function",
            (resolved as? NasmNamedElement)?.name
        )

        // Also verify the offset is correct (should be the label definition, not the global declaration)
        val expectedLabelOffset = fileB.text.indexOf("my_function:")
        assertTrue("Label should exist in fileB", expectedLabelOffset >= 0)
        assertEquals(
            "Resolved element should point to the label definition line",
            expectedLabelOffset,
            resolved?.textOffset
        )
    }

    /**
     * Test that a global declaration resolves to the label definition in the same file.
     */
    fun testGlobalResolvesToLabelInSameFile() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
global my_function
global helper

section .text
my_function:
    call helper
    ret

helper:
    mov rax, 1
    ret
            """.trimIndent()
        )

        // Find the global declaration for my_function
        val globalSymbols = psiFile.findGlobalSymbols()
        val myFunctionGlobal = globalSymbols.firstOrNull {
            (it as? NasmNamedElement)?.name == "my_function"
        }
        assertNotNull("Should find 'my_function' global declaration", myFunctionGlobal)

        // Get the reference from the global declaration
        val reference = myFunctionGlobal?.reference
        assertNotNull("Global declaration should have a reference", reference)

        // Resolve the reference - it should point to the label definition in the same file
        val resolved = reference?.resolve()
        assertNotNull("Global 'my_function' should resolve to a target", resolved)

        // Verify the resolved element is a label definition
        assertTrue(
            "Resolved element should be a NasmLabelDef, but was ${resolved?.javaClass?.simpleName}",
            resolved is NasmLabelDef
        )

        // Verify it's in the same file
        assertEquals(
            "Resolved label should be in the same file",
            psiFile.name,
            resolved?.containingFile?.name
        )

        // Verify the name matches
        assertEquals(
            "Resolved label should be named 'my_function'",
            "my_function",
            (resolved as? NasmNamedElement)?.name
        )
    }

    /**
     * Test that extern doesn't resolve to itself (the bug we're fixing).
     */
    fun testExternDoesNotResolveToItself() {
        val fileB = myFixture.addFileToProject(
            "provider.asm",
            """
global some_func

section .text
some_func:
    ret
            """.trimIndent()
        )

        val fileA = myFixture.configureByText(
            "consumer.asm",
            """
extern some_func

section .text
_start:
    call some_func
    ret
            """.trimIndent()
        )

        // Find the extern declaration
        val externSymbols = fileA.findExternSymbols()
        val someFuncExtern = externSymbols.firstOrNull {
            (it as? NasmNamedElement)?.name == "some_func"
        }
        assertNotNull("Should find 'some_func' extern declaration", someFuncExtern)

        // Resolve it
        val resolved = someFuncExtern?.reference?.resolve()
        assertNotNull("Extern should resolve", resolved)

        // The critical assertion: it should NOT resolve to itself
        assertFalse(
            "Extern should NOT resolve to itself (should resolve to the label in provider.asm)",
            resolved === someFuncExtern
        )

        // It should be a different type (NasmLabelDef, not NasmSymbolDecl)
        assertTrue(
            "Should resolve to a label definition (NasmLabelDef), not an extern declaration (NasmSymbolDecl)",
            resolved is NasmLabelDef && resolved !is NasmSymbolDecl
        )
    }

    /**
     * Test multiple files with multiple extern/global pairs.
     */
    fun testMultipleFilesWithMultipleSymbols() {
        // Library file with multiple exports
        val libFile = myFixture.addFileToProject(
            "lib.asm",
            """
global func1
global func2
global data1

section .text
func1:
    ret

func2:
    ret

section .data
data1:
    dq 0
            """.trimIndent()
        )

        // Main file using the library
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
extern func1
extern func2
extern data1

section .text
global _start
_start:
    call func1
    call func2
    mov rax, [data1]
    ret
            """.trimIndent()
        )

        // Test all three extern declarations resolve correctly
        val externSymbols = mainFile.findExternSymbols()

        for (externName in listOf("func1", "func2", "data1")) {
            val extern = externSymbols.firstOrNull {
                (it as? NasmNamedElement)?.name == externName
            }
            assertNotNull("Should find '$externName' extern", extern)

            val resolved = extern?.reference?.resolve()
            assertNotNull("'$externName' extern should resolve", resolved)

            assertEquals(
                "'$externName' should resolve to lib.asm",
                "lib.asm",
                resolved?.containingFile?.name
            )

            assertEquals(
                "'$externName' should resolve to correct name",
                externName,
                (resolved as? NasmNamedElement)?.name
            )
        }
    }

    /**
     * Test that extern with type annotation (e.g., "extern func:function") still resolves.
     */
    fun testExternWithTypeAnnotationResolvesAcrossFiles() {
        val defFile = myFixture.addFileToProject(
            "defs.asm",
            """
global malloc
global errno

section .text
malloc:
    ret

section .bss
errno:
    resq 1
            """.trimIndent()
        )

        val useFile = myFixture.configureByText(
            "use.asm",
            """
extern malloc:function
extern errno:data

section .text
main:
    mov rdi, 100
    call malloc
    mov rax, [errno]
    ret
            """.trimIndent()
        )

        // Test extern with :function annotation
        val mallocExtern = useFile.findExternSymbols().firstOrNull {
            (it as? NasmNamedElement)?.name == "malloc"
        }
        assertNotNull("Should find 'malloc' extern", mallocExtern)

        val mallocResolved = mallocExtern?.reference?.resolve()
        assertNotNull("'malloc' extern should resolve", mallocResolved)
        assertEquals("malloc", (mallocResolved as? NasmNamedElement)?.name)
        assertEquals("defs.asm", mallocResolved?.containingFile?.name)

        // Test extern with :data annotation
        val errnoExtern = useFile.findExternSymbols().firstOrNull {
            (it as? NasmNamedElement)?.name == "errno"
        }
        assertNotNull("Should find 'errno' extern", errnoExtern)

        val errnoResolved = errnoExtern?.reference?.resolve()
        assertNotNull("'errno' extern should resolve", errnoResolved)
        assertEquals("errno", (errnoResolved as? NasmNamedElement)?.name)
        assertEquals("defs.asm", errnoResolved?.containingFile?.name)
    }

    /**
     * Test that when a label is both defined locally AND has a global in another file,
     * the local definition takes precedence.
     */
    fun testLocalDefinitionTakesPrecedenceOverGlobalInOtherFile() {
        val fileB = myFixture.addFileToProject(
            "lib.asm",
            """
global helper

section .text
helper:
    mov rax, 999
    ret
            """.trimIndent()
        )

        val fileA = myFixture.configureByText(
            "main.asm",
            """
extern helper

section .text
; Local override of helper
helper:
    mov rax, 1
    ret

_start:
    call helper
    ret
            """.trimIndent()
        )

        val helperExtern = fileA.findExternSymbols().firstOrNull {
            (it as? NasmNamedElement)?.name == "helper"
        }
        assertNotNull("Should find 'helper' extern", helperExtern)

        val resolved = helperExtern?.reference?.resolve()
        assertNotNull("'helper' extern should resolve", resolved)

        // Should resolve to the LOCAL definition, not the one in lib.asm
        assertEquals(
            "Should resolve to local definition in main.asm",
            "main.asm",
            resolved?.containingFile?.name
        )
    }
}

// Helper extension functions
private fun PsiElement.findExternSymbols(): List<NasmSymbolDecl> {
    return PsiTreeUtil.findChildrenOfType(this, NasmExternDir::class.java)
        .flatMap { externDir ->
            externDir.symbolList?.symbolDeclList ?: emptyList()
        }
}

private fun PsiElement.findGlobalSymbols(): List<NasmSymbolDecl> {
    return PsiTreeUtil.findChildrenOfType(this, NasmGlobalDir::class.java)
        .flatMap { globalDir ->
            globalDir.symbolList?.symbolDeclList ?: emptyList()
        }
}
