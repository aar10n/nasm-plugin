package dev.agb.nasmplugin.navigation

import com.intellij.openapi.vfs.VirtualFile

/**
 * Interface for resolving include paths for NASM files.
 */
interface NasmIncludePathResolver {
    /**
     * Get NASM-specific include paths for a file.
     * @param file the file to get include paths for
     * @return list of absolute include paths, or empty list if not available
     */
    fun getNasmIncludePaths(file: VirtualFile): List<String>
}
