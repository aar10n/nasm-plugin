package dev.agb.nasmplugin.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import dev.agb.nasmplugin.database.DirectiveDatabase
import dev.agb.nasmplugin.database.InstructionDatabase
import dev.agb.nasmplugin.database.RegisterDatabase

/**
 * Validates names for NASM elements during rename refactoring.
 * Ensures that new names are valid NASM identifiers.
 */
class NasmNamesValidator : NamesValidator {

    // NASM preprocessor directives (without %) that are not in the directives database
    // These are special because they're preprocessor-specific
    private val preprocessorDirectives = setOf(
            "include", "define", "xdefine", "undef", "assign",
            "macro", "endmacro", "if", "ifdef", "ifndef", "else",
            "elif", "endif", "error", "warning", "rep", "endrep"
    )

    // Valid identifier pattern for NASM
    private val identifierPattern = Regex("""^[a-zA-Z_@$?][a-zA-Z0-9_@$?]*$""")
    private val localLabelPattern = Regex("""^\.\.?[a-zA-Z_@$?][a-zA-Z0-9_@$?]*$""")
    private val macroLocalPattern = Regex("""^%%[a-zA-Z_@$?][a-zA-Z0-9_@$?]*$""")
    private val contextLocalPattern = Regex("""^%\$[a-zA-Z_@$?][a-zA-Z0-9_@$?]*$""")

    /**
     * Check if a string is a valid keyword in NASM.
     * A keyword is any reserved word that cannot be used as an identifier:
     * - Directives (from DirectiveDatabase)
     * - Instructions (from InstructionDatabase)
     * - Registers (from RegisterDatabase)
     * - Preprocessor directives (special list)
     */
    override fun isKeyword(name: String, project: Project?): Boolean {
        val lowerName = name.lowercase()

        // Check if it's a directive
        if (DirectiveDatabase.getDirective(lowerName) != null) {
            return true
        }

        // Check if it's an instruction
        if (InstructionDatabase.getInstruction(lowerName) != null) {
            return true
        }

        // Check if it's a register
        if (RegisterDatabase.getRegister(lowerName) != null) {
            return true
        }

        // Check if it's a preprocessor directive
        if (preprocessorDirectives.contains(lowerName)) {
            return true
        }

        return false
    }

    /**
     * Check if a string is a valid identifier in NASM.
     */
    override fun isIdentifier(name: String, project: Project?): Boolean {
        if (name.isEmpty()) return false

        // Check different identifier patterns
        return when {
            name.startsWith("%%") -> macroLocalPattern.matches(name)
            name.startsWith("%$") -> contextLocalPattern.matches(name)
            name.startsWith(".") -> localLabelPattern.matches(name)
            else -> identifierPattern.matches(name) && !isKeyword(name, project)
        }
    }
}
