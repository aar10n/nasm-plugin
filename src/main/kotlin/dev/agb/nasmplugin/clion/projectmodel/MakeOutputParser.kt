package dev.agb.nasmplugin.clion.projectmodel

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Path

/**
 * Shared utility for parsing make output to extract NASM compilation commands.
 * Used by both CMakeProjectAnalyzer and MakefileProjectAnalyzer.
 */
object MakeOutputParser {
    private val LOG = Logger.getInstance(MakeOutputParser::class.java)

    /**
     * Run `make -nB` in the specified directory to get a dry-run of build commands.
     */
    fun runMakeDryRun(workingDirectory: String): String? {
        return try {
            val commandLine = GeneralCommandLine("make", "-nB")
                .withWorkDirectory(workingDirectory)

            val handler = CapturingProcessHandler(commandLine)
            val result = handler.runProcess(30000) // 30 seconds timeout

            if (result.exitCode == 0 || result.exitCode == 2) {
                // Exit code 2 is common when make finds nothing to do
                result.stdout
            } else {
                LOG.warn("make -nB failed with exit code ${result.exitCode}: ${result.stderr}")
                null
            }
        } catch (e: Exception) {
            LOG.warn("Failed to execute make -nB", e)
            null
        }
    }

    /**
     * Parse the output of `make -nB` to extract NASM compilation commands.
     */
    fun parseMakeOutput(
        output: String,
        workingDirectory: String,
        project: Project
    ): Map<VirtualFile, NasmCompilationInfo> {
        val compilations = mutableMapOf<VirtualFile, NasmCompilationInfo>()

        // Process each line looking for NASM invocations
        for (line in output.lines()) {
            val trimmed = line.trim()

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue
            }

            // Look for lines that invoke NASM
            if (!trimmed.contains("nasm", ignoreCase = true)) {
                continue
            }

            // Parse the command line
            val nasmInfo = parseNasmCommand(trimmed, workingDirectory, project)
            if (nasmInfo != null) {
                compilations[nasmInfo.file] = nasmInfo
            }
        }

        return compilations
    }

    /**
     * Parse a single NASM command line from make output.
     */
    private fun parseNasmCommand(
        commandLine: String,
        workingDirectory: String,
        project: Project
    ): NasmCompilationInfo? {
        // Tokenize the command line
        val tokens = tokenizeCommandLine(commandLine)

        // Find the nasm executable
        val nasmIndex = tokens.indexOfFirst { it.contains("nasm", ignoreCase = true) }
        if (nasmIndex == -1) {
            return null
        }

        // Everything after 'nasm' is arguments
        val arguments = tokens.drop(nasmIndex + 1)

        // Find the source file (should be a .asm or .nasm file that exists)
        val sourceFile = arguments
            .firstOrNull { arg ->
                !arg.startsWith("-") &&
                (arg.endsWith(".asm", ignoreCase = true) || arg.endsWith(".nasm", ignoreCase = true))
            } ?: run {
                LOG.debug("Could not find source file in NASM command: $commandLine")
                return null
            }

        // Resolve the source file to a VirtualFile
        val virtualFile = resolveFile(sourceFile, workingDirectory) ?: run {
            LOG.debug("Could not resolve source file: $sourceFile")
            return null
        }

        // Filter out the source file from arguments
        val compilerArguments = arguments.filter { it != sourceFile }

        // Extract output file
        val outputFile = extractOutputFile(compilerArguments)

        return NasmCompilationInfo(
            file = virtualFile,
            workingDirectory = Path.of(workingDirectory),
            compilerArguments = compilerArguments,
            outputFile = outputFile
        )
    }

    /**
     * Tokenize a command line, handling quoted strings.
     */
    private fun tokenizeCommandLine(commandLine: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var escapeNext = false

        for (char in commandLine) {
            when {
                escapeNext -> {
                    current.append(char)
                    escapeNext = false
                }
                char == '\\' -> {
                    escapeNext = true
                }
                char == '"' || char == '\'' -> {
                    inQuotes = !inQuotes
                }
                char.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }

        return tokens
    }

    /**
     * Resolve a file path relative to a working directory.
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
     * Extract output file from compiler arguments.
     */
    private fun extractOutputFile(args: List<String>): Path? {
        var i = 0
        while (i < args.size) {
            val arg = args[i]
            when {
                arg.startsWith("-o") && arg.length > 2 -> {
                    return Path.of(arg.substring(2))
                }
                arg == "-o" && i + 1 < args.size -> {
                    return Path.of(args[i + 1])
                }
            }
            i++
        }
        return null
    }
}
