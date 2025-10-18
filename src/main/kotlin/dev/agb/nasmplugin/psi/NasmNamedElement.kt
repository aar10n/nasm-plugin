package dev.agb.nasmplugin.psi

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiNameIdentifierOwner

/**
 * Interface for named elements in NASM (labels, macros, etc.)
 */
interface NasmNamedElement : PsiNameIdentifierOwner {
    fun getPresentation(): ItemPresentation
}
