package dev.agb.nasmplugin.references

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests that symbols declared with 'extern' can be resolved when referenced in code.
 * This ensures that "call write" doesn't show "unresolved symbol" when "extern write" is declared.
 */
class ExternSymbolResolutionTest : BasePlatformTestCase() {

    fun testExternSymbolResolvesInCallInstruction() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
extern write
extern printf

section .text
_start:
    call write
    call printf
            """.trimIndent()
        )

        // Find extern declarations
        val externSymbols = psiFile.findExternSymbols()
        assertEquals("Should find 2 extern symbols", 2, externSymbols.size)

        val externNames = externSymbols.mapNotNull { (it as? NasmNamedElement)?.name }
        assertTrue("Should have 'write' extern", "write" in externNames)
        assertTrue("Should have 'printf' extern", "printf" in externNames)

        // Test that extern symbols can be found by name using the utility function
        val writeExtern = psiFile.findExternDefinition("write")
        assertNotNull("'write' extern should be findable", writeExtern)
        assertTrue("'write' extern should be NasmSymbolDecl", writeExtern is NasmSymbolDecl)
        assertEquals("'write' extern name should be 'write'", "write", (writeExtern as? NasmNamedElement)?.name)

        val printfExtern = psiFile.findExternDefinition("printf")
        assertNotNull("'printf' extern should be findable", printfExtern)
        assertTrue("'printf' extern should be NasmSymbolDecl", printfExtern is NasmSymbolDecl)
        assertEquals("'printf' extern name should be 'printf'", "printf", (printfExtern as? NasmNamedElement)?.name)

        // The key test: Check that the annotator won't mark these as "unresolved"
        // by testing the actual resolution through NasmSymbolResolver
        val writeResolved = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve(
            name = "write",
            file = psiFile,
            context = null,
            searchTypes = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
        assertNotNull("'write' should resolve through NasmSymbolResolver", writeResolved)

        val printfResolved = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve(
            name = "printf",
            file = psiFile,
            context = null,
            searchTypes = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
        assertNotNull("'printf' should resolve through NasmSymbolResolver", printfResolved)
    }

    fun testExternWithTypeSyntax() {
        val psiFile = myFixture.configureByText(
            "test2.asm",
            """
extern malloc:function
extern free:function
extern global_var:data

section .text
main:
    mov rdi, 100
    call malloc

    mov rdi, rax
    call free

    mov rax, [global_var]
    ret
            """.trimIndent()
        )

        // Test resolution through NasmSymbolResolver
        assertNotNull("'malloc' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("malloc", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
        assertNotNull("'free' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("free", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
        assertNotNull("'global_var' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("global_var", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
    }

    fun testExternMultipleSymbolsOnOneLine() {
        val psiFile = myFixture.configureByText(
            "test3.asm",
            """
extern malloc, free, realloc

section .text
test:
    call malloc
    call free
    call realloc
            """.trimIndent()
        )

        // Find extern declarations
        val externSymbols = psiFile.findExternSymbols()
        assertEquals("Should find 3 extern symbols", 3, externSymbols.size)

        // Test all three resolve
        assertNotNull("'malloc' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("malloc", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
        assertNotNull("'free' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("free", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
        assertNotNull("'realloc' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("realloc", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
    }

    fun testExternInMemoryOperand() {
        val psiFile = myFixture.configureByText(
            "test4.asm",
            """
extern counter
extern buffer

section .text
main:
    mov rax, [counter]
    lea rdi, [buffer]
    ret
            """.trimIndent()
        )

        // Test resolution through NasmSymbolResolver
        assertNotNull("'counter' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("counter", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
        assertNotNull("'buffer' should resolve",
            dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve("buffer", psiFile, null,
                dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL, true))
    }

    fun testExternWithInclude() {
        // Create an include file with extern declarations
        myFixture.addFileToProject(
            "external.inc",
            """
extern write
extern read
            """.trimIndent()
        )

        val psiFile = myFixture.configureByText(
            "main.asm",
            """
%include "external.inc"

section .text
_start:
    call write
    call read
            """.trimIndent()
        )

        // Test that symbols resolve through NasmSymbolResolver (which searches includes)
        val writeResolved = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve(
            name = "write",
            file = psiFile,
            context = null,
            searchTypes = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
        assertNotNull("'write' should resolve from included file", writeResolved)

        val readResolved = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.resolve(
            name = "read",
            file = psiFile,
            context = null,
            searchTypes = dev.agb.nasmplugin.psi.impl.NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )
        assertNotNull("'read' should resolve from included file", readResolved)
    }
}

// Helper extension functions
private fun PsiElement.findExternSymbols(): List<NasmSymbolDecl> {
    return PsiTreeUtil.findChildrenOfType(this, NasmExternDir::class.java)
        .flatMap { externDir ->
            externDir.symbolList?.symbolDeclList ?: emptyList()
        }
}

private fun PsiElement.findExternDefinition(name: String): NasmSymbolDecl? {
    return findExternSymbols().firstOrNull { (it as? NasmNamedElement)?.name == name }
}
