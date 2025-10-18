package dev.agb.nasmplugin.structure

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.psi.*

/**
 * Structure view model for NASM assembly files.
 * Provides the tree structure showing sections, macros, and labels.
 */
class NasmStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, NasmStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {

    override fun getSorters(): Array<Sorter> =
        Sorter.EMPTY_ARRAY // Disable alphabetical sorting to maintain file order

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return when (val value = element.value) {
            is NasmLabelDef -> {
                // Private labels are leaves
                val name = value.name
                name != null && name.startsWith(".")
            }
            is NasmMultiLineMacro, is NasmEquDefinition,
            is NasmPpAssignStmt, is NasmPpDefineStmt -> true
            else -> false
        }
    }
}
