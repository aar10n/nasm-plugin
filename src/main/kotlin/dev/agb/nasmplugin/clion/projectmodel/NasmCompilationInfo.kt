package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Represents compilation information for a single NASM file.
 * This includes the source file, compiler flags, and extracted metadata.
 */
data class NasmCompilationInfo(
    /** The NASM source file being compiled */
    val file: VirtualFile,

    /** The working directory where compilation occurs */
    val workingDirectory: Path,

    /** Complete compiler command line arguments (excluding the compiler executable itself) */
    val compilerArguments: List<String>,

    /** Output file path (if specified) */
    val outputFile: Path? = null
) {
    /** Lazy-parsed include directories from compiler flags (absolute and deduplicated) */
    val includePaths: List<Path> by lazy {
        extractIncludePaths(compilerArguments, workingDirectory)
    }

    /** Lazy-parsed macro definitions from compiler flags */
    val macroDefinitions: Map<String, String?> by lazy {
        extractMacroDefinitions(compilerArguments)
    }

    companion object {
        /**
         * Extract include paths from NASM compiler arguments.
         * NASM uses -I or -i for include paths.
         * Returns absolute paths, deduplicated.
         */
        private fun extractIncludePaths(args: List<String>, workingDir: Path): List<Path> {
            val paths = mutableSetOf<Path>()  // Use Set for automatic deduplication
            var i = 0
            while (i < args.size) {
                val arg = args[i]
                when {
                    // -I<path> or -i<path> (no space)
                    arg.startsWith("-I") && arg.length > 2 -> {
                        val path = Path.of(arg.substring(2))
                        paths.add(resolveToAbsolute(path, workingDir))
                    }
                    arg.startsWith("-i") && arg.length > 2 -> {
                        val path = Path.of(arg.substring(2))
                        paths.add(resolveToAbsolute(path, workingDir))
                    }
                    // -I <path> or -i <path> (with space)
                    (arg == "-I" || arg == "-i") && i + 1 < args.size -> {
                        val path = Path.of(args[i + 1])
                        paths.add(resolveToAbsolute(path, workingDir))
                        i++ // Skip next argument
                    }
                }
                i++
            }
            return paths.toList()
        }

        /**
         * Resolve a path to absolute form.
         * If the path is relative, resolve it relative to the working directory.
         */
        private fun resolveToAbsolute(path: Path, workingDir: Path): Path {
            return if (path.isAbsolute) {
                path.normalize()
            } else {
                workingDir.resolve(path).normalize()
            }
        }

        /**
         * Extract macro definitions from NASM compiler arguments.
         * NASM uses -D for macro definitions.
         * Returns a map where key is the macro name and value is the definition (null if not specified).
         */
        private fun extractMacroDefinitions(args: List<String>): Map<String, String?> {
            val macros = mutableMapOf<String, String?>()
            var i = 0
            while (i < args.size) {
                val arg = args[i]
                when {
                    // -D<name>=<value> or -D<name> (no space)
                    arg.startsWith("-D") && arg.length > 2 -> {
                        val definition = arg.substring(2)
                        val parts = definition.split('=', limit = 2)
                        macros[parts[0]] = parts.getOrNull(1)
                    }
                    // -D <name>=<value> or -D <name> (with space)
                    arg == "-D" && i + 1 < args.size -> {
                        val definition = args[i + 1]
                        val parts = definition.split('=', limit = 2)
                        macros[parts[0]] = parts.getOrNull(1)
                        i++ // Skip next argument
                    }
                }
                i++
            }
            return macros
        }
    }
}

/**
 * Container for all NASM compilation information in a project.
 */
data class NasmProjectCompilationInfo(
    /** Map from file path to compilation info */
    val compilations: Map<VirtualFile, NasmCompilationInfo>
) {
    /**
     * Get compilation info for a specific file, or null if not found.
     */
    fun getCompilationInfo(file: VirtualFile): NasmCompilationInfo? = compilations[file]

    /**
     * Get all NASM files that are compiled in this project.
     */
    val compiledFiles: Set<VirtualFile> get() = compilations.keys
}
