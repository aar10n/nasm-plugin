package dev.agb.nasmplugin.clion

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.cidr.lang.psi.OCDeclarator
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.psi.NasmFile
import dev.agb.nasmplugin.psi.NasmLabelDef

/**
 * Provides navigation from C/C++ extern declarations to NASM label definitions.
 */
class CppToNasmGotoHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        sourceElement ?: return null

        // Only handle C/C++ declarators
        val declarator = sourceElement.parent as? OCDeclarator
            ?: PsiTreeUtil.getParentOfType(sourceElement, OCDeclarator::class.java)
            ?: return null

        // Check if this is an extern declaration (has no body)
        if (!isExternDeclaration(declarator)) {
            return null
        }

        val symbolName = declarator.name

        // Search for matching NASM label definitions
        val nasmLabels = findNasmGlobalLabels(symbolName, declarator.project)

        if (nasmLabels.isNotEmpty()) {
            return nasmLabels.toTypedArray()
        }

        return null
    }

    /**
     * Checks if a C/C++ declarator is an extern declaration.
     *
     * In CIDR PSI, extern declarations typically don't have a definition body.
     * This heuristic checks if the declarator has no associated definition.
     */
    private fun isExternDeclaration(declarator: OCDeclarator): Boolean {
        // Check the declaration text for 'extern' keyword
        val declarationText = declarator.text
        if (declarationText.contains("extern")) {
            return true
        }

        // Additional heuristic: if it's a function declarator without a body
        // (ends with semicolon in the containing statement)
        val parent = declarator.parent
        return parent?.text?.trimEnd()?.endsWith(";") == true
    }

    /**
     * Searches all NASM files in the project for global labels matching the symbol name.
     *
     * @param symbolName The name of the label to find
     * @param project The current project
     * @return List of NASM label definitions matching the symbol name
     */
    private fun findNasmGlobalLabels(symbolName: String, project: com.intellij.openapi.project.Project): List<PsiElement> {
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val results = mutableListOf<PsiElement>()

        try {
            // Get all NASM files in the project
            val nasmFiles = FileTypeIndex.getFiles(NasmFileType, scope)

            for (virtualFile in nasmFiles) {
                val nasmFile = psiManager.findFile(virtualFile) as? NasmFile ?: continue

                // Find all label definitions in the file
                val labelDefs = PsiTreeUtil.findChildrenOfType(nasmFile, NasmLabelDef::class.java)

                for (labelDef in labelDefs) {
                    // Check if this is a global label with matching name
                    val globalLabel = labelDef.globalLabel
                    if (globalLabel != null && labelDef.name == symbolName) {
                        results.add(labelDef)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently ignore - likely running in non-CLion IDE without CIDR support
        }

        return results
    }
}
