package dev.agb.nasmplugin.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Represents a single-line macro defined via command-line (equivalent to NASM -D flag).
 *
 * @property name The macro name (identifier)
 * @property value The macro value, or null if the macro is defined without a value
 */
data class CommandLineMacro(
    val name: String,
    val value: String?
) {
    /**
     * Returns the full definition text as it would appear in NASM source.
     * Examples:
     * - "DEBUG" if value is null
     * - "VERSION 2" if value is "2"
     */
    val definitionText: String
        get() = if (value != null) "$name $value" else name
}

/**
 * Service that parses and provides access to command-line macro definitions.
 * These macros can come from two sources:
 * 1. Global macros defined in project settings (manual configuration)
 * 2. Per-file macros from the compilation database (auto-discovered from build system)
 *
 * Per-file macros take precedence over global macros.
 */
@Service(Service.Level.PROJECT)
class CommandLineMacroProvider(private val project: Project) {

    /**
     * Gets command-line macros for a specific file by combining:
     * 1. Per-file macros from compilation database (if available)
     * 2. Global macros from project settings
     *
     * Per-file macros take precedence over global macros.
     *
     * @param file The file to get macros for, or null to get global macros only
     * @return List of command-line macros
     */
    fun getCommandLineMacros(file: VirtualFile? = null): List<CommandLineMacro> {
        LOG.warn("NASM_DEBUG: getCommandLineMacros called for file: ${file?.path ?: "null"}")

        // Get per-file macros from compilation database
        val perFileMacros = if (file != null) {
            getPerFileMacros(file)
        } else {
            emptyMap()
        }

        // Get global macros from project settings
        val globalMacros = getGlobalMacros()
        LOG.warn("NASM_DEBUG: Global macros: ${globalMacros.size} - $globalMacros")

        // Merge: per-file macros override global macros with the same name
        val mergedMacros = mutableMapOf<String, CommandLineMacro>()
        globalMacros.forEach { mergedMacros[it.name] = it }
        perFileMacros.forEach { (name, value) -> mergedMacros[name] = CommandLineMacro(name, value) }

        val result = mergedMacros.values.toList()
        LOG.warn("NASM_DEBUG: Final merged macros for ${file?.name ?: "null"}: ${result.size} - $result")

        return result
    }

    /**
     * Gets per-file macros from the compilation database.
     */
    private fun getPerFileMacros(file: VirtualFile): Map<String, String?> {
        return try {
            LOG.warn("NASM_DEBUG: Getting per-file macros for ${file.path}")
            // Use fully qualified name to avoid compile-time dependency in non-CLion environments
            val serviceClass = Class.forName("dev.agb.nasmplugin.clion.projectmodel.NasmProjectModelService")

            // Get the companion object and call getInstance
            val companionField = serviceClass.getDeclaredField("Companion")
            val companion = companionField.get(null)
            val getInstance = companion.javaClass.getDeclaredMethod("getInstance", Project::class.java)
            val service = getInstance.invoke(companion, project)
            LOG.warn("NASM_DEBUG: Got service instance: $service")

            val getCompilationInfo = serviceClass.getDeclaredMethod("getCompilationInfo", VirtualFile::class.java)
            val compilationInfo = getCompilationInfo.invoke(service, file)
            LOG.warn("NASM_DEBUG: Got compilation info: $compilationInfo")

            if (compilationInfo != null) {
                val getMacroDefinitions = compilationInfo.javaClass.getDeclaredMethod("getMacroDefinitions")
                @Suppress("UNCHECKED_CAST")
                val macros = getMacroDefinitions.invoke(compilationInfo) as? Map<String, String?> ?: emptyMap()
                LOG.warn("NASM_DEBUG: Extracted ${macros.size} per-file macros for ${file.name}: $macros")
                macros
            } else {
                LOG.warn("NASM_DEBUG: No compilation info found for ${file.name}")
                emptyMap()
            }
        } catch (e: ClassNotFoundException) {
            // CLion project model service not available (not in CLion or plugin not loaded)
            LOG.warn("NASM_DEBUG: CLion project model service not available (ClassNotFoundException)")
            emptyMap()
        } catch (e: Exception) {
            LOG.warn("NASM_DEBUG: Failed to get per-file macros from project model", e)
            emptyMap()
        }
    }

    /**
     * Parses the command-line macro definitions from global project settings.
     * Returns a list of CommandLineMacro objects.
     *
     * Format: comma-separated list of "MACRO[=value]" entries
     * Examples:
     * - "DEBUG" → CommandLineMacro("DEBUG", null)
     * - "VERSION=2" → CommandLineMacro("VERSION", "2")
     * - "DEBUG,VERSION=2" → [CommandLineMacro("DEBUG", null), CommandLineMacro("VERSION", "2")]
     */
    private fun getGlobalMacros(): List<CommandLineMacro> {
        val settings = NasmProjectSettings.getInstance(project)
        val macroString = settings.commandLineMacros.trim()

        if (macroString.isEmpty()) {
            return emptyList()
        }

        return macroString
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { parseMacroDefinition(it) }
    }

    /**
     * Parses a single macro definition string into a CommandLineMacro.
     * Handles formats:
     * - "MACRO" → CommandLineMacro("MACRO", null)
     * - "MACRO=value" → CommandLineMacro("MACRO", "value")
     * - "MACRO=" → CommandLineMacro("MACRO", "") (empty value)
     */
    private fun parseMacroDefinition(definition: String): CommandLineMacro? {
        val parts = definition.split('=', limit = 2)
        val name = parts[0].trim()

        if (name.isEmpty() || !isValidMacroName(name)) {
            return null
        }

        val value = if (parts.size > 1) parts[1] else null

        return CommandLineMacro(name, value)
    }

    /**
     * Checks if a string is a valid NASM macro name.
     * Valid names:
     * - Start with letter or underscore
     * - Contain only letters, digits, underscores
     */
    private fun isValidMacroName(name: String): Boolean {
        if (name.isEmpty()) return false

        val first = name[0]
        if (!first.isLetter() && first != '_') {
            return false
        }

        return name.all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * Finds a command-line macro by name (case-sensitive).
     * Returns null if no macro with the given name is defined.
     *
     * @param name The macro name to search for
     * @param file The file to get macros for, or null to search global macros only
     */
    fun findMacroByName(name: String, file: VirtualFile? = null): CommandLineMacro? {
        return getCommandLineMacros(file).firstOrNull { it.name == name }
    }

    companion object {
        private val LOG = Logger.getInstance(CommandLineMacroProvider::class.java)

        @JvmStatic
        fun getInstance(project: Project): CommandLineMacroProvider {
            return project.getService(CommandLineMacroProvider::class.java)
        }
    }
}
