package dev.agb.nasmplugin.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

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
 * These macros are defined in project settings and act as if they were passed
 * via NASM's -D command-line flag.
 */
@Service(Service.Level.PROJECT)
class CommandLineMacroProvider(private val project: Project) {

    /**
     * Parses the command-line macro definitions from settings.
     * Returns a list of CommandLineMacro objects.
     *
     * Format: comma-separated list of "MACRO[=value]" entries
     * Examples:
     * - "DEBUG" → CommandLineMacro("DEBUG", null)
     * - "VERSION=2" → CommandLineMacro("VERSION", "2")
     * - "DEBUG,VERSION=2" → [CommandLineMacro("DEBUG", null), CommandLineMacro("VERSION", "2")]
     */
    fun getCommandLineMacros(): List<CommandLineMacro> {
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
     */
    fun findMacroByName(name: String): CommandLineMacro? {
        return getCommandLineMacros().firstOrNull { it.name == name }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommandLineMacroProvider {
            return project.getService(CommandLineMacroProvider::class.java)
        }
    }
}
