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

        // Only handle C/C++ declarators where we're clicking on the name identifier itself
        // This prevents matching when clicking on function calls or references
        val declarator = sourceElement.parent as? OCDeclarator ?: return null

        // Verify that the clicked element is actually the name identifier of this declarator
        // This ensures we're clicking on the declaration itself, not just somewhere inside it
        if (declarator.nameIdentifier != sourceElement) {
            return null
        }

        // Check if this is an extern declaration (has no body)
        // Also skip declarators with initializers
        if (!isExternDeclaration(declarator) || hasInitializer(declarator)) {
            return null
        }

        val symbolName = declarator.name ?: return null

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
     * Only matches:
     * 1. Explicit extern declarations (e.g., extern int foo;)
     * 2. Function prototypes at file scope
     *
     * Does NOT match:
     * - Local variables (with or without initializers)
     * - Struct/class members
     * - Function parameters
     * - Function definitions with bodies
     */
    private fun isExternDeclaration(declarator: OCDeclarator): Boolean {
        val parent = declarator.parent
        val declarationText = parent?.text ?: declarator.text

        // Check for explicit 'extern' keyword - this is the most reliable
        if (declarationText.contains(Regex("\\bextern\\b"))) {
            return true
        }

        // For everything else, we need to be very conservative
        // Only accept function declarations (they have parentheses in their signature)
        // and reject everything else to avoid false positives with local variables

        // Check if this is a function declarator (has parameters)
        // Function declarators contain '(' after the name
        val isFunction = try {
            declarator.text.contains("(") || declarationText.contains(Regex("\\w+\\s*\\("))
        } catch (e: Exception) {
            false
        }

        // Only proceed if it's a function
        if (!isFunction) {
            return false
        }

        // Function prototypes end with semicolon and have no body
        if (declarationText.trimEnd().endsWith(";") && !declarationText.contains("{")) {
            return true
        }

        return false
    }

    /**
     * Checks if a declarator has an initializer (assignment).
     * Uses both CIDR API and text-based heuristics for robustness.
     */
    private fun hasInitializer(declarator: OCDeclarator): Boolean {
        // Try CIDR API first - check if there's an initializer child
        try {
            val init = declarator.initializer
            if (init != null) {
                return true
            }
        } catch (e: Throwable) {
            // Silently continue to text-based check
        }

        // Fallback: check the surrounding text for '=' sign
        val parent = declarator.parent
        val text = parent?.text ?: declarator.text

        // Look for assignment after the declarator name
        val nameIdentifier = declarator.nameIdentifier
        if (nameIdentifier != null) {
            val textAfterName = text.substring(nameIdentifier.startOffsetInParent + nameIdentifier.textLength)
            if (textAfterName.contains(Regex("\\s*="))) {
                return true
            }
        }

        return false
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
