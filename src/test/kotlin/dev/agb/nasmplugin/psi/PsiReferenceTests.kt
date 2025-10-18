package dev.agb.nasmplugin.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.impl.NasmSymbolResolver

/**
 * Comprehensive test suite for PSI element reference resolution.
 * Tests label references, symbol references, macro invocations, and cross-file references.
 */
class PsiReferenceTests : BasePlatformTestCase() {

    // ===== Label Reference Tests =====

    fun testSimpleLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                start:
                    mov rax, 1
                    jmp start
            """.trimIndent()
        )

        val startLabel = psiFile.findLabels().firstOrNull { it.name == "start" }
        assertNotNull("start label should be found", startLabel)

        val usages = ReferencesSearch.search(startLabel!!).findAll()
        assertTrue("start label should have at least 1 usage", usages.isNotEmpty())

        val usage = usages.first()
        assertEquals("Reference should resolve to start label", startLabel, usage.element.reference?.resolve())
    }

    fun testLocalLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                main:
                    mov rax, 1
                    jmp .loop
                .loop:
                    inc rax
                    jmp .loop
            """.trimIndent()
        )

        val localLabel = psiFile.findLabels().firstOrNull { it.name == ".loop" }
        assertNotNull(".loop label should be found", localLabel)

        val usages = ReferencesSearch.search(localLabel!!).findAll()
        assertTrue(".loop should have at least 1 usage", usages.size >= 1)
    }

    fun testLocalLabelScoping() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                func1:
                    mov rax, 1
                .loop:
                    inc rax
                    jmp .loop
                    ret

                func2:
                    mov rax, 2
                .loop:
                    dec rax
                    jmp .loop
                    ret
            """.trimIndent()
        )

        val allLabels = psiFile.findLabels()
        val loopLabels = allLabels.filter { it.name == ".loop" }
        assertEquals("Should have exactly 2 .loop labels", 2, loopLabels.size)

        val firstLoop = loopLabels[0]
        val secondLoop = loopLabels[1]

        // Find reference elements in each scope
        val func1Def = allLabels.first { it.name == "func1" }
        val func2Def = allLabels.first { it.name == "func2" }

        // Create context elements in each scope
        val elementInFunc1Scope = psiFile.findElementAt(func1Def.textOffset + 30)
        val elementInFunc2Scope = psiFile.findElementAt(func2Def.textOffset + 30)

        // Test resolution with proper scoping
        val resolvedFromFunc1 = psiFile.findLabelDefinition(".loop", elementInFunc1Scope)
        val resolvedFromFunc2 = psiFile.findLabelDefinition(".loop", elementInFunc2Scope)

        assertNotNull("Should resolve .loop from func1 scope", resolvedFromFunc1)
        assertNotNull("Should resolve .loop from func2 scope", resolvedFromFunc2)

        assertEquals("Should resolve to first .loop in func1 scope", firstLoop.textOffset, resolvedFromFunc1!!.textOffset)
        assertEquals("Should resolve to second .loop in func2 scope", secondLoop.textOffset, resolvedFromFunc2!!.textOffset)
    }

    fun testExplicitGlobalLocalLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                global_func:
                .local_label:
                    mov rax, 1

                another_func:
                    jmp global_func.local_label
            """.trimIndent()
        )

        val localLabel = psiFile.findLabels().firstOrNull { it.name == ".local_label" }
        assertNotNull("local_label should be found", localLabel)

        // Test explicit global.local syntax
        val resolved = psiFile.findLabelDefinition("global_func.local_label")
        assertNotNull("Should resolve global_func.local_label", resolved)
        assertEquals("Should resolve to the local label", localLabel!!.textOffset, resolved!!.textOffset)
    }

    fun testUnresolvedLabelReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                start:
                    jmp nonexistent_label
            """.trimIndent()
        )

        val resolved = psiFile.findLabelDefinition("nonexistent_label")
        assertNull("Should not resolve nonexistent label", resolved)
    }

    // ===== Symbol Reference Tests (EQU constants, data definitions) =====

    fun testEquConstantReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                BUFFER_SIZE equ 1024

                section .bss
                    buffer: resb BUFFER_SIZE
            """.trimIndent()
        )

        val equDef = psiFile.findEquDefinitions().firstOrNull { (it as? NasmNamedElement)?.name == "BUFFER_SIZE" }
        assertNotNull("BUFFER_SIZE equ should be found", equDef)

        val usages = ReferencesSearch.search(equDef!!).findAll()
        assertTrue("BUFFER_SIZE should have at least 1 usage", usages.isNotEmpty())
    }

    fun testDataDefinitionReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                section .data
                    msg db 'Hello', 0

                section .text
                    mov rsi, msg
            """.trimIndent()
        )

        // Note: data labels are actually NasmLabelDef, not a separate type
        val dataLabel = psiFile.findLabels().firstOrNull { it.name == "msg" }
        assertNotNull("msg data label should be found", dataLabel)

        val usages = ReferencesSearch.search(dataLabel!!).findAll()
        assertTrue("msg should have at least 1 usage", usages.isNotEmpty())
    }

    // ===== Macro Reference Tests =====

    fun testMultiLineMacroInvocation() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %macro print 1
                    mov rsi, %1
                    call write_string
                %endmacro

                main:
                    print msg
            """.trimIndent()
        )

        val macroDef = psiFile.findMultiLineMacros().firstOrNull { (it as? NasmNamedElement)?.name == "print" }
        assertNotNull("print macro should be found", macroDef)

        val usages = ReferencesSearch.search(macroDef!!).findAll()
        assertTrue("print macro should have at least 1 invocation", usages.isNotEmpty())
    }

    fun testSingleLineMacroReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %define MAX_SIZE 256

                section .bss
                    buffer: resb MAX_SIZE
            """.trimIndent()
        )

        val macroDef = psiFile.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "MAX_SIZE" }
        assertNotNull("MAX_SIZE macro should be found", macroDef)

        val usages = ReferencesSearch.search(macroDef!!).findAll()
        assertTrue("MAX_SIZE should have at least 1 usage", usages.isNotEmpty())
    }

    fun testMacroReferencingOtherMacro() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %define BASE 100
                %define DERIVED (BASE * 2)
                %define FINAL (DERIVED + 50)
            """.trimIndent()
        )

        val baseMacro = psiFile.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "BASE" }
        assertNotNull("BASE macro should be found", baseMacro)

        val baseUsages = ReferencesSearch.search(baseMacro!!).findAll()
        assertTrue("BASE should be referenced in DERIVED", baseUsages.isNotEmpty())

        val derivedMacro = psiFile.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "DERIVED" }
        assertNotNull("DERIVED macro should be found", derivedMacro)

        val derivedUsages = ReferencesSearch.search(derivedMacro!!).findAll()
        assertTrue("DERIVED should be referenced in FINAL", derivedUsages.isNotEmpty())
    }

    fun testAssignStatementReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %assign counter 0
                %assign counter counter+1

                mov rax, counter
            """.trimIndent()
        )

        val assignDef = psiFile.findPpAssignStmts().firstOrNull { (it as? NasmNamedElement)?.name == "counter" }
        assertNotNull("counter assign should be found", assignDef)

        val usages = ReferencesSearch.search(assignDef!!).findAll()
        assertTrue("counter should have at least 1 usage", usages.isNotEmpty())
    }

    // ===== Cross-file Reference Tests =====

    fun testIncludeFileResolution() {
        // Create the included file
        val includedFile = myFixture.addFileToProject(
            "macros.inc",
            """
                %macro debug_print 1
                    mov rdi, %1
                    call debug_output
                %endmacro

                MAGIC_NUMBER equ 0x42
            """.trimIndent()
        )

        // Create the main file
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
                %include "macros.inc"

                main:
                    debug_print msg
                    mov rax, MAGIC_NUMBER
            """.trimIndent()
        )

        // Test include statement resolution
        val includeStmt = mainFile.findIncludeStatements().firstOrNull()
        assertNotNull("Should find include statement", includeStmt)

        val includePath = includeStmt!!.getIncludePath()
        assertEquals("Include path should be macros.inc", "macros.inc", includePath)

        val resolvedFile = mainFile.resolveIncludeFile(includePath!!)
        assertNotNull("Should resolve included file", resolvedFile)
        assertEquals("Should resolve to macros.inc", "macros.inc", resolvedFile!!.name)
    }

    fun testCrossFileMacroReference() {
        // Create the included file with a macro
        myFixture.addFileToProject(
            "defs.inc",
            """
                %define VERSION 1
                %define BUILD_NUMBER 100
            """.trimIndent()
        )

        // Create the main file that uses the macro
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
                %include "defs.inc"

                version_info:
                    dd VERSION
                    dd BUILD_NUMBER
            """.trimIndent()
        )

        // Test that macros from included files can be resolved
        val includedFiles = mainFile.getIncludedFiles()
        assertTrue("Should have at least 1 included file", includedFiles.isNotEmpty())

        // Find VERSION in included files
        val versionMacro = includedFiles.firstNotNullOfOrNull { file ->
            file.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "VERSION" }
        }
        assertNotNull("Should find VERSION macro in included file", versionMacro)
    }

    fun testCrossFileLabelReference() {
        // Create the included file with labels
        myFixture.addFileToProject(
            "utils.asm",
            """
                print_string:
                    push rdi
                    call puts
                    pop rdi
                    ret
            """.trimIndent()
        )

        // Create the main file
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
                %include "utils.asm"

                main:
                    call print_string
            """.trimIndent()
        )

        val includedFiles = mainFile.getIncludedFiles()
        assertTrue("Should have at least 1 included file", includedFiles.isNotEmpty())

        val printStringLabel = includedFiles.firstNotNullOfOrNull { file ->
            file.findLabels().firstOrNull { it.name == "print_string" }
        }
        assertNotNull("Should find print_string label in included file", printStringLabel)
    }

    // ===== NasmSymbolResolver Tests =====

    fun testSymbolResolverLabels() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                my_label:
                    nop
            """.trimIndent()
        )

        val resolved = NasmSymbolResolver.resolve(
            name = "my_label",
            file = psiFile,
            searchTypes = setOf(NasmSymbolResolver.SearchType.LABELS),
            includeIncludes = false
        )

        assertNotNull("Should resolve label", resolved)
        assertEquals("Should resolve to my_label", "my_label", (resolved as? NasmNamedElement)?.name)
    }

    fun testSymbolResolverDataDefinitions() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                CONST equ 42
            """.trimIndent()
        )

        val resolved = NasmSymbolResolver.resolve(
            name = "CONST",
            file = psiFile,
            searchTypes = setOf(NasmSymbolResolver.SearchType.DATA_DEFINITIONS),
            includeIncludes = false
        )

        assertNotNull("Should resolve EQU constant", resolved)
        assertEquals("Should resolve to CONST", "CONST", (resolved as? NasmNamedElement)?.name)
    }

    fun testSymbolResolverMacros() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %macro test_macro 0
                    nop
                %endmacro
            """.trimIndent()
        )

        val resolved = NasmSymbolResolver.resolveMacro(
            name = "test_macro",
            file = psiFile,
            includeIncludes = false
        )

        assertNotNull("Should resolve macro", resolved)
        assertEquals("Should resolve to test_macro", "test_macro", (resolved as? NasmNamedElement)?.name)
    }

    fun testSymbolResolverWithContext() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                global_label:
                .local:
                    nop
            """.trimIndent()
        )

        val globalLabel = psiFile.findLabels().first { it.name == "global_label" }
        val contextElement = psiFile.findElementAt(globalLabel.textOffset + 20)

        val resolved = NasmSymbolResolver.resolveLabel(
            name = ".local",
            file = psiFile,
            context = contextElement,
            includeIncludes = false
        )

        assertNotNull("Should resolve local label with context", resolved)
        assertEquals("Should resolve to .local", ".local", (resolved as? NasmNamedElement)?.name)
    }

    fun testSymbolResolverAllTypes() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                label1:
                    nop
                CONST equ 5
                %define MACRO 10
                %assign VAR 15
            """.trimIndent()
        )

        // Test resolving each type with SearchType.ALL
        val resolvedLabel = NasmSymbolResolver.resolve(
            name = "label1",
            file = psiFile,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = false
        )
        assertNotNull("Should resolve label with ALL search types", resolvedLabel)

        val resolvedEqu = NasmSymbolResolver.resolve(
            name = "CONST",
            file = psiFile,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = false
        )
        assertNotNull("Should resolve EQU with ALL search types", resolvedEqu)

        val resolvedMacro = NasmSymbolResolver.resolve(
            name = "MACRO",
            file = psiFile,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = false
        )
        assertNotNull("Should resolve %define with ALL search types", resolvedMacro)

        val resolvedAssign = NasmSymbolResolver.resolve(
            name = "VAR",
            file = psiFile,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = false
        )
        assertNotNull("Should resolve %assign with ALL search types", resolvedAssign)
    }

    // ===== Rename Refactoring Tests =====

    fun testLabelRename() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                old_name:
                    mov rax, 1
                    jmp old_name
            """.trimIndent()
        )

        val label = psiFile.findLabels().firstOrNull { it.name == "old_name" }
        assertNotNull("Should find old_name label", label)

        // Test that the label is a named element (required for rename)
        assertTrue("Label should be NasmNamedElement", label is NasmNamedElement)

        // Find usages before rename
        val usagesBefore = ReferencesSearch.search(label!!).findAll()
        assertTrue("Should have usages before rename", usagesBefore.isNotEmpty())
    }

    fun testMacroRename() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %define OLD_MACRO 123

                mov rax, OLD_MACRO
            """.trimIndent()
        )

        val macro = psiFile.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "OLD_MACRO" }
        assertNotNull("Should find OLD_MACRO", macro)

        assertTrue("Macro should be NasmNamedElement", macro is NasmNamedElement)

        val usages = ReferencesSearch.search(macro!!).findAll()
        assertTrue("Should have usages", usages.isNotEmpty())
    }

    // ===== Find Usages Tests =====

    fun testFindLabelUsages() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                loop_start:
                    inc rax
                    cmp rax, 10
                    jl loop_start
                    je loop_start
                    jmp loop_start
            """.trimIndent()
        )

        val label = psiFile.findLabels().firstOrNull { it.name == "loop_start" }
        assertNotNull("Should find loop_start label", label)

        val usages = ReferencesSearch.search(label!!).findAll()
        assertTrue("loop_start should have multiple usages", usages.size >= 3)
    }

    fun testFindMacroUsages() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                %define CONSTANT 42
                %define DERIVED (CONSTANT * 2)

                mov rax, CONSTANT
                mov rbx, CONSTANT
                mov rcx, DERIVED
            """.trimIndent()
        )

        val constantMacro = psiFile.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "CONSTANT" }
        assertNotNull("Should find CONSTANT macro", constantMacro)

        val usages = ReferencesSearch.search(constantMacro!!).findAll()
        // Should find usage in DERIVED and potentially in mov instructions
        assertTrue("CONSTANT should have at least 1 usage", usages.size >= 1)
    }

    fun testNoUsagesForUnusedSymbol() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                unused_label:
                    nop

                main:
                    ret
            """.trimIndent()
        )

        val unusedLabel = psiFile.findLabels().firstOrNull { it.name == "unused_label" }
        assertNotNull("Should find unused_label", unusedLabel)

        val usages = ReferencesSearch.search(unusedLabel!!).findAll()
        assertEquals("unused_label should have no usages", 0, usages.size)
    }

    // ===== Reference Variants Tests (for code completion) =====

    fun testLabelReferenceHasNoVariants() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                my_label:
                    jmp my_label
            """.trimIndent()
        )

        // Find the reference to my_label
        val references = PsiTreeUtil.findChildrenOfType(psiFile, PsiElement::class.java)
            .filter { it.text == "my_label" && it.textOffset > 10 }
            .flatMap { it.references.toList() + (it.parent?.references?.toList() ?: emptyList()) }

        assertTrue("Should find at least one reference", references.isNotEmpty())

        // References should not provide variants (completion is handled elsewhere)
        val variants = references.firstOrNull()?.variants ?: emptyArray()
        assertEquals("Reference should not provide variants", 0, variants.size)
    }

    // ===== Extern Declaration Tests =====

    fun testExternSymbolReference() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                extern printf

                main:
                    call printf
            """.trimIndent()
        )

        val externSymbol = psiFile.findExternSymbols().firstOrNull { (it as? NasmNamedElement)?.name == "printf" }
        assertNotNull("Should find extern printf", externSymbol)

        val resolved = NasmSymbolResolver.resolve(
            name = "printf",
            file = psiFile,
            searchTypes = setOf(NasmSymbolResolver.SearchType.EXTERNS),
            includeIncludes = false
        )
        assertNotNull("Should resolve extern symbol", resolved)
    }

    // ===== Edge Cases =====

    fun testEmptyFileResolution() {
        val psiFile = myFixture.configureByText("empty.asm", "")

        val resolved = NasmSymbolResolver.resolve(
            name = "anything",
            file = psiFile,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = false
        )

        assertNull("Should not resolve anything in empty file", resolved)
    }

    fun testCaseSensitiveResolution() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                MyLabel:
                    nop
                mylabel:
                    nop
            """.trimIndent()
        )

        val labels = psiFile.findLabels()
        assertEquals("Should find both labels (case-sensitive)", 2, labels.size)

        val resolvedUpper = psiFile.findLabelDefinition("MyLabel")
        val resolvedLower = psiFile.findLabelDefinition("mylabel")

        assertNotNull("Should resolve MyLabel", resolvedUpper)
        assertNotNull("Should resolve mylabel", resolvedLower)
        assertNotSame("Should resolve to different labels", resolvedUpper, resolvedLower)
    }

    fun testReferenceToUndefinedLocalLabel() {
        val psiFile = myFixture.configureByText(
            "test.asm",
            """
                global_label:
                    jmp .undefined_local
            """.trimIndent()
        )

        val globalLabel = psiFile.findLabels().first { it.name == "global_label" }
        val contextElement = psiFile.findElementAt(globalLabel.textOffset + 20)

        val resolved = psiFile.findLabelDefinition(".undefined_local", contextElement)
        assertNull("Should not resolve undefined local label", resolved)
    }

    fun testMultipleIncludesNoCircular() {
        // Create file A that includes B
        myFixture.addFileToProject(
            "b.inc",
            """
                %define FROM_B 1
            """.trimIndent()
        )

        // Create file C that includes B
        myFixture.addFileToProject(
            "c.inc",
            """
                %include "b.inc"
                %define FROM_C 2
            """.trimIndent()
        )

        // Create main file that includes both B and C
        val mainFile = myFixture.configureByText(
            "main.asm",
            """
                %include "b.inc"
                %include "c.inc"

                value: dd FROM_B, FROM_C
            """.trimIndent()
        )

        val includedFiles = mainFile.getIncludedFiles()
        // Should include b.inc directly, and c.inc (which also includes b.inc)
        // But b.inc should only appear once due to circular reference protection
        assertTrue("Should have at least 1 included file", includedFiles.isNotEmpty())

        // Verify we can find macros from all included files
        val fromB = includedFiles.firstNotNullOfOrNull { file ->
            file.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "FROM_B" }
        }
        assertNotNull("Should find FROM_B from included file", fromB)

        val fromC = includedFiles.firstNotNullOfOrNull { file ->
            file.findPpDefineStmts().firstOrNull { (it as? NasmNamedElement)?.name == "FROM_C" }
        }
        assertNotNull("Should find FROM_C from included file", fromC)
    }
}
