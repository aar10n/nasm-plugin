package dev.agb.nasmplugin.clion.projectmodel.analyzers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import dev.agb.nasmplugin.clion.projectmodel.MakeOutputParser
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectCompilationInfo
import java.io.File

/**
 * Analyzer for Makefile-based projects.
 * Runs `make -nB` to extract compilation commands and parses them for NASM files.
 */
class MakefileProjectAnalyzer : NasmProjectModelAnalyzer {
    private val LOG = Logger.getInstance(MakefileProjectAnalyzer::class.java)

    override fun isApplicable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val baseDir = File(basePath)

        // Check for common Makefile names
        val makefileNames = listOf("Makefile", "makefile", "GNUmakefile")
        return makefileNames.any { File(baseDir, it).exists() }
    }

    override fun analyze(project: Project): NasmProjectCompilationInfo? {
        val basePath = project.basePath ?: run {
            LOG.warn("Project base path is null")
            return null
        }

        val makeOutput = MakeOutputParser.runMakeDryRun(basePath) ?: run {
            LOG.warn("Failed to run make -nB in $basePath")
            return null
        }

        val compilations = MakeOutputParser.parseMakeOutput(makeOutput, basePath, project)

        return if (compilations.isEmpty()) {
            LOG.debug("No NASM compilation commands found in make output")
            null
        } else {
            LOG.info("Found ${compilations.size} NASM compilation commands from Makefile")
            NasmProjectCompilationInfo(compilations)
        }
    }

    override fun getDisplayName(): String = "Makefile"
}
