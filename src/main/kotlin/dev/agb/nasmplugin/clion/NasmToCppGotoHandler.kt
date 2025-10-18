package dev.agb.nasmplugin.clion

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.NasmExternDir
import dev.agb.nasmplugin.psi.NasmSymbolDecl
import dev.agb.nasmplugin.psi.NasmTypes

/**
 * Enhanced goto declaration handler for NASM extern symbols in CLion.
 * Provides direct navigation from extern declarations to NASM global labels and C/C++ code.
 */
class NasmToCppGotoHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        sourceElement ?: return null

        // Only handle IDENTIFIER tokens
        if (sourceElement.node?.elementType != NasmTypes.IDENTIFIER) {
            return null
        }

        // Check if this identifier is part of a symbol declaration
        val symbolDecl = PsiTreeUtil.getParentOfType(sourceElement, NasmSymbolDecl::class.java)
            ?: return null

        // Only handle extern declarations (not global declarations)
        val externDir = PsiTreeUtil.getParentOfType(symbolDecl, NasmExternDir::class.java)
            ?: return null

        // The symbol's reference already handles resolution to both NASM global labels
        // and C/C++ symbols, so we just need to use it
        val resolvedTarget = symbolDecl.reference?.resolve()

        if (resolvedTarget != null) {
            return arrayOf(resolvedTarget)
        }

        return null
    }
}
