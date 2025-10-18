package dev.agb.nasmplugin.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.NasmPpDefineStmt
import dev.agb.nasmplugin.psi.NasmPpAssignStmt

/**
 * Common PSI utility functions for NASM elements.
 */
object NasmPsiUtils {

    /**
     * Checks if this element is a child of the given ancestor.
     */
    fun PsiElement.isChildOf(ancestor: PsiElement): Boolean {
        var current: PsiElement? = this
        while (current != null) {
            if (current == ancestor) return true
            current = current.parent
        }
        return false
    }

    /**
     * Finds the first parent of the specified type.
     */
    inline fun <reified T : PsiElement> PsiElement.findParentOfType(): T? {
        return PsiTreeUtil.getParentOfType(this, T::class.java)
    }

    /**
     * Checks if an element is within a macro definition body.
     */
    fun PsiElement.isInMacroDefinitionBody(): Boolean {
        // Check if we're within a pp_define_stmt or pp_assign_stmt
        var parent = this.parent
        while (parent != null) {
            when (parent) {
                is NasmPpDefineStmt -> {
                    // Make sure we're in the value/body part, not the name part
                    val nameElement = parent.macroName
                    // If we're not the name element itself, we're in the body
                    return nameElement != null && !isChildOf(nameElement)
                }
                is NasmPpAssignStmt -> {
                    // Skip MACRO_ASSIGN token to get to the name
                    val nameElement = parent.firstChild?.nextSibling
                    return nameElement != null && !isChildOf(nameElement)
                }
            }
            parent = parent.parent
        }
        return false
    }

    /**
     * Gets all elements of a specific type from the element and its children.
     */
    inline fun <reified T : PsiElement> PsiElement.findAllOfType(): Collection<T> {
        return PsiTreeUtil.collectElementsOfType(this, T::class.java)
    }

    /**
     * Gets the first child element of a specific type.
     */
    inline fun <reified T : PsiElement> PsiElement.findChildOfType(): T? {
        return PsiTreeUtil.findChildOfType(this, T::class.java)
    }
}