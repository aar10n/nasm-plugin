package dev.agb.nasmplugin.clion.projectmodel

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Parses compile_commands.json to extract NASM compilation information.
 */
object CompilationDatabaseParser {
    private val LOG = Logger.getInstance(CompilationDatabaseParser::class.java)
    private val gson = Gson()

    /**
     * Data class representing a single entry in compile_commands.json
     */
    private data class CompileCommand(
        @SerializedName("directory")
        val directory: String,

        @SerializedName("command")
        val command: String? = null,

        @SerializedName("arguments")
        val arguments: List<String>? = null,

        @SerializedName("file")
        val file: String,

        @SerializedName("output")
        val output: String? = null
    )

    /**
     * Parse a compilation database file and extract NASM compilation entries.
     *
     * @param compileCommandsPath Path to compile_commands.json
     * @param project The current project
     * @return NasmProjectCompilationInfo containing all NASM files found, or null if parsing fails
     */
    fun parse(compileCommandsPath: Path, project: Project): NasmProjectCompilationInfo? {
        if (!compileCommandsPath.exists()) {
            LOG.debug("Compilation database not found: $compileCommandsPath")
            return null
        }

        return try {
            val content = compileCommandsPath.readText()
            val commands = gson.fromJson(content, Array<CompileCommand>::class.java)

            val compilations = commands
                .filter { isNasmCompilation(it) }
                .mapNotNull { entry ->
                    parseNasmEntry(entry, project)
                }
                .associateBy { it.file }

            if (compilations.isEmpty()) {
                LOG.debug("No NASM compilation entries found in $compileCommandsPath")
                null
            } else {
                LOG.info("Found ${compilations.size} NASM compilation entries in $compileCommandsPath")
                NasmProjectCompilationInfo(compilations)
            }
        } catch (e: JsonSyntaxException) {
            LOG.warn("Failed to parse compilation database at $compileCommandsPath", e)
            null
        } catch (e: Exception) {
            LOG.warn("Error reading compilation database at $compileCommandsPath", e)
            null
        }
    }

    /**
     * Check if a compile command entry is for a NASM file.
     */
    private fun isNasmCompilation(entry: CompileCommand): Boolean {
        // Check if the file extension is .asm or .nasm
        val file = File(entry.file)
        val extension = file.extension.lowercase()
        if (extension != "asm" && extension != "nasm") {
            return false
        }

        // Check if the compiler command contains 'nasm'
        val commandLine = entry.command ?: entry.arguments?.joinToString(" ") ?: ""
        return commandLine.contains("nasm", ignoreCase = true)
    }

    /**
     * Parse a single NASM compilation entry into NasmCompilationInfo.
     */
    private fun parseNasmEntry(entry: CompileCommand, project: Project): NasmCompilationInfo? {
        // Resolve the source file
        val sourceFile = resolveFile(entry.file, entry.directory) ?: run {
            LOG.warn("Could not resolve NASM source file: ${entry.file}")
            return null
        }

        // Parse the command line to extract arguments
        val arguments = entry.arguments ?: parseCommandString(entry.command ?: "")

        // Filter out the compiler executable and the source file itself
        val compilerArguments = arguments
            .dropWhile { !it.contains("nasm", ignoreCase = true) } // Skip until we find 'nasm'
            .drop(1) // Skip the 'nasm' executable itself
            .filter { it != entry.file && it != sourceFile.path } // Remove source file from args

        // Extract output file if present
        val outputFile = entry.output?.let { Path.of(it) }
            ?: extractOutputFile(compilerArguments)

        return NasmCompilationInfo(
            file = sourceFile,
            workingDirectory = Path.of(entry.directory),
            compilerArguments = compilerArguments,
            outputFile = outputFile
        )
    }

    /**
     * Resolve a file path relative to a working directory and convert to VirtualFile.
     */
    private fun resolveFile(filePath: String, workingDir: String): VirtualFile? {
        val file = File(filePath)
        val absolutePath = if (file.isAbsolute) {
            file
        } else {
            File(workingDir, filePath)
        }.canonicalPath

        return LocalFileSystem.getInstance().findFileByPath(absolutePath)
    }

    /**
     * Parse a command string into individual arguments.
     * Handles quoted arguments with spaces.
     */
    private fun parseCommandString(command: String): List<String> {
        val args = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var escapeNext = false

        for (char in command) {
            when {
                escapeNext -> {
                    current.append(char)
                    escapeNext = false
                }
                char == '\\' -> {
                    escapeNext = true
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        args.add(current.toString())
                        current.clear()
                    }
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            args.add(current.toString())
        }

        return args
    }

    /**
     * Extract output file from compiler arguments.
     * NASM uses -o for output file.
     */
    private fun extractOutputFile(args: List<String>): Path? {
        var i = 0
        while (i < args.size) {
            val arg = args[i]
            when {
                // -o<file> (no space)
                arg.startsWith("-o") && arg.length > 2 -> {
                    return Path.of(arg.substring(2))
                }
                // -o <file> (with space)
                arg == "-o" && i + 1 < args.size -> {
                    return Path.of(args[i + 1])
                }
            }
            i++
        }
        return null
    }
}
