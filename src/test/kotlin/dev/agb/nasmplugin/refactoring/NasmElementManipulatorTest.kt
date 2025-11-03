package dev.agb.nasmplugin.refactoring

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for element manipulators that enable rename refactoring.
 * Tests the range calculation and registration of manipulators.
 */
class NasmElementManipulatorTest : BasePlatformTestCase() {

    // ===== NasmElementManipulator Tests =====

    fun testNasmElementManipulatorRegisteredForLabels() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            my_label:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val label = labels.firstOrNull()
        assertNotNull("Should find label", label)

        // Verify manipulator is registered
        val manipulator = com.intellij.psi.ElementManipulators.getManipulator(label!!)
        assertNotNull("Should have manipulator for label", manipulator)
        assertTrue("Should be NasmElementManipulator",
            manipulator is NasmElementManipulator)
    }

    fun testNasmElementManipulatorGetRangeForLabel() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            my_function:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val label = labels.firstOrNull()
        assertNotNull("Should find label", label)

        val manipulator = NasmElementManipulator()
        val range = manipulator.getRangeInElement(label!!)

        // Range should cover the label name (without the colon)
        assertTrue("Range should be valid", range.length > 0)
        assertTrue("Range should be within element", range.endOffset <= label.textLength)
        assertEquals("Range should start at 0", 0, range.startOffset)
    }

    fun testNasmElementManipulatorRegisteredForEquConstants() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            MY_CONSTANT equ 42
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should find constant", constant)

        val manipulator = com.intellij.psi.ElementManipulators.getManipulator(constant!!)
        assertNotNull("Should have manipulator for constant", manipulator)
    }

    fun testNasmElementManipulatorGetRangeForConstant() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            BUFFER_SIZE equ 1024
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should find constant", constant)

        val manipulator = NasmElementManipulator()
        val range = manipulator.getRangeInElement(constant!!)

        assertTrue("Range should be valid", range.length > 0)
        assertTrue("Range should cover identifier", range.endOffset <= constant.textLength)
    }

    fun testNasmElementManipulatorRegisteredForMacros() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro TEST_MACRO 0
                nop
            %endmacro
            """.trimIndent()
        )

        val macros = file.findMultiLineMacros()
        val macro = macros.firstOrNull()
        assertNotNull("Should find macro", macro)

        val manipulator = com.intellij.psi.ElementManipulators.getManipulator(macro!!)
        assertNotNull("Should have manipulator for macro", manipulator)
    }

    fun testNasmElementManipulatorGetRangeForMacro() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro MY_MACRO 0
                nop
            %endmacro
            """.trimIndent()
        )

        val macros = file.findMultiLineMacros()
        val macro = macros.firstOrNull()
        assertNotNull("Should find macro", macro)

        val manipulator = NasmElementManipulator()
        val range = manipulator.getRangeInElement(macro!!)

        assertTrue("Range should be valid for macro", range.length > 0)
    }

    fun testNasmElementManipulatorGetRangeForPrivateLabel() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            .private_label:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        val label = labels.firstOrNull()
        assertNotNull("Should find private label", label)

        val manipulator = NasmElementManipulator()
        val range = manipulator.getRangeInElement(label!!)

        assertTrue("Range should be valid for private label", range.length > 0)
        assertTrue("Range should include dot", range.length >= ".private_label".length)
    }

    // ===== NasmSymbolRefManipulator Tests =====

    fun testSymbolRefManipulatorGetRange() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                jmp target
                ret
            target:
                ret
            """.trimIndent()
        )

        val instructions = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmInstruction::class.java
        )
        val jmpInstr = instructions.firstOrNull { it.text.contains("jmp") }
        val symbolRef = com.intellij.psi.util.PsiTreeUtil.findChildOfType(
            jmpInstr,
            NasmSymbolRef::class.java
        )
        assertNotNull("Should find symbol reference", symbolRef)

        val manipulator = NasmSymbolRefManipulator()
        val range = manipulator.getRangeInElement(symbolRef!!)

        assertEquals("Range should cover entire symbol", symbolRef.textLength, range.length)
        assertEquals("Range should start at 0", 0, range.startOffset)
    }

    fun testSymbolRefManipulatorRegistered() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                call helper
                ret
            helper:
                ret
            """.trimIndent()
        )

        val instructions = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmInstruction::class.java
        )
        val callInstr = instructions.firstOrNull { it.text.contains("call") }
        val symbolRef = com.intellij.psi.util.PsiTreeUtil.findChildOfType(
            callInstr,
            NasmSymbolRef::class.java
        )
        assertNotNull("Should find symbol reference", symbolRef)

        val manipulator = com.intellij.psi.ElementManipulators.getManipulator(symbolRef!!)
        assertNotNull("Should have manipulator for symbol ref", manipulator)
        assertTrue("Should be NasmSymbolRefManipulator",
            manipulator is NasmSymbolRefManipulator)
    }

    // ===== NasmPreprocessorTokenManipulator Tests =====

    fun testPreprocessorTokenManipulatorGetRange() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %define TOKEN value
            """.trimIndent()
        )

        val tokens = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmPreprocessorToken::class.java
        )
        assertTrue("Should find preprocessor tokens", tokens.isNotEmpty())

        val manipulator = NasmPreprocessorTokenManipulator()
        val token = tokens.first()
        val range = manipulator.getRangeInElement(token)

        assertEquals("Range should cover entire token", token.textLength, range.length)
        assertEquals("Range should start at 0", 0, range.startOffset)
    }

    // ===== NasmInstructionManipulator Tests =====

    fun testInstructionManipulatorGetRangeForMnemonic() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                mov rax, rbx
                ret
            """.trimIndent()
        )

        val instructions = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmInstruction::class.java
        )
        val movInstr = instructions.firstOrNull { it.text.contains("mov") }
        assertNotNull("Should find mov instruction", movInstr)

        val manipulator = NasmInstructionManipulator()
        val range = manipulator.getRangeInElement(movInstr!!)

        // Range should cover the mnemonic part
        assertTrue("Range should be valid", range.length > 0)
        assertTrue("Range should be within instruction", range.endOffset <= movInstr.textLength)
    }

    fun testInstructionManipulatorGetRangeForMacroInvocation() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro TEST 0
                nop
            %endmacro

            section .text
            main:
                TEST
                ret
            """.trimIndent()
        )

        val instructions = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmInstruction::class.java
        )
        val testInstr = instructions.firstOrNull { it.text.trim() == "TEST" }
        assertNotNull("Should find macro invocation", testInstr)

        val manipulator = NasmInstructionManipulator()
        val range = manipulator.getRangeInElement(testInstr!!)

        assertTrue("Range should be valid for macro invocation", range.length > 0)
    }

    // ===== Integration Tests =====

    fun testAllManipulatorsRegistered() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42

            %macro TEST 0
                nop
            %endmacro

            section .text
            helper:
                ret

            main:
                call helper
                mov rax, CONSTANT
                TEST
                ret
            """.trimIndent()
        )

        // Test label manipulator
        val labels = file.findLabels()
        val label = labels.firstOrNull { it.name == "helper" }
        assertNotNull("Should find label", label)
        assertNotNull("Label should have manipulator",
            com.intellij.psi.ElementManipulators.getManipulator(label!!))

        // Test constant manipulator
        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should find constant", constant)
        assertNotNull("Constant should have manipulator",
            com.intellij.psi.ElementManipulators.getManipulator(constant!!))

        // Test macro manipulator
        val macros = file.findMultiLineMacros()
        val macro = macros.firstOrNull()
        assertNotNull("Should find macro", macro)
        assertNotNull("Macro should have manipulator",
            com.intellij.psi.ElementManipulators.getManipulator(macro!!))

        // Test symbol reference manipulator
        val instructions = com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
            file,
            NasmInstruction::class.java
        )
        val callInstr = instructions.firstOrNull { it.text.contains("call") }
        val symbolRef = com.intellij.psi.util.PsiTreeUtil.findChildOfType(
            callInstr,
            NasmSymbolRef::class.java
        )
        assertNotNull("Should find symbol reference", symbolRef)
        assertNotNull("Symbol ref should have manipulator",
            com.intellij.psi.ElementManipulators.getManipulator(symbolRef!!))
    }

    fun testManipulatorsValidateRange() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            MY_CONST equ 42

            section .text
            my_label:
                ret
            """.trimIndent()
        )

        val constants = file.findEquDefinitions()
        val constant = constants.firstOrNull()
        assertNotNull("Should find constant", constant)

        val manipulator = NasmElementManipulator()
        val range = manipulator.getRangeInElement(constant!!)

        // Verify range is within element bounds
        assertTrue("Range start should be >= 0", range.startOffset >= 0)
        assertTrue("Range end should be <= element length",
            range.endOffset <= constant.textLength)
        assertTrue("Range should be non-empty", range.length > 0)
    }

    fun testManipulatorsWithSpecialCharacters() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            _start:
                ret

            __internal:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()

        // Test _start label
        val startLabel = labels.firstOrNull { it.name == "_start" }
        assertNotNull("Should find _start label", startLabel)

        val manipulator = NasmElementManipulator()
        val range1 = manipulator.getRangeInElement(startLabel!!)
        assertTrue("Should handle leading underscore", range1.length > 0)

        // Test __internal label
        val internalLabel = labels.firstOrNull { it.name == "__internal" }
        assertNotNull("Should find __internal label", internalLabel)

        val range2 = manipulator.getRangeInElement(internalLabel!!)
        assertTrue("Should handle double underscore", range2.length > 0)
    }

    fun testManipulatorsWithNumericSuffixes() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            loop1:
                ret

            loop2:
                ret
            """.trimIndent()
        )

        val labels = file.findLabels()
        labels.forEach { label ->
            val manipulator = NasmElementManipulator()
            val range = manipulator.getRangeInElement(label)
            assertTrue("Should handle numeric suffixes in ${label.name}",
                range.length > 0)
        }
    }
}
