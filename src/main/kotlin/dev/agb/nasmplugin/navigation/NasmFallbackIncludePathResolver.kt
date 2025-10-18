package dev.agb.nasmplugin.navigation

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.agb.nasmplugin.settings.NasmProjectSettings
import java.nio.file.Paths

/**
 * Include path resolver that uses manually configured include paths from project settings.
 *
 * This service is registered as the implementation of NasmIncludePathResolver in plugin.xml.
 */
@Service(Service.Level.PROJECT)
class NasmFallbackIncludePathResolver(private val project: Project) : NasmIncludePathResolver {

    private val settings: NasmProjectSettings
        get() = NasmProjectSettings.getInstance(project)

    /**
     * Get NASM include paths from project settings.
     * Resolves relative paths against the project base path.
     *
     * @param file the file to get include paths for (unused in fallback resolver)
     * @return list of absolute include paths from project settings
     */
    override fun getNasmIncludePaths(file: VirtualFile): List<String> {
        val basePath = project.basePath ?: return emptyList()
        val basePathObj = Paths.get(basePath)

        return settings.includePaths.map { path ->
            val pathObj = Paths.get(path)
            when {
                pathObj.isAbsolute -> pathObj.toString()
                else -> basePathObj.resolve(pathObj).normalize().toString()
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NasmFallbackIncludePathResolver::class.java)

        @JvmStatic
        fun getInstance(project: Project): NasmFallbackIncludePathResolver {
            return project.getService(NasmFallbackIncludePathResolver::class.java)
        }
    }
}
