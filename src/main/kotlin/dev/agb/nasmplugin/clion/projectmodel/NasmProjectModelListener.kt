package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Startup activity that analyzes the project on opening.
 */
class NasmProjectStartupActivity : ProjectActivity {
    private val LOG = Logger.getInstance(NasmProjectStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        LOG.info("Analyzing NASM project model for ${project.name}")
        val service = NasmProjectModelService.getInstance(project)
        service.analyzeProject()
    }
}
