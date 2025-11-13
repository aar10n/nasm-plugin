package dev.agb.nasmplugin.clion.projectmodel.analyzers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import dev.agb.nasmplugin.clion.projectmodel.CompilationDatabaseParser
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectCompilationInfo
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Analyzer for pure compilation database projects (not CMake or Makefile).
 * Looks for compile_commands.json directly in the project root or common locations.
 */
class CompilationDatabaseProjectAnalyzer : NasmProjectModelAnalyzer {
    private val LOG = Logger.getInstance(CompilationDatabaseProjectAnalyzer::class.java)

    override fun isApplicable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val baseDir = File(basePath)

        // This analyzer applies if we find compile_commands.json but it's NOT a CMake project
        // (CMakeProjectAnalyzer should handle CMake projects)
        return findCompileCommands(baseDir) != null && !isCMakeProject(baseDir)
    }

    override fun analyze(project: Project): NasmProjectCompilationInfo? {
        val basePath = project.basePath ?: run {
            LOG.warn("Project base path is null")
            return null
        }

        val compileCommandsPath = findCompileCommands(File(basePath)) ?: run {
            LOG.debug("No compile_commands.json found in project at $basePath")
            return null
        }

        LOG.info("Found compilation database: $compileCommandsPath")
        return CompilationDatabaseParser.parse(compileCommandsPath, project)
    }

    override fun getDisplayName(): String = "Compilation Database"

    /**
     * Find compile_commands.json in the project directory.
     * Searches in common locations.
     */
    private fun findCompileCommands(baseDir: File): Path? {
        val searchLocations = listOf(
            "compile_commands.json",
            "build/compile_commands.json",
            ".build/compile_commands.json",
            "out/compile_commands.json"
        )

        for (location in searchLocations) {
            val path = baseDir.resolve(location).toPath()
            if (path.exists()) {
                return path
            }
        }

        return null
    }

    /**
     * Check if this is a CMake project (to avoid conflict with CMakeProjectAnalyzer).
     */
    private fun isCMakeProject(baseDir: File): Boolean {
        // Check for CMakeLists.txt
        if (File(baseDir, "CMakeLists.txt").exists()) {
            return true
        }

        // Check for cmake-build-* directories
        val cmakeBuildDirs = baseDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("cmake-build-")
        }

        return !cmakeBuildDirs.isNullOrEmpty()
    }
}
