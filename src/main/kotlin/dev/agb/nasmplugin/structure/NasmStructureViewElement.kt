package dev.agb.nasmplugin.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.NasmElementPresentation

/**
 * Structure view element for NASM files.
 * Handles the tree structure including sections, macros, and labels with proper nesting.
 */
class NasmStructureViewElement(private val element: PsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? NavigationItem)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean =
        (element as? NavigationItem)?.canNavigate() == true

    override fun canNavigateToSource(): Boolean =
        (element as? NavigationItem)?.canNavigateToSource() == true

    override fun getAlphaSortKey(): String =
        (element as? NasmNamedElement)?.name ?: ""

    override fun getPresentation(): ItemPresentation {
        if (element is NasmNamedElement) {
            element.getPresentation()?.let { return it }
        }
        return NasmElementPresentation(element)
    }

    override fun getChildren(): Array<TreeElement> = when (element) {
        is PsiFile -> buildFileChildren(element)
        is NasmLabelDef -> buildLabelChildren(element)
        is NasmSectionDir -> buildSectionChildren(element)
        else -> EMPTY_ARRAY
    }

    /**
     * Build children for the root file element.
     * Shows sections with nested macros and labels, plus any elements before the first section.
     * If there are no sections, shows all elements at the top level.
     */
    private fun buildFileChildren(file: PsiFile): Array<TreeElement> {
        // Find all section directives and sort by offset
        val sections = PsiTreeUtil.findChildrenOfType(file, NasmSectionDir::class.java)
            .sortedBy { it.textOffset }

        // Find all named elements and sort by text offset to maintain file order
        val namedElements = NasmPsiUtil.findAllNamedElements(file)
            .sortedBy { it.textOffset }

        // If there are no sections, show all top-level elements
        if (sections.isEmpty()) {
            return buildListForNoSections(namedElements)
        }

        // Group elements: those before first section go at top level, others under their section
        val firstSectionOffset = sections.firstOrNull()?.textOffset ?: Int.MAX_VALUE

        return buildList {
            // Add elements before the first section (macros, externs at file start)
            namedElements
                .filter { it.textOffset < firstSectionOffset }
                .filter { it is NasmMultiLineMacro || it is NasmPpDefineStmt ||
                         it is NasmPpAssignStmt || it is NasmEquDefinition }
                .mapTo(this) { NasmStructureViewElement(it) }

            // Add sections (they will contain their children)
            sections.mapTo(this) { NasmStructureViewElement(it) }
        }.toTypedArray()
    }

    /**
     * Build children for files without sections.
     * Shows all macros, defines, equs, and public labels with nested private labels.
     */
    private fun buildListForNoSections(namedElements: List<NasmNamedElement>): Array<TreeElement> {
        val publicLabels = mutableMapOf<String, NasmLabelDef>()
        val otherElements = mutableListOf<NasmNamedElement>()

        namedElements.forEach { element ->
            when (element) {
                is NasmLabelDef -> {
                    element.name?.let { name ->
                        if (!name.startsWith(".")) {
                            // Public label - collect it
                            publicLabels[name] = element
                        }
                        // Private labels will be nested under public labels
                    }
                }
                is NasmEquDefinition, is NasmPpAssignStmt, is NasmPpDefineStmt,
                is NasmMultiLineMacro -> {
                    // These elements go at the top level
                    otherElements.add(element)
                }
            }
        }

        return buildList {
            // Add other elements first
            otherElements.mapTo(this) { NasmStructureViewElement(it) }

            // Add public labels (they will contain nested private labels)
            publicLabels.values.mapTo(this) { NasmStructureViewElement(it) }
        }.toTypedArray()
    }

    /**
     * Build children for a section directive.
     * Shows labels and macros within that section.
     */
    private fun buildSectionChildren(section: NasmSectionDir): Array<TreeElement> {
        val file = section.containingFile ?: return EMPTY_ARRAY

        // Find all sections to determine the range of this section
        val sections = PsiTreeUtil.findChildrenOfType(file, NasmSectionDir::class.java)
            .sortedBy { it.textOffset }

        // Find the range for this section (from this section to the next section or end of file)
        val sectionStart = section.textOffset
        val sectionEnd = sections.zipWithNext()
            .firstOrNull { it.first == section }
            ?.second?.textOffset
            ?: Int.MAX_VALUE

        // Find all named elements in this section's range
        val namedElements = NasmPsiUtil.findAllNamedElements(file)

        // Collect public labels and other elements in this section
        val publicLabels = mutableMapOf<String, NasmLabelDef>()
        val otherElements = mutableListOf<NasmNamedElement>()

        namedElements
            .filter { it.textOffset in (sectionStart + 1) until sectionEnd }
            .forEach { element ->
                when (element) {
                    is NasmLabelDef -> {
                        element.name?.let { name ->
                            if (!name.startsWith(".")) {
                                // Public label - collect it
                                publicLabels[name] = element
                            }
                            // Private labels will be nested under public labels
                        }
                    }
                    is NasmEquDefinition, is NasmPpAssignStmt, is NasmPpDefineStmt,
                    is NasmMultiLineMacro -> {
                        // These elements go directly in the section
                        otherElements.add(element)
                    }
                }
            }

        return buildList {
            // Add other elements first
            otherElements.mapTo(this) { NasmStructureViewElement(it) }

            // Add public labels (they will contain nested private labels)
            publicLabels.values.mapTo(this) { NasmStructureViewElement(it) }
        }.toTypedArray()
    }

    /**
     * Build children for a public label.
     * Shows private labels (starting with .) that come after this public label.
     */
    private fun buildLabelChildren(publicLabel: NasmLabelDef): Array<TreeElement> {
        val publicName = publicLabel.name
        if (publicName == null || publicName.startsWith(".")) {
            return EMPTY_ARRAY
        }

        val file = publicLabel.containingFile ?: return EMPTY_ARRAY
        val allLabels = NasmPsiUtil.findLabels(file)

        // Find the index of this public label
        val publicIndex = allLabels.indexOf(publicLabel)
        if (publicIndex == -1) {
            return EMPTY_ARRAY
        }

        // Collect private labels that come after this public label
        // Stop when we hit another public label
        return allLabels
            .drop(publicIndex + 1)
            .takeWhile { it.name?.startsWith(".") == true }
            .map { NasmStructureViewElement(it) }
            .toTypedArray()
    }


    companion object {
        private val EMPTY_ARRAY = emptyArray<TreeElement>()
    }
}
