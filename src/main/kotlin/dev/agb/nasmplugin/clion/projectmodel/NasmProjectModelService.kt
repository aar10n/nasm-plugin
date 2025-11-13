package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.agb.nasmplugin.clion.projectmodel.analyzers.NasmProjectModelAnalyzer
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Project-level service that manages NASM compilation information.
 * This service:
 * - Detects the project model type (CMake, Makefile, etc.)
 * - Analyzes the project to extract NASM compilation info
 * - Provides access to compilation info for NASM files
 * - Handles re-indexing when project structure changes
 */
@Service(Service.Level.PROJECT)
class NasmProjectModelService(private val project: Project) {
    private val LOG = Logger.getInstance(NasmProjectModelService::class.java)

    private val lock = ReentrantReadWriteLock()
    private var compilationInfo: NasmProjectCompilationInfo? = null
    private var currentAnalyzer: NasmProjectModelAnalyzer? = null

    companion object {
        /**
         * Get the service instance for a project.
         */
        fun getInstance(project: Project): NasmProjectModelService = project.service()
    }

    /**
     * Analyze the project and update compilation info.
     * This should be called:
     * - On project open
     * - When the project structure changes
     * - When build configuration changes
     */
    fun analyzeProject() {
        LOG.warn("NASM: analyzeProject() called for project ${project.name}")

        val analyzer = selectAnalyzer()
        if (analyzer == null) {
            LOG.warn("NASM: No applicable project model analyzer found for project ${project.name}")
            lock.write {
                compilationInfo = null
                currentAnalyzer = null
            }
            return
        }

        LOG.warn("NASM: Using ${analyzer.getDisplayName()} analyzer for project ${project.name}")

        val info = try {
            analyzer.analyze(project)
        } catch (e: Exception) {
            LOG.warn("NASM: Failed to analyze project with ${analyzer.getDisplayName()} analyzer", e)
            null
        }

        lock.write {
            compilationInfo = info
            currentAnalyzer = analyzer
        }

        if (info != null) {
            LOG.warn("NASM: Successfully analyzed project: found ${info.compiledFiles.size} NASM files")
            info.compiledFiles.forEach { file ->
                val compInfo = info.getCompilationInfo(file)
                LOG.warn("NASM:   ${file.name}: ${compInfo?.compilerArguments?.size ?: 0} args, ${compInfo?.includePaths?.size ?: 0} includes, ${compInfo?.macroDefinitions?.size ?: 0} macros")
            }
        } else {
            LOG.warn("NASM: No NASM files found in project")
        }
    }

    /**
     * Get compilation info for a specific NASM file.
     */
    fun getCompilationInfo(file: VirtualFile): NasmCompilationInfo? {
        return lock.read {
            compilationInfo?.getCompilationInfo(file)
        }
    }

    /**
     * Get all NASM files that are compiled in this project.
     */
    fun getAllCompiledFiles(): Set<VirtualFile> {
        return lock.read {
            compilationInfo?.compiledFiles ?: emptySet()
        }
    }

    /**
     * Get the current compilation info for the entire project.
     */
    fun getProjectCompilationInfo(): NasmProjectCompilationInfo? {
        return lock.read {
            compilationInfo
        }
    }

    /**
     * Get the name of the current project model type.
     */
    fun getProjectModelType(): String? {
        return lock.read {
            currentAnalyzer?.getDisplayName()
        }
    }

    /**
     * Check if the project has been analyzed.
     */
    fun isAnalyzed(): Boolean {
        return lock.read {
            compilationInfo != null
        }
    }

    /**
     * Select the appropriate project model analyzer for this project.
     * Uses CLion's CidrWorkspaceManager to detect the active project model type.
     * Falls back to file-based detection if no workspace is initialized.
     */
    private fun selectAnalyzer(): NasmProjectModelAnalyzer? {
        // First, try to detect project model using CLion's workspace system
        val workspaceAnalyzer = selectAnalyzerByWorkspace()
        if (workspaceAnalyzer != null) {
            return workspaceAnalyzer
        }

        // Fall back to file-based detection if no workspace is found
        // This handles cases where CLion hasn't initialized workspaces yet
        return selectAnalyzerByFileDetection()
    }

    /**
     * Detect project model using CLion's CidrWorkspaceManager.
     * This is the preferred method as it uses CLion's own project model detection.
     */
    private fun selectAnalyzerByWorkspace(): NasmProjectModelAnalyzer? {
        try {
            val workspaceManager = com.jetbrains.cidr.project.workspace.CidrWorkspaceManager.getInstance(project)

            // Check if workspace models are ready
            if (!workspaceManager.modelsAreReady) {
                LOG.debug("CLion workspace models are not ready yet")
                return null
            }

            // Get all initialized workspaces
            val workspaces = workspaceManager.initializedWorkspaces

            // Detect workspace type by class name (to avoid compile-time dependency on CMake/Makefile plugins)
            for (workspace in workspaces) {
                val className = workspace.javaClass.name
                when {
                    className.contains("CMakeWorkspace") -> {
                        LOG.info("Detected CMake workspace via CLion SDK")
                        return dev.agb.nasmplugin.clion.projectmodel.analyzers.CMakeProjectAnalyzer()
                    }
                    className.contains("MakefileWorkspace") -> {
                        LOG.info("Detected Makefile workspace via CLion SDK")
                        return dev.agb.nasmplugin.clion.projectmodel.analyzers.MakefileProjectAnalyzer()
                    }
                }
            }
        } catch (e: Exception) {
            LOG.debug("Failed to detect workspace via CLion SDK (this is expected in tests)", e)
        }

        return null
    }

    /**
     * Fall back to file-based detection when CLion workspace is not available.
     * This is used in tests and during early project initialization.
     */
    private fun selectAnalyzerByFileDetection(): NasmProjectModelAnalyzer? {
        val analyzers = listOf(
            dev.agb.nasmplugin.clion.projectmodel.analyzers.CMakeProjectAnalyzer(),
            dev.agb.nasmplugin.clion.projectmodel.analyzers.MakefileProjectAnalyzer(),
            dev.agb.nasmplugin.clion.projectmodel.analyzers.CompilationDatabaseProjectAnalyzer()
        )

        for (analyzer in analyzers) {
            try {
                if (analyzer.isApplicable(project)) {
                    LOG.info("Detected ${analyzer.getDisplayName()} project via file-based detection")
                    return analyzer
                }
            } catch (e: Exception) {
                LOG.warn("Error checking applicability of ${analyzer.javaClass.simpleName}", e)
            }
        }

        return null
    }
}
