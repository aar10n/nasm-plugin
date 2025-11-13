package dev.agb.nasmplugin.clion

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectModelService

/**
 * Action to manually trigger re-analysis of the NASM project model.
 * This is useful for debugging or when automatic triggers don't fire.
 */
class ReloadNasmProjectModelAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Run analysis on background thread to avoid EDT freeze
        ApplicationManager.getApplication().executeOnPooledThread {
            val service = NasmProjectModelService.getInstance(project)
            service.analyzeProject()
        }
    }
}
