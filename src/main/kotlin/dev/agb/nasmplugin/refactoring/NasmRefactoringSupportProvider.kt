package dev.agb.nasmplugin.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import dev.agb.nasmplugin.psi.NasmNamedElement

/**
 * Provides refactoring support for NASM elements, including rename refactoring.
 */
class NasmRefactoringSupportProvider : RefactoringSupportProvider() {

    /**
     * Determines if an element can be renamed.
     * We allow renaming for all NASM named elements (labels, macros, constants, etc.)
     */
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Check if element is a NasmNamedElement (label, macro, constant, etc.)
        if (element is NasmNamedElement) {
            // Make sure we have a name identifier
            return element.nameIdentifier != null
        }
        return false
    }

    /**
     * Determines if safe delete refactoring is available.
     * We could enable this in the future for unused labels, macros, etc.
     */
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return false // Disabled for now, can be implemented later
    }
}