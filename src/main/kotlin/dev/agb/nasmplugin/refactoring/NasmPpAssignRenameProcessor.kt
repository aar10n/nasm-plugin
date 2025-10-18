package dev.agb.nasmplugin.refactoring

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import dev.agb.nasmplugin.psi.NasmNamedElement
import dev.agb.nasmplugin.psi.NasmPpAssignStmt
import dev.agb.nasmplugin.psi.findPpAssignStmts

/**
 * Custom rename processor for %assign statements.
 * This handles the special case where %assign allows reassignment to the same variable name.
 * When renaming a variable, we need to rename both references AND subsequent %assign statements
 * with the same name (which are technically redefinitions).
 */
class NasmPpAssignRenameProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is NasmPpAssignStmt
    }

    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        super.prepareRenaming(element, newName, allRenames)

        // For %assign statements, also rename other %assign statements with the same name
        // These are technically redefinitions, but should be renamed together
        if (element is NasmPpAssignStmt) {
            val name = element.name ?: return
            val containingFile = element.containingFile ?: return

            // Find all %assign statements with the same name in the file
            containingFile.findPpAssignStmts().forEach { assignStmt ->
                // Include other PpAssignStmt elements with the same name
                val assignName = (assignStmt as? NasmNamedElement)?.name
                if (assignName != null && assignName.equals(name, ignoreCase = true) && assignStmt != element) {
                    // Add this element to be renamed as well
                    allRenames[assignStmt] = newName
                }
            }
        }
    }
}
