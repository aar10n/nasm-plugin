package dev.agb.nasmplugin.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.psi.NasmFile

/**
 * Handles typed characters to trigger auto-completion popup for NASM-specific triggers
 */
class NasmTypedHandler : TypedHandlerDelegate() {

    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is NasmFile) {
            return Result.CONTINUE
        }

        // Trigger auto-popup when '%' is typed (for preprocessor directives)
        if (charTyped == '%') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }
}
