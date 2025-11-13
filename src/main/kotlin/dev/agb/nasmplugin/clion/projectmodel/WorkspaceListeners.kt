package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.project.workspace.CidrWorkspaceInstantaneousStateChangeListener

/**
 * Listens for CLion workspace instantaneous state changes to trigger NASM project re-analysis.
 *
 * This listener fires when "Reload Makefile Project" or "Reload CMake Project" is clicked,
 * ensuring that the NASM project model is re-analyzed to pick up updated compiler flags,
 * include paths, and macro definitions from the compilation database.
 *
 * IMPORTANT: The analysis is run asynchronously to avoid blocking the EDT.
 */
class CidrWorkspaceInstantaneousListener(private val project: Project) : CidrWorkspaceInstantaneousStateChangeListener {
    private val LOG = Logger.getInstance(CidrWorkspaceInstantaneousListener::class.java)

    override fun workspaceInstantaneousStateChanged() {
        LOG.info("NASM: Workspace instantaneous state changed, scheduling project re-analysis for ${project.name}")

        // Run analysis on background thread to avoid EDT freeze
        ApplicationManager.getApplication().executeOnPooledThread {
            val service = NasmProjectModelService.getInstance(project)
            service.analyzeProject()
        }
    }
}
