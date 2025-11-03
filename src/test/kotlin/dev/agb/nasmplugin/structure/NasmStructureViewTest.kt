package dev.agb.nasmplugin.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.psi.*

/**
 * Tests for NASM structure view functionality.
 * Tests that the structure view correctly shows sections, labels, macros,
 * and other elements in a hierarchical tree structure.
 */
class NasmStructureViewTest : BasePlatformTestCase() {

    fun testStructureViewBuilderIsCreated() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            global main
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file)

        assertNotNull("Structure view builder should be created", builder)
        assertTrue("Builder should be TreeBasedStructureViewBuilder",
            builder is TreeBasedStructureViewBuilder)
    }

    fun testStructureViewModelIsCreated() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder

        val model = builder.createStructureViewModel(null)

        assertNotNull("Structure view model should be created", model)
        assertTrue("Model should be NasmStructureViewModel",
            model is NasmStructureViewModel)
    }

    fun testRootNodeIsNotShown() {
        val file = myFixture.configureByText(
            "test.asm",
            "section .text\nmain:\n    ret"
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder

        assertFalse("Root node should not be shown", builder.isRootNodeShown)
    }

    fun testStructureViewShowsSections() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .data
                msg db "Hello", 0

            section .text
            global main
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children
        assertEquals("Should have 2 sections", 2, children.size)

        // Both children should be sections
        val child1 = (children[0] as NasmStructureViewElement).value
        val child2 = (children[1] as NasmStructureViewElement).value

        assertTrue("First element should be a section", child1 is NasmSectionDir)
        assertTrue("Second element should be a section", child2 is NasmSectionDir)
    }

    fun testStructureViewShowsLabelsUnderSections() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            global main
            main:
                call helper
                ret

            helper:
                mov rax, 0
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children
        assertEquals("Should have 1 section", 1, children.size)

        val sectionElement = children[0]
        val sectionChildren = sectionElement.children

        assertEquals("Section should have 2 labels", 2, sectionChildren.size)

        val label1 = (sectionChildren[0] as NasmStructureViewElement).value
        val label2 = (sectionChildren[1] as NasmStructureViewElement).value

        assertTrue("First child should be a label", label1 is NasmLabelDef)
        assertTrue("Second child should be a label", label2 is NasmLabelDef)

        assertEquals("First label should be 'main'", "main", (label1 as NasmNamedElement).name)
        assertEquals("Second label should be 'helper'", "helper", (label2 as NasmNamedElement).name)
    }

    fun testStructureViewShowsPrivateLabelsUnderPublicLabels() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                call .init
                call .cleanup
                ret

            .init:
                nop
                ret

            .cleanup:
                nop
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children
        assertEquals("Should have 1 section", 1, children.size)

        val sectionChildren = children[0].children
        assertEquals("Section should have 1 public label", 1, sectionChildren.size)

        val publicLabel = sectionChildren[0] as NasmStructureViewElement
        assertEquals("Public label should be 'main'", "main",
            (publicLabel.value as NasmNamedElement).name)

        // Check that private labels are nested under public label
        val privateLabels = publicLabel.children
        assertEquals("Should have 2 private labels nested", 2, privateLabels.size)

        val privateName1 = ((privateLabels[0] as NasmStructureViewElement).value as NasmNamedElement).name
        val privateName2 = ((privateLabels[1] as NasmStructureViewElement).value as NasmNamedElement).name

        assertEquals("First private label should be '.init'", ".init", privateName1)
        assertEquals("Second private label should be '.cleanup'", ".cleanup", privateName2)
    }

    fun testStructureViewShowsMacros() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro SAVE_REGS 0
                push rax
                push rbx
            %endmacro

            %macro RESTORE_REGS 0
                pop rbx
                pop rax
            %endmacro

            section .text
            main:
                SAVE_REGS
                RESTORE_REGS
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children

        // First two children should be macros, last should be section
        assertTrue("Should have at least 3 elements", children.size >= 3)

        val macro1 = (children[0] as NasmStructureViewElement).value
        val macro2 = (children[1] as NasmStructureViewElement).value
        val section = (children[2] as NasmStructureViewElement).value

        assertTrue("First element should be a macro", macro1 is NasmMultiLineMacro)
        assertTrue("Second element should be a macro", macro2 is NasmMultiLineMacro)
        assertTrue("Third element should be a section", section is NasmSectionDir)

        assertEquals("First macro should be 'SAVE_REGS'", "SAVE_REGS",
            (macro1 as NasmNamedElement).name)
        assertEquals("Second macro should be 'RESTORE_REGS'", "RESTORE_REGS",
            (macro2 as NasmNamedElement).name)
    }

    fun testStructureViewShowsEquConstants() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            BUFFER_SIZE equ 1024
            MAX_COUNT equ 256

            section .bss
                buffer resb BUFFER_SIZE
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children

        // First two should be EQU constants, third should be section
        assertTrue("Should have at least 3 elements", children.size >= 3)

        val equ1 = (children[0] as NasmStructureViewElement).value
        val equ2 = (children[1] as NasmStructureViewElement).value

        assertTrue("First element should be an EQU definition", equ1 is NasmEquDefinition)
        assertTrue("Second element should be an EQU definition", equ2 is NasmEquDefinition)

        assertEquals("First constant should be 'BUFFER_SIZE'", "BUFFER_SIZE",
            (equ1 as NasmNamedElement).name)
        assertEquals("Second constant should be 'MAX_COUNT'", "MAX_COUNT",
            (equ2 as NasmNamedElement).name)
    }

    fun testStructureViewWithMultipleSections() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .data
                msg db "Hello", 0

            section .bss
                buffer resb 1024

            section .text
            global main
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children
        assertEquals("Should have 3 sections", 3, children.size)

        val section1 = (children[0] as NasmStructureViewElement).value as NasmSectionDir
        val section2 = (children[1] as NasmStructureViewElement).value as NasmSectionDir
        val section3 = (children[2] as NasmStructureViewElement).value as NasmSectionDir

        // Verify sections are in file order
        assertTrue("First section should be .data",
            section1.text.contains(".data"))
        assertTrue("Second section should be .bss",
            section2.text.contains(".bss"))
        assertTrue("Third section should be .text",
            section3.text.contains(".text"))
    }

    fun testStructureViewWithFileWithoutSections() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro TEST 0
                nop
            %endmacro

            CONSTANT equ 42

            global start
            start:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children

        // Should show macro, constant, and label at top level
        assertEquals("Should have 3 top-level elements", 3, children.size)

        val macro = (children[0] as NasmStructureViewElement).value
        val constant = (children[1] as NasmStructureViewElement).value
        val label = (children[2] as NasmStructureViewElement).value

        assertTrue("First element should be macro", macro is NasmMultiLineMacro)
        assertTrue("Second element should be constant", constant is NasmEquDefinition)
        assertTrue("Third element should be label", label is NasmLabelDef)
    }

    fun testPrivateLabelIsAlwaysLeaf() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                ret

            .private:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null) as NasmStructureViewModel

        val section = model.root.children[0]
        val publicLabel = section.children[0]
        val privateLabel = publicLabel.children[0]

        // Private labels should always be leaves
        assertTrue("Private label should be a leaf",
            model.isAlwaysLeaf(privateLabel as com.intellij.ide.structureView.StructureViewTreeElement))
    }

    fun testMacroIsAlwaysLeaf() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %macro TEST 0
                nop
            %endmacro
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null) as NasmStructureViewModel

        val macro = model.root.children[0]

        assertTrue("Macro should be a leaf",
            model.isAlwaysLeaf(macro as com.intellij.ide.structureView.StructureViewTreeElement))
    }

    fun testEquDefinitionIsAlwaysLeaf() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            CONSTANT equ 42
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null) as NasmStructureViewModel

        val equ = model.root.children[0]

        assertTrue("EQU definition should be a leaf",
            model.isAlwaysLeaf(equ as com.intellij.ide.structureView.StructureViewTreeElement))
    }

    fun testNavigationFromStructureView() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            global main
            main:
                call helper
                ret

            helper:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(myFixture.editor)
        val root = model.root

        val section = root.children[0]
        val mainLabel = section.children[0] as NasmStructureViewElement

        // Test that we can navigate to the element
        assertTrue("Should be able to navigate", mainLabel.canNavigate())
        assertTrue("Should be able to navigate to source", mainLabel.canNavigateToSource())
    }

    fun testStructureViewElementPresentation() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val section = root.children[0]
        val label = section.children[0]

        val presentation = label.presentation
        assertNotNull("Label should have presentation", presentation)

        val presentedText = presentation.presentableText
        assertNotNull("Presentation should have text", presentedText)
    }

    fun testStructureViewWithMacrosInSection() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text

            %macro HELPER 0
                nop
            %endmacro

            main:
                HELPER
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val section = root.children[0]
        val sectionChildren = section.children

        // Section should contain macro and label
        assertEquals("Section should have 2 children", 2, sectionChildren.size)

        val macro = (sectionChildren[0] as NasmStructureViewElement).value
        val label = (sectionChildren[1] as NasmStructureViewElement).value

        assertTrue("First child should be macro", macro is NasmMultiLineMacro)
        assertTrue("Second child should be label", label is NasmLabelDef)
    }

    fun testStructureViewAlphaSortKey() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            zebra:
                ret

            apple:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val section = root.children[0]
        val label1 = section.children[0] as NasmStructureViewElement
        val label2 = section.children[1] as NasmStructureViewElement

        // Test that alphaSortKey returns the element name
        assertEquals("First label sort key should be 'zebra'", "zebra", label1.alphaSortKey)
        assertEquals("Second label sort key should be 'apple'", "apple", label2.alphaSortKey)
    }

    fun testStructureViewFileOrder() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            section .text
            first:
                ret

            second:
                ret

            third:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val section = root.children[0]
        val labels = section.children

        assertEquals("Should have 3 labels", 3, labels.size)

        // Verify they're in file order, not alphabetical
        assertEquals("First label should be 'first'", "first",
            ((labels[0] as NasmStructureViewElement).value as NasmNamedElement).name)
        assertEquals("Second label should be 'second'", "second",
            ((labels[1] as NasmStructureViewElement).value as NasmNamedElement).name)
        assertEquals("Third label should be 'third'", "third",
            ((labels[2] as NasmStructureViewElement).value as NasmNamedElement).name)
    }

    fun testStructureViewWithDefineStatements() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %define VERSION 1
            %define AUTHOR "test"

            section .text
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children

        // Should have defines before section
        assertTrue("Should have at least 3 elements", children.size >= 3)

        val define1 = (children[0] as NasmStructureViewElement).value
        val define2 = (children[1] as NasmStructureViewElement).value
        val section = (children[2] as NasmStructureViewElement).value

        assertTrue("First element should be %define", define1 is NasmPpDefineStmt)
        assertTrue("Second element should be %define", define2 is NasmPpDefineStmt)
        assertTrue("Third element should be section", section is NasmSectionDir)
    }

    fun testStructureViewWithAssignStatements() {
        val file = myFixture.configureByText(
            "test.asm",
            """
            %assign counter 0
            %assign limit 100

            section .text
            main:
                ret
            """.trimIndent()
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children

        // Should have assigns before section
        assertTrue("Should have at least 3 elements", children.size >= 3)

        val assign1 = (children[0] as NasmStructureViewElement).value
        val assign2 = (children[1] as NasmStructureViewElement).value
        val section = (children[2] as NasmStructureViewElement).value

        assertTrue("First element should be %assign", assign1 is NasmPpAssignStmt)
        assertTrue("Second element should be %assign", assign2 is NasmPpAssignStmt)
        assertTrue("Third element should be section", section is NasmSectionDir)
    }

    fun testStructureViewEmptyFile() {
        val file = myFixture.configureByText(
            "test.asm",
            ""
        )

        val factory = NasmStructureViewFactory()
        val builder = factory.getStructureViewBuilder(file) as TreeBasedStructureViewBuilder
        val model = builder.createStructureViewModel(null)
        val root = model.root

        val children = root.children
        assertEquals("Empty file should have no children", 0, children.size)
    }
}
