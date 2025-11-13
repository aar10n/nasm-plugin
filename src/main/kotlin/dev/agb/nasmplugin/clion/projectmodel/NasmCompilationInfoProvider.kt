package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.nio.file.Path

/**
 * Provides compilation information for NASM files to other parts of the plugin.
 * This is the main integration point between the project model system and other features.
 */
object NasmCompilationInfoProvider {

    /**
     * Get include paths for a NASM file.
     * These paths should be searched when resolving %include directives.
     *
     * @return List of include paths, or empty list if no compilation info is available
     */
    fun getIncludePaths(file: VirtualFile, project: Project): List<Path> {
        val service = NasmProjectModelService.getInstance(project)
        val info = service.getCompilationInfo(file)
        return info?.includePaths ?: emptyList()
    }

    /**
     * Get include paths for a PSI file.
     */
    fun getIncludePaths(psiFile: PsiFile): List<Path> {
        val virtualFile = psiFile.virtualFile ?: return emptyList()
        return getIncludePaths(virtualFile, psiFile.project)
    }

    /**
     * Get macro definitions for a NASM file.
     * These macros are defined via compiler flags (-D).
     *
     * @return Map of macro name to value (null if macro is defined without a value)
     */
    fun getMacroDefinitions(file: VirtualFile, project: Project): Map<String, String?> {
        val service = NasmProjectModelService.getInstance(project)
        val info = service.getCompilationInfo(file)
        return info?.macroDefinitions ?: emptyMap()
    }

    /**
     * Get macro definitions for a PSI file.
     */
    fun getMacroDefinitions(psiFile: PsiFile): Map<String, String?> {
        val virtualFile = psiFile.virtualFile ?: return emptyMap()
        return getMacroDefinitions(virtualFile, psiFile.project)
    }

    /**
     * Check if a macro is defined for a NASM file (either in-file or via compiler flags).
     *
     * @param macroName The name of the macro to check
     * @return true if the macro is defined via compiler flags
     */
    fun isMacroDefined(file: VirtualFile, project: Project, macroName: String): Boolean {
        return getMacroDefinitions(file, project).containsKey(macroName)
    }

    /**
     * Check if a macro is defined for a PSI file.
     */
    fun isMacroDefined(psiFile: PsiFile, macroName: String): Boolean {
        val virtualFile = psiFile.virtualFile ?: return false
        return isMacroDefined(virtualFile, psiFile.project, macroName)
    }

    /**
     * Get the value of a macro defined via compiler flags.
     *
     * @return The macro value, null if defined without value, or null if not defined
     */
    fun getMacroValue(file: VirtualFile, project: Project, macroName: String): String? {
        return getMacroDefinitions(file, project)[macroName]
    }

    /**
     * Get the value of a macro for a PSI file.
     */
    fun getMacroValue(psiFile: PsiFile, macroName: String): String? {
        val virtualFile = psiFile.virtualFile ?: return null
        return getMacroValue(virtualFile, psiFile.project, macroName)
    }

    /**
     * Get the full compilation info for a file.
     *
     * @return NasmCompilationInfo or null if not available
     */
    fun getCompilationInfo(file: VirtualFile, project: Project): NasmCompilationInfo? {
        val service = NasmProjectModelService.getInstance(project)
        return service.getCompilationInfo(file)
    }

    /**
     * Get the full compilation info for a PSI file.
     */
    fun getCompilationInfo(psiFile: PsiFile): NasmCompilationInfo? {
        val virtualFile = psiFile.virtualFile ?: return null
        return getCompilationInfo(virtualFile, psiFile.project)
    }

    /**
     * Check if a file is part of the project's build system.
     *
     * @return true if the file has compilation info available
     */
    fun isCompiled(file: VirtualFile, project: Project): Boolean {
        return getCompilationInfo(file, project) != null
    }

    /**
     * Check if a PSI file is part of the project's build system.
     */
    fun isCompiled(psiFile: PsiFile): Boolean {
        val virtualFile = psiFile.virtualFile ?: return false
        return isCompiled(virtualFile, psiFile.project)
    }

    /**
     * Resolve an include file given the current file and the include path.
     * This searches the include directories configured in the build system.
     *
     * @param currentFile The file containing the %include directive
     * @param includePath The path specified in the %include directive
     * @return The resolved VirtualFile, or null if not found
     */
    fun resolveInclude(currentFile: VirtualFile, project: Project, includePath: String): VirtualFile? {
        val includePaths = getIncludePaths(currentFile, project)

        // Try each include path
        for (includeDir in includePaths) {
            val resolvedPath = includeDir.resolve(includePath)
            val file = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                .findFileByPath(resolvedPath.toString())

            if (file != null && file.exists()) {
                return file
            }
        }

        // Also try relative to the current file's directory
        val currentDir = currentFile.parent
        if (currentDir != null) {
            val relativeFile = currentDir.findFileByRelativePath(includePath)
            if (relativeFile != null && relativeFile.exists()) {
                return relativeFile
            }
        }

        return null
    }

    /**
     * Resolve an include file for a PSI file.
     */
    fun resolveInclude(psiFile: PsiFile, includePath: String): VirtualFile? {
        val virtualFile = psiFile.virtualFile ?: return null
        return resolveInclude(virtualFile, psiFile.project, includePath)
    }
}
