package dev.agb.nasmplugin.clion.projectmodel.analyzers

import com.intellij.openapi.project.Project
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectCompilationInfo

/**
 * Interface for analyzing different project models (CMake, Makefile, etc.)
 * to extract NASM compilation information.
 */
interface NasmProjectModelAnalyzer {
    /**
     * Check if this analyzer can handle the given project.
     * Multiple analyzers may return true; the first applicable one will be used.
     */
    fun isApplicable(project: Project): Boolean

    /**
     * Analyze the project and extract NASM compilation information.
     * This may involve:
     * - Parsing compilation databases
     * - Running build system commands (make -nB, etc.)
     * - Reading build configuration files
     *
     * Returns null if analysis fails or no NASM files are found.
     */
    fun analyze(project: Project): NasmProjectCompilationInfo?

    /**
     * Get a human-readable name for this project model type.
     * Used for logging and debugging.
     */
    fun getDisplayName(): String
}
