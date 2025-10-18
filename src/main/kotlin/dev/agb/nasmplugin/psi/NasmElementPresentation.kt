package dev.agb.nasmplugin.psi

import com.intellij.icons.AllIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Presentation for NASM elements in the structure view.
 * Provides icons and display text for different element types.
 */
class NasmElementPresentation(private val element: PsiElement) : ItemPresentation {

    override fun getPresentableText(): String? {
        return when (element) {
            is NasmNamedElement -> element.name
            is NasmSectionDir -> {
                // Get section name
                val sectionName = element.sectionName?.text ?: "section"
                "section $sectionName"
            }
            else -> element.text
        }
    }

    override fun getLocationString(): String? = null // Could show additional info like line number

    override fun getIcon(unused: Boolean): Icon? {
        return when (element) {
            is NasmSectionDir -> AllIcons.Nodes.Package // Package icon for sections
            is NasmMultiLineMacro -> AllIcons.Nodes.Method // Green icon with M
            is NasmPpDefineStmt -> AllIcons.Nodes.AbstractMethod // Green # icon
            is NasmLabelDef -> {
                val name = element.name
                if (name != null && name.startsWith(".")) {
                    AllIcons.Nodes.Property // Private label - smaller/lighter function icon
                } else {
                    AllIcons.Nodes.Function // Public label - red icon
                }
            }
            is NasmDataDef -> AllIcons.Nodes.Field // Orange/yellow icon for data definitions
            is NasmEquDefinition -> AllIcons.Nodes.Constant
            is NasmPpAssignStmt -> AllIcons.Nodes.Constant
            is NasmExternDir -> AllIcons.Nodes.Interface
            else -> AllIcons.Nodes.EmptyNode
        }
    }
}
