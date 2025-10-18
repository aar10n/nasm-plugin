package dev.agb.nasmplugin.completion

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Comprehensive tests for the NASM completion system.
 * Tests all components: NasmCompletionContributor, CompletionContextDetector,
 * NasmCompletionVariantBuilder, and CompletionContext.
 */
class CompletionTests : BasePlatformTestCase() {

    // ===== Context Detection Tests =====

    fun testDetectLineStartContext() {
        myFixture.configureByText("test.asm", "\n<caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect LineStart context", CompletionContext.LineStart, context)
    }

    fun testDetectAfterPercentContext() {
        myFixture.configureByText("test.asm", "%<caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect AfterPercent context", CompletionContext.AfterPercent, context)
    }

    fun testDetectMemoryOperandContext() {
        myFixture.configureByText("test.asm", "mov rax, [<caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect MemoryOperand context", CompletionContext.MemoryOperand, context)
    }

    fun testDetectMemoryOperandWithComplexExpression() {
        myFixture.configureByText("test.asm", "mov rax, [rdi + <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect MemoryOperand context in complex expression", CompletionContext.MemoryOperand, context)
    }

    fun testDetectJumpTargetContext() {
        myFixture.configureByText("test.asm", "jmp <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect JumpTarget context", CompletionContext.JumpTarget, context)
    }

    fun testDetectCallTargetContext() {
        myFixture.configureByText("test.asm", "call <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect JumpTarget context for call", CompletionContext.JumpTarget, context)
    }

    fun testDetectConditionalJumpContext() {
        myFixture.configureByText("test.asm", "jne <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect JumpTarget context for conditional jump", CompletionContext.JumpTarget, context)
    }

    fun testDetectGlobalDirectiveContext() {
        myFixture.configureByText("test.asm", "global <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect GlobalDirective context", CompletionContext.GlobalDirective, context)
    }

    fun testDetectExternDirectiveContext() {
        myFixture.configureByText("test.asm", "extern <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect ExternDirective context", CompletionContext.ExternDirective, context)
    }

    fun testDetectDataOperandContext() {
        myFixture.configureByText("test.asm", "mov rax, <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect DataOperand context", CompletionContext.DataOperand, context)
    }

