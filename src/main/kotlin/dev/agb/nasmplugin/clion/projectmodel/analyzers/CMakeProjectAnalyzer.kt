package dev.agb.nasmplugin.clion.projectmodel.analyzers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import dev.agb.nasmplugin.clion.projectmodel.MakeOutputParser
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectCompilationInfo
import java.io.File

/**
 * Analyzer for CMake-based projects.
 * we run `make -nB` on the CMake-generated Makefile to extract compilation commands.
 */
class CMakeProjectAnalyzer : NasmProjectModelAnalyzer {
    private val LOG = Logger.getInstance(CMakeProjectAnalyzer::class.java)

    override fun isApplicable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val baseDir = File(basePath)

        // Check for CMakeLists.txt in project root
        if (File(baseDir, "CMakeLists.txt").exists()) {
            return true
        }

        // Check for cmake-build-* directories
        val cmakeBuildDirs = baseDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("cmake-build-")
        }

        return !cmakeBuildDirs.isNullOrEmpty()
    }

    override fun analyze(project: Project): NasmProjectCompilationInfo? {
        val basePath = project.basePath ?: run {
            LOG.warn("Project base path is null")
            return null
        }

        // Find the CMake build directory with a Makefile
        val buildDir = findBuildDirectory(basePath) ?: run {
            LOG.debug("No CMake build directory with Makefile found at $basePath")
            return null
        }

        LOG.info("Using CMake build directory: $buildDir")

        // Run make -nB in the build directory to get compilation commands
        val makeOutput = MakeOutputParser.runMakeDryRun(buildDir.absolutePath) ?: run {
            LOG.warn("Failed to run make -nB in CMake build directory: $buildDir")
            return null
        }

        val compilations = MakeOutputParser.parseMakeOutput(makeOutput, basePath, project)

        return if (compilations.isEmpty()) {
            LOG.debug("No NASM compilation commands found in CMake project")
            null
        } else {
            LOG.info("Found ${compilations.size} NASM compilation commands from CMake project")
            NasmProjectCompilationInfo(compilations)
        }
    }

    override fun getDisplayName(): String = "CMake"

    /**
     * Find the CMake build directory containing a Makefile.
     * Searches in priority order: cmake-build-debug, cmake-build-release, other cmake-build-*, build.
     */
    private fun findBuildDirectory(basePath: String): File? {
        val baseDir = File(basePath)

        // Priority order for build directories
        val buildDirNames = listOf(
            "cmake-build-debug",
            "cmake-build-release",
            "cmake-build-relwithdebinfo",
            "build"
        )

        // Check priority directories first
        for (buildDirName in buildDirNames) {
            val dir = baseDir.resolve(buildDirName)
            if (dir.exists() && dir.isDirectory && File(dir, "Makefile").exists()) {
                return dir
            }
        }

        // Check any other cmake-build-* directory
        val cmakeBuildDirs = baseDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("cmake-build-")
        } ?: emptyArray()

        for (dir in cmakeBuildDirs) {
            if (File(dir, "Makefile").exists()) {
                return dir
            }
        }

        return null
    }
}
