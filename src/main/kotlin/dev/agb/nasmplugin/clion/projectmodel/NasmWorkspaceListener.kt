package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceListener
import com.jetbrains.cidr.project.workspace.CidrWorkspaceState

/**
 * Listens to CLion workspace changes to trigger re-analysis of NASM project model.
 * This ensures that when the user clicks "Reload CMake Project" or "Reload Makefile Project",
 * the plugin re-analyzes the compilation database and updates include paths and macros.
 */
class NasmWorkspaceListener(private val project: Project) : CidrWorkspaceListener {
    private val LOG = Logger.getInstance(NasmWorkspaceListener::class.java)

    init {
        LOG.warn("NASM: NasmWorkspaceListener initialized for project ${project.name}")
    }

    /**
     * Called when the workspace state changes (loaded, reloaded, or configuration changed).
     * This is the main trigger for re-analyzing the NASM project model.
     */
    override fun workspaceStateChanged(
        workspace: CidrWorkspace,
        oldState: CidrWorkspaceState,
        newState: CidrWorkspaceState,
        allWorkspaceStates: Map<CidrWorkspace, out CidrWorkspaceState>
    ) {
        LOG.warn("NASM: Workspace state changed from $oldState to $newState (ready=${newState.isReady})")

        // Only trigger analysis when workspace reaches Loaded state
        // This prevents multiple redundant analyses during the initialization cycle
        if (newState.toString() == "Loaded" && oldState.toString() != "Loaded") {
            LOG.warn("NASM: Workspace reached Loaded state, scheduling project analysis")

            // Run analysis on background thread to avoid EDT freeze
            ApplicationManager.getApplication().executeOnPooledThread {
                val service = NasmProjectModelService.getInstance(project)
                service.analyzeProject()
            }
        } else {
            LOG.debug("NASM: Skipping analysis for state transition $oldState -> $newState")
        }
    }

    /**
     * Called when a workspace is initialized.
     * Note: We don't analyze here because workspaceStateChanged will be called
     * when the workspace reaches the ready state.
     */
    override fun initialized(workspace: CidrWorkspace) {
        LOG.warn("NASM: Workspace initialized: ${workspace.javaClass.simpleName}")
        // No need to analyze here - workspaceStateChanged will handle it when ready
    }

    /**
     * Called when a workspace is shut down.
     */
    override fun shutdown(workspace: CidrWorkspace) {
        LOG.warn("NASM: Workspace shutdown: ${workspace.javaClass.simpleName}")
    }
}