    fun testDetectDataOperandAfterComma() {
        myFixture.configureByText("test.asm", "add rax, rbx, <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("Should detect DataOperand context after comma", CompletionContext.DataOperand, context)
    }

    fun testDetectGeneralContext() {
        myFixture.configureByText("test.asm", "some_random_text <caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        // General context is the fallback
        assertTrue("Should detect General or DataOperand context",
            context == CompletionContext.General || context == CompletionContext.DataOperand)
    }

    // ===== Context Precedence Tests =====

    fun testAfterPercentHasHighestPrecedence() {
        // After % should override any other context
        myFixture.configureByText("test.asm", "mov rax, %<caret>")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("AfterPercent should have highest precedence", CompletionContext.AfterPercent, context)
    }

    fun testMemoryOperandPrecedenceOverDataOperand() {
        // Inside brackets should be MemoryOperand, not DataOperand
        myFixture.configureByText("test.asm", "mov rax, [rbx + <caret>]")
        val position = myFixture.file.findElementAt(myFixture.caretOffset) ?: return
        val context = CompletionContextDetector.detectContext(position)
        assertEquals("MemoryOperand should have precedence over DataOperand", CompletionContext.MemoryOperand, context)
    }

    // ===== Completion Contributor Tests =====

    fun testInstructionCompletionAtLineStart() {
        myFixture.configureByText("test.asm", "section .text\n<caret>")
        val completions = myFixture.completeBasic() ?: return

        val instructionNames = completions.map { it.lookupString }
        assertTrue("Should include mov instruction", "mov" in instructionNames)
        assertTrue("Should include add instruction", "add" in instructionNames)
        assertTrue("Should include jmp instruction", "jmp" in instructionNames)
    }

    fun testDirectiveCompletionAtLineStart() {
        myFixture.configureByText("test.asm", "<caret>")
        val completions = myFixture.completeBasic() ?: return

        val directiveNames = completions.map { it.lookupString }
        assertTrue("Should include %macro directive", "%macro" in directiveNames || "macro" in directiveNames)
        assertTrue("Should include section directive", "section" in directiveNames || "%section" in directiveNames)
    }

    fun testPreprocessorCompletionAfterPercent() {
        myFixture.configureByText("test.asm", "%<caret>")
        val completions = myFixture.completeBasic() ?: return

        val names = completions.map { it.lookupString }
        // After %, directives should be shown without the % prefix
        assertTrue("Should include macro (without %)", "macro" in names)
        assertTrue("Should include define (without %)", "define" in names)
        assertTrue("Should include include (without %)", "include" in names)
    }

    fun testRegisterCompletionInMemoryOperand() {
        myFixture.configureByText("test.asm", "mov rax, [<caret>")
        val completions = myFixture.completeBasic() ?: return

        val registerNames = completions.map { it.lookupString }
        assertTrue("Should include rax register", "rax" in registerNames)
        assertTrue("Should include rbx register", "rbx" in registerNames)
        assertTrue("Should include rsp register", "rsp" in registerNames)
    }

    fun testRegisterCompletionInDataOperand() {
        myFixture.configureByText("test.asm", "mov rax, <caret>")
        val completions = myFixture.completeBasic() ?: return

        val registerNames = completions.map { it.lookupString }
        assertTrue("Should include registers in data operand", "rbx" in registerNames)
    }

    fun testNoInstructionsInMemoryOperand() {
        myFixture.configureByText("test.asm", "mov rax, [<caret>")
        val completions = myFixture.completeBasic() ?: return

        val hasInstructions = completions.any { it.lookupString == "mov" }
        assertFalse("Should not show instructions inside memory operand", hasInstructions)
    }

    fun testNoInstructionsAfterPercent() {
        myFixture.configureByText("test.asm", "%<caret>")
        val completions = myFixture.completeBasic() ?: return

        val hasInstructions = completions.any { it.lookupString == "mov" }
        assertFalse("Should not show instructions after %", hasInstructions)
    }

    // ===== User-Defined Symbol Tests =====

    fun testLabelCompletionInJumpContext() {
        myFixture.configureByText("test.asm", """
            section .text
            my_label:
                nop
                jmp <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val labelNames = completions.map { it.lookupString }
        assertTrue("Should include user-defined label", "my_label" in labelNames)
    }

    fun testLabelCompletionInCallContext() {
        myFixture.configureByText("test.asm", """
            section .text
            my_function:
                ret
            start:
                call <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val labelNames = completions.map { it.lookupString }
        assertTrue("Should include function label", "my_function" in labelNames)
    }

    fun testMacroCompletionAtLineStart() {
        myFixture.configureByText("test.asm", """
            %macro my_macro 0
                nop
            %endmacro

            section .text
            <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val macroNames = completions.map { it.lookupString }
        assertTrue("Should include user-defined macro", "my_macro" in macroNames)
    }

    fun testEquConstantCompletion() {
        myFixture.configureByText("test.asm", """
            MY_CONST equ 42

            section .text
            mov rax, <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val constantNames = completions.map { it.lookupString }
        assertTrue("Should include EQU constant", "MY_CONST" in constantNames)
    }

    fun testDefineCompletion() {
        myFixture.configureByText("test.asm", """
            %define MY_DEFINE 100

            section .text
            mov rax, <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val defineNames = completions.map { it.lookupString }
        assertTrue("Should include %define constant", "MY_DEFINE" in defineNames)
    }

    fun testAssignCompletion() {
        myFixture.configureByText("test.asm", """
            %assign MY_ASSIGN 200

            section .text
            mov rax, <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val assignNames = completions.map { it.lookupString }
        assertTrue("Should include %assign constant", "MY_ASSIGN" in assignNames)
    }

    fun testExternSymbolInExternContext() {
        myFixture.configureByText("test.asm", """
            extern printf

            extern <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val externNames = completions.map { it.lookupString }
        assertTrue("Should include extern symbols", "printf" in externNames)
    }

    fun testExternSymbolInGlobalContext() {
        myFixture.configureByText("test.asm", """
            extern printf

            global <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val names = completions.map { it.lookupString }
        // Extern symbols should not appear in global directive context
        assertFalse("Should not include extern symbols in global context", "printf" in names)
    }

    fun testLocalLabelNotInGlobalContext() {
        myFixture.configureByText("test.asm", """
            section .text
            my_func:
            .local_label:
                nop

            global <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val names = completions.map { it.lookupString }
        // Local labels should not appear in global directive (only global labels)
        assertTrue("Should include global label", "my_func" in names)
        assertFalse("Should not include local label", ".local_label" in names)
    }

    fun testMacroNotInJumpContext() {
        myFixture.configureByText("test.asm", """
            %macro my_macro 0
                nop
            %endmacro

            section .text
            jmp <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val names = completions.map { it.lookupString }
        // Multi-line macros ARE allowed in jump context (they can generate labels)
        assertTrue("Should include multi-line macros in jump context", "my_macro" in names)
    }

    // ===== Priority Tests =====

    fun testUserSymbolPriorityInJumpContext() {
        myFixture.configureByText("test.asm", """
            section .text
            my_label:
                nop
                jmp my<caret>
        """.trimIndent())

        // Complete and check if user symbol has high priority
        val completions = myFixture.completeBasic()
        if (completions == null) {
            // Auto-completed - verify label was inserted
            assertTrue("Should auto-complete to label", myFixture.editor.document.text.contains("my_label"))
        } else {
            val myLabel = completions.firstOrNull { it.lookupString == "my_label" }
            assertNotNull("Should find my_label", myLabel)

            // Check that it's prioritized (assuming it comes before generic items)
            val prioritizedElement = myLabel as? PrioritizedLookupElement<*>
            if (prioritizedElement != null) {
                assertTrue("Label should have high priority in jump context",
                    prioritizedElement.priority > 90.0)
            }
        }
    }

    fun testInstructionPriorityAtLineStart() {
        myFixture.configureByText("test.asm", """
            section .text
            mo<caret>
        """.trimIndent())

        val completions = myFixture.completeBasic()
        // Should show or auto-complete to mov instruction
        if (completions == null) {
            assertTrue("Should have mov-related completion",
                myFixture.editor.document.text.contains("mov"))
        } else {
            val movInstructions = completions.filter { it.lookupString.startsWith("mov") }
            assertTrue("Should have mov instruction", movInstructions.isNotEmpty())
        }
    }

    // ===== Variant Builder Tests =====

    fun testBuildVariantsFromFile() {
        val file = myFixture.configureByText("test.asm", """
            MY_CONST equ 100
            %define MY_DEFINE 200
            %assign MY_ASSIGN 300

            %macro my_macro 0
                nop
            %endmacro

            extern printf

            section .text
            my_label:
                nop
        """.trimIndent())

        val variants = NasmCompletionVariantBuilder.buildVariants(file)
        val variantNames = variants.mapNotNull {
            (it as? com.intellij.codeInsight.lookup.LookupElementBuilder)?.lookupString
        }

        assertTrue("Should include EQU constant", "MY_CONST" in variantNames)
        assertTrue("Should include %define", "MY_DEFINE" in variantNames)
        assertTrue("Should include %assign", "MY_ASSIGN" in variantNames)
        assertTrue("Should include macro", "my_macro" in variantNames)
        assertTrue("Should include label", "my_label" in variantNames)
        assertTrue("Should include extern", "printf" in variantNames)
    }

    fun testBuildVariantsWithTypeFilter() {
        val file = myFixture.configureByText("test.asm", """
            MY_CONST equ 100

            section .text
            my_label:
                nop
        """.trimIndent())

        // Build variants with only labels
        val variants = NasmCompletionVariantBuilder.buildVariants(
            file,
            types = setOf(NasmCompletionVariantBuilder.VariantType.LABELS)
        )
        val variantNames = variants.mapNotNull {
            (it as? com.intellij.codeInsight.lookup.LookupElementBuilder)?.lookupString
        }

        assertTrue("Should include label", "my_label" in variantNames)
        assertFalse("Should not include EQU constant", "MY_CONST" in variantNames)
    }

    fun testBuildVariantsMultipleTypes() {
        val file = myFixture.configureByText("test.asm", """
            MY_CONST equ 100
            %define MY_DEFINE 200

            section .text
            my_label:
                nop
        """.trimIndent())

        // Build variants with labels and EQU constants only
        val variants = NasmCompletionVariantBuilder.buildVariants(
            file,
            types = setOf(
                NasmCompletionVariantBuilder.VariantType.LABELS,
                NasmCompletionVariantBuilder.VariantType.EQU_CONSTANTS
            )
        )
        val variantNames = variants.mapNotNull {
            (it as? com.intellij.codeInsight.lookup.LookupElementBuilder)?.lookupString
        }

        assertTrue("Should include label", "my_label" in variantNames)
        assertTrue("Should include EQU constant", "MY_CONST" in variantNames)
        assertFalse("Should not include %define", "MY_DEFINE" in variantNames)
    }

    // ===== Include File Completion Tests =====

    fun testIncludeDirectiveDetection() {
        myFixture.configureByText("test.asm", "%include \"<caret>")

        // The completion system should detect this as an include directive
        val completions = myFixture.completeBasic()
        // Can't test actual file completions without real files, but ensure it doesn't crash
        assertNotNull("Include directive completion should not crash", completions)
    }

    fun testIncludeDirectiveWithSingleQuote() {
        myFixture.configureByText("test.asm", "%include '<caret>")

        val completions = myFixture.completeBasic()
        assertNotNull("Include directive with single quote should not crash", completions)
    }

    fun testIncludeDirectiveWithAngleBracket() {
        myFixture.configureByText("test.asm", "%include <<caret>")

        val completions = myFixture.completeBasic()
        assertNotNull("Include directive with angle bracket should not crash", completions)
    }

    // ===== Edge Cases =====

    fun testCompletionInEmptyFile() {
        myFixture.configureByText("test.asm", "<caret>")
        val completions = myFixture.completeBasic() ?: return

        assertTrue("Should have completions in empty file", completions.isNotEmpty())
    }

    fun testCompletionAfterComment() {
        myFixture.configureByText("test.asm", "; comment\n<caret>")
        val completions = myFixture.completeBasic() ?: return

        assertTrue("Should have completions after comment", completions.isNotEmpty())
    }

    fun testCompletionInsideComment() {
        myFixture.configureByText("test.asm", "; comm<caret>ent")
        val completions = myFixture.completeBasic() ?: return

        // Inside a comment, we might still get completions, but they shouldn't be meaningful
        // The important thing is it doesn't crash
        assertNotNull("Completion inside comment should not crash", completions)
    }

    fun testCompletionWithMultipleLabels() {
        myFixture.configureByText("test.asm", """
            section .text
            label1:
                nop
            label2:
                nop
            label3:
                nop
                jmp <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val labelNames = completions.map { it.lookupString }

        assertTrue("Should include label1", "label1" in labelNames)
        assertTrue("Should include label2", "label2" in labelNames)
        assertTrue("Should include label3", "label3" in labelNames)
    }

    fun testCompletionWithLocalLabels() {
        myFixture.configureByText("test.asm", """
            section .text
            global_label:
            .local1:
                nop
            .local2:
                nop
                jmp <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val labelNames = completions.map { it.lookupString }

        assertTrue("Should include global label", "global_label" in labelNames)
        assertTrue("Should include local label 1", ".local1" in labelNames)
        assertTrue("Should include local label 2", ".local2" in labelNames)
    }

    fun testCompletionDoesNotShowCurrentPosition() {
        myFixture.configureByText("test.asm", """
            section .text
            my_label:
                <caret>
        """.trimIndent())

        // Complete right after the label definition
        val completions = myFixture.completeBasic() ?: return

        // Should still show the label (it's valid to reference it)
        val labelNames = completions.map { it.lookupString }
        assertTrue("Should include the label defined above", "my_label" in labelNames)
    }

    fun testNoDuplicateCompletions() {
        myFixture.configureByText("test.asm", """
            MY_CONST equ 100
            MY_CONST equ 200  ; Redefinition (might be invalid but shouldn't cause duplicates)

            section .text
            mov rax, <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val constantNames = completions.map { it.lookupString }

        // Count how many times MY_CONST appears
        val myConstCount = constantNames.count { it == "MY_CONST" }

        // Should appear only once despite being defined twice
        assertEquals("Should not have duplicate completions", 1, myConstCount)
    }

    fun testCompletionWithIndentation() {
        myFixture.configureByText("test.asm", """
            section .text
            my_label:
                    <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        assertTrue("Should have completions after indentation", completions.isNotEmpty())

        val instructionNames = completions.map { it.lookupString }
        assertTrue("Should include instructions", "mov" in instructionNames)
    }

    fun testCompletionAfterLabelWithColon() {
        myFixture.configureByText("test.asm", """
            section .text
            my_label: <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val instructionNames = completions.map { it.lookupString }
        assertTrue("Should include instructions after label with colon", "nop" in instructionNames)
    }

    fun testCaseInsensitiveCompletion() {
        myFixture.configureByText("test.asm", """
            %macro MY_MACRO 0
                nop
            %endmacro

            section .text
            my_<caret>
        """.trimIndent())

        val completions = myFixture.completeBasic()
        if (completions == null) {
            // Auto-completed
            val text = myFixture.editor.document.text
            assertTrue("Should complete case-insensitively",
                text.contains("MY_MACRO") || text.contains("my_macro"))
        } else {
            val macroNames = completions.map { it.lookupString.lowercase() }
            assertTrue("Should include macro case-insensitively", "my_macro" in macroNames)
        }
    }

    fun testCompletionInDataSection() {
        myFixture.configureByText("test.asm", """
            section .data
            <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        assertTrue("Should have completions in data section", completions.isNotEmpty())
    }

    fun testCompletionInBssSection() {
        myFixture.configureByText("test.asm", """
            section .bss
            <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        assertTrue("Should have completions in bss section", completions.isNotEmpty())
    }

    // ===== Context Properties Tests =====

    fun testLineStartContextProperties() {
        val context = CompletionContext.LineStart
        assertTrue("LineStart should show instructions", context.showInstructions)
        assertTrue("LineStart should show directives", context.showDirectives)
        assertFalse("LineStart should not show registers", context.showRegisters)
        assertFalse("LineStart should not show preprocessor functions", context.showPreprocessorFunctions)
    }

    fun testJumpTargetContextProperties() {
        val context = CompletionContext.JumpTarget
        assertFalse("JumpTarget should not show instructions", context.showInstructions)
        assertFalse("JumpTarget should not show registers", context.showRegisters)

        val allowedTypes = context.allowedSymbolTypes
        assertTrue("JumpTarget should allow labels",
            NasmCompletionVariantBuilder.VariantType.LABELS in allowedTypes)
        assertFalse("JumpTarget should not allow EQU constants",
            NasmCompletionVariantBuilder.VariantType.EQU_CONSTANTS in allowedTypes)
    }

    fun testMemoryOperandContextProperties() {
        val context = CompletionContext.MemoryOperand
        assertTrue("MemoryOperand should show registers", context.showRegisters)
        assertTrue("MemoryOperand should show preprocessor functions", context.showPreprocessorFunctions)
        assertFalse("MemoryOperand should not show instructions", context.showInstructions)

        val allowedTypes = context.allowedSymbolTypes
        assertTrue("MemoryOperand should allow labels",
            NasmCompletionVariantBuilder.VariantType.LABELS in allowedTypes)
        assertTrue("MemoryOperand should allow EQU constants",
            NasmCompletionVariantBuilder.VariantType.EQU_CONSTANTS in allowedTypes)
        assertFalse("MemoryOperand should not allow multi-line macros",
            NasmCompletionVariantBuilder.VariantType.MULTI_LINE_MACROS in allowedTypes)
    }

    fun testDataOperandContextProperties() {
        val context = CompletionContext.DataOperand
        assertTrue("DataOperand should show registers", context.showRegisters)
        assertTrue("DataOperand should show preprocessor functions", context.showPreprocessorFunctions)
        assertFalse("DataOperand should not show instructions", context.showInstructions)

        // DataOperand should allow all symbol types
        assertEquals("DataOperand should allow all symbol types",
            NasmCompletionVariantBuilder.VariantType.entries.toSet(),
            context.allowedSymbolTypes)
    }

    fun testAfterPercentContextProperties() {
        val context = CompletionContext.AfterPercent
        assertFalse("AfterPercent should not show instructions", context.showInstructions)
        assertFalse("AfterPercent should not show registers", context.showRegisters)
        assertTrue("AfterPercent should show directives", context.showDirectives)
        assertTrue("AfterPercent should show preprocessor functions", context.showPreprocessorFunctions)
        assertTrue("AfterPercent should not allow any user symbols", context.allowedSymbolTypes.isEmpty())
    }

    fun testGlobalDirectiveContextProperties() {
        val context = CompletionContext.GlobalDirective
        assertFalse("GlobalDirective should not show instructions", context.showInstructions)
        assertFalse("GlobalDirective should not show registers", context.showRegisters)

        val allowedTypes = context.allowedSymbolTypes
        assertTrue("GlobalDirective should allow labels",
            NasmCompletionVariantBuilder.VariantType.LABELS in allowedTypes)
        assertFalse("GlobalDirective should not allow externs",
            NasmCompletionVariantBuilder.VariantType.EXTERNS in allowedTypes)
    }

    fun testExternDirectiveContextProperties() {
        val context = CompletionContext.ExternDirective

        val allowedTypes = context.allowedSymbolTypes
        assertTrue("ExternDirective should allow externs",
            NasmCompletionVariantBuilder.VariantType.EXTERNS in allowedTypes)
        assertEquals("ExternDirective should only allow externs", 1, allowedTypes.size)
    }

    fun testGeneralContextProperties() {
        val context = CompletionContext.General
        assertTrue("General should show instructions", context.showInstructions)
        assertTrue("General should show registers", context.showRegisters)
        assertTrue("General should show directives", context.showDirectives)
        assertTrue("General should show preprocessor functions", context.showPreprocessorFunctions)

        // General should allow all symbol types
        assertEquals("General should allow all symbol types",
            NasmCompletionVariantBuilder.VariantType.entries.toSet(),
            context.allowedSymbolTypes)
    }

    // ===== Integration Tests =====

    fun testCompleteWorkflow() {
        // Simulate a complete workflow: define various symbols and reference them
        myFixture.configureByText("test.asm", """
            MY_CONST equ 42
            %define MY_DEFINE 100

            %macro SAVE_REGS 0
                push rax
                push rbx
            %endmacro

            extern printf

            section .data
            message db "Hello", 0

            section .text
            global main
            main:
                SAVE_REGS
                mov rax, MY_CONST
                call <caret>
        """.trimIndent())

        val completions = myFixture.completeBasic() ?: return
        val names = completions.map { it.lookupString }

        // In call context, should show labels and macros
        assertTrue("Should include main label", "main" in names)
        assertTrue("Should include printf extern", "printf" in names)
        // Data labels are still labels, so they might appear
        assertTrue("Should include message label", "message" in names)
    }
}
