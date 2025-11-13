package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.agb.nasmplugin.navigation.NasmFallbackIncludePathResolver
import dev.agb.nasmplugin.navigation.NasmIncludePathResolver

/**
 * Include path resolver that integrates with the project model service.
 *
 * This resolver:
 * 1. Gets per-file include paths from the compilation database (if available)
 * 2. Merges them with global include paths from project settings
 * 3. Returns absolute, deduplicated paths
 *
 * This service is registered as the implementation of NasmIncludePathResolver in plugin-clion.xml,
 * taking precedence over the fallback resolver.
 */
@Service(Service.Level.PROJECT)
class ProjectModelIncludePathResolver(private val project: Project) : NasmIncludePathResolver {

    private val fallbackResolver: NasmFallbackIncludePathResolver
        get() = NasmFallbackIncludePathResolver.getInstance(project)

    /**
     * Get NASM include paths for a file by combining:
     * 1. Per-file include paths from compilation database (if available)
     * 2. Global include paths from project settings
     *
     * @param file the file to get include paths for
     * @return list of absolute include paths, deduplicated
     */
    override fun getNasmIncludePaths(file: VirtualFile): List<String> {
        // Get per-file include paths from compilation database
        val compilationInfo = NasmProjectModelService.getInstance(project).getCompilationInfo(file)
        val perFileIncludePaths = compilationInfo?.includePaths?.map { it.toString() } ?: emptyList()

        // Get global include paths from project settings
        val globalIncludePaths = fallbackResolver.getNasmIncludePaths(file)

        // Merge and deduplicate (per-file paths take precedence)
        val allPaths = (perFileIncludePaths + globalIncludePaths).distinct()

        if (LOG.isDebugEnabled && compilationInfo != null) {
            LOG.debug("Include paths for ${file.name}: per-file=${perFileIncludePaths.size}, global=${globalIncludePaths.size}, total=${allPaths.size}")
        }

        return allPaths
    }

    companion object {
        private val LOG = Logger.getInstance(ProjectModelIncludePathResolver::class.java)

        @JvmStatic
        fun getInstance(project: Project): ProjectModelIncludePathResolver {
            return project.getService(ProjectModelIncludePathResolver::class.java)
        }
    }
}
