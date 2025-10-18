package dev.agb.nasmplugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.database.RegisterDatabase
import dev.agb.nasmplugin.database.InstructionDatabase
import dev.agb.nasmplugin.psi.NasmInstruction
import dev.agb.nasmplugin.psi.NasmTypes
import dev.agb.nasmplugin.psi.NasmMultiLineMacro
import dev.agb.nasmplugin.psi.NasmPpDefineStmt
import dev.agb.nasmplugin.psi.NasmEquDefinition
import dev.agb.nasmplugin.psi.NasmPpAssignStmt
import dev.agb.nasmplugin.psi.impl.CommentExtractor
import dev.agb.nasmplugin.psi.impl.NasmCommandLineMacroElement

/**
 * Provides documentation for NASM assembly elements including instructions, registers,
 * macros, and EQU constants.
 * Activated via hover (Ctrl+hover) and Quick Documentation (Ctrl+Q).
 */
class NasmDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        // Check if this element or any parent is an instruction mnemonic, register, macro, or EQU constant
        var current = contextElement
        while (current != null) {
            val type = current.node?.elementType

            if (type == NasmTypes.REGISTER) {
                return current
            }

            // Only show instruction documentation if we're on the mnemonic itself
            if (current is dev.agb.nasmplugin.psi.NasmMnemonic) {
                // Return the parent instruction for documentation
                val parent = current.parent
                if (parent is NasmInstruction) {
                    return parent
                }
            }

            // Check for macro definition or EQU definition
            if (current is NasmMultiLineMacro || current is NasmPpDefineStmt ||
                current is NasmEquDefinition || current is NasmPpAssignStmt) {
                return current
            }

            // Check if this is a reference to a macro - resolve it to show the definition
            if (type == NasmTypes.IDENTIFIER) {
                val reference = current.reference
                if (reference != null) {
                    val resolved = reference.resolve()
                    // If it resolves to a macro (source or command-line), show docs for it
                    if (resolved is NasmMultiLineMacro || resolved is NasmPpDefineStmt ||
                        resolved is NasmPpAssignStmt || resolved is NasmCommandLineMacroElement) {
                        return resolved
                    }
                }
            }

            current = current.parent
        }
        return null
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        // Use the element returned by getCustomDocumentationElement
        // Don't walk up the parent chain - getCustomDocumentationElement already does the filtering
        return element?.let { getDocForElement(it) }
    }

    private fun getDocForElement(element: PsiElement): String? {
        val text = element.text
        if (text.isNullOrEmpty()) return null

        return when {
            element is NasmInstruction -> {
                // Get the mnemonic from the instruction
                val mnemonic = element.mnemonic
                mnemonic.text?.let { generateInstructionDoc(it) }
            }
            element.node?.elementType == NasmTypes.REGISTER -> generateRegisterDoc(text)
            element is NasmCommandLineMacroElement -> generateCommandLineMacroDoc(element)
            element is NasmMultiLineMacro -> generateMacroDoc(element)
            element is NasmPpDefineStmt -> generateSingleLineMacroDoc(element)
            element is NasmPpAssignStmt -> generateAssignMacroDoc(element)
            element is NasmEquDefinition -> generateEquDoc(element)
            else -> null
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        element ?: return null
        val text = element.text
        if (text.isNullOrEmpty()) return null

        return when {
            element is NasmInstruction -> {
                // Get the mnemonic from the instruction
                val mnemonic = element.mnemonic
                mnemonic.text?.let { instructionName ->
                    InstructionDatabase.getInstruction(instructionName)?.let { instruction ->
                        instruction.documentation?.let { doc ->
                            "${instruction.name.uppercase()}: ${doc.summary}"
                        }
                    }
                }
            }
            element.node?.elementType == NasmTypes.REGISTER -> {
                RegisterDatabase.getRegister(text)?.let { reg ->
                    "${reg.name} (${reg.size.displayName} register)"
                }
            }
            element is NasmCommandLineMacroElement -> {
                "Command-line Macro: ${element.name}"
            }
            element is NasmMultiLineMacro -> {
                val name = (element as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name
                name?.let { "Macro: $it" }
            }
            element is NasmPpDefineStmt -> {
                val name = (element as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name
                name?.let { "Single-line Macro: $it" }
            }
            element is NasmPpAssignStmt -> {
                val name = (element as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name
                name?.let { "Assign Macro: $it" }
            }
            element is NasmEquDefinition -> {
                val name = (element as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name
                name?.let { "EQU Constant: $it" }
            }
            else -> null
        }
    }

    /**
     * Groups instruction variants by pattern, collapsing similar operands with different sizes.
     * For example, converts:
     *   PUSH R_M16, PUSH R_M32, PUSH R_M64
     * Into:
     *   PUSH r/m{16,32,64}
     */
    private fun groupVariantsByPattern(variants: List<InstructionDatabase.InstructionVariant>): List<String> {
        // Map each variant to its normalized pattern (operand types without sizes)
        data class PatternGroup(
            val operandTypes: List<String>, // e.g., ["r/m", "r"]
            val sizesByPosition: MutableMap<Int, MutableSet<String>>,  // sizes for each operand position
            val optional: List<Boolean>     // whether each operand is optional
        )

        val patternMap = mutableMapOf<String, PatternGroup>()

        variants.forEach { variant ->
            val types = mutableListOf<String>()
            val sizes = mutableListOf<String>()
            val optional = mutableListOf<Boolean>()

            variant.operands.forEach { operand ->
                val typeStr = operand.type.toString()
                // Extract base type and size from operand types like R_M16, IMM32, etc.
                val (baseType, size) = parseOperandType(typeStr)
                types.add(baseType)
                sizes.add(size)
                optional.add(operand.optional)
            }

            // Create a key based on operand types and count (to preserve order)
            val key = types.joinToString(",")

            val group = patternMap.getOrPut(key) {
                PatternGroup(types, mutableMapOf(), optional)
            }

            // For each operand position, track all sizes seen
            sizes.forEachIndexed { idx, size ->
                if (size.isNotEmpty()) {
                    group.sizesByPosition.getOrPut(idx) { mutableSetOf() }.add(size)
                }
            }
        }

        // Convert groups back to readable strings, sorted for consistency
        return patternMap.values.map { group ->
            if (group.operandTypes.isEmpty()) return@map ""

            // Format each operand
            val formattedOperands = group.operandTypes.mapIndexed { idx, baseType ->
                val sizes = group.sizesByPosition[idx] ?: emptySet()
                val formattedType = formatOperandWithSizes(baseType, sizes.sorted())
                if (group.optional.getOrElse(idx) { false }) "[$formattedType]" else formattedType
            }

            formattedOperands.joinToString(", ")
        }.filter { it.isNotEmpty() }.sorted()
    }

    /**
     * Parses an operand type string like "R_M16" into base type "r/m" and size "16"
     */
    private fun parseOperandType(typeStr: String): Pair<String, String> {
        // Handle common patterns
        return when {
            typeStr.startsWith("R_M") -> {
                val size = typeStr.removePrefix("R_M")
                "r/m" to size
            }
            typeStr.startsWith("IMM") -> {
                val size = typeStr.removePrefix("IMM")
                "imm" to size
            }
            typeStr.startsWith("REG") -> {
                val size = typeStr.removePrefix("REG")
                "r" to size
            }
            typeStr.matches(Regex("R\\d+")) -> {
                val size = typeStr.removePrefix("R")
                "r" to size
            }
            typeStr.matches(Regex("M\\d+")) -> {
                val size = typeStr.removePrefix("M")
                "m" to size
            }
            typeStr.startsWith("MOFFS") -> {
                val size = typeStr.removePrefix("MOFFS")
                "moffs" to size
            }
            typeStr.startsWith("REL") -> {
                val size = typeStr.removePrefix("REL")
                "rel" to size
            }
            typeStr.startsWith("XMM") -> "xmm" to ""
            typeStr.startsWith("YMM") -> "ymm" to ""
            typeStr.startsWith("ZMM") -> "zmm" to ""
            typeStr.startsWith("MM") -> "mm" to ""
            typeStr.startsWith("SREG") -> "sreg" to ""
            typeStr.startsWith("ST") -> "st" to ""
            // Default: try to split at last digit sequence
            else -> {
                val match = Regex("^(.+?)(\\d+)$").find(typeStr)
                if (match != null) {
                    val (base, size) = match.destructured
                    base.lowercase().replace("_", "/") to size
                } else {
                    typeStr.lowercase().replace("_", "/") to ""
                }
            }
        }
    }

    /**
     * Formats an operand base type with its sizes, e.g., "r/m" + ["16", "32", "64"] -> "r/m{16,32,64}"
     */
    private fun formatOperandWithSizes(baseType: String, sizes: List<String>): String {
        return if (sizes.isEmpty() || sizes.size == 1 && sizes[0].isEmpty()) {
            baseType
        } else if (sizes.size == 1) {
            "$baseType${sizes[0]}"
        } else {
            "$baseType{${sizes.joinToString(",")}}"
        }
    }

    private fun generateInstructionDoc(instructionName: String): String? {
        val instruction = InstructionDatabase.getInstruction(instructionName) ?: return null
        val doc = instruction.documentation

        return buildString {
            append("<div>")

            // Title
            append("<h3 style='margin-top: 0;'>${instruction.name.uppercase()}</h3>")

            // If we have full documentation, show it
            if (doc != null) {
                // Summary
                if (doc.summary.isNotEmpty()) {
                    append("<p><b>${doc.summary}</b></p>")
                }

                // Description
                if (doc.description.isNotEmpty()) {
                    append("<p>${doc.description}</p>")
                }
            } else {
                // Fallback: show instruction description if no detailed documentation available
                if (instruction.description.isNotEmpty()) {
                    append("<p><b>${instruction.description}</b></p>")
                }
            }

            // Syntax variants from instruction database
            if (instruction.variants.isNotEmpty()) {
                append("<p><b>Operand Variants:</b></p>")
                append("<ul>")

                // Group variants by operand pattern (collapse similar operands with different sizes)
                val grouped = groupVariantsByPattern(instruction.variants)
                grouped.forEach { pattern ->
                    append("<li><code>${instruction.name.uppercase()} $pattern</code></li>")
                }

                append("</ul>")
            }

            // Operation (only if full documentation exists)
            if (doc != null && doc.operation.isNotEmpty()) {
                append("<p><b>Operation:</b></p>")
                append("<pre>${doc.operation}</pre>")
            }

            // Flags Affected (only if full documentation exists)
            if (doc != null && doc.flagsAffected.isNotEmpty()) {
                append("<p><b>Flags Affected:</b> ${doc.flagsAffected}</p>")
            }

            // Notes (only if full documentation exists)
            if (doc != null && doc.notes.isNotEmpty()) {
                append("<p><i>${doc.notes}</i></p>")
            }

            append("</div>")
        }
    }

    private fun generateRegisterDoc(registerName: String): String? {
        val register = RegisterDatabase.getRegister(registerName) ?: return null

        return buildString {
            append("<div>")

            // Title
            append("<h3 style='margin-top: 0;'>${register.name.uppercase()}</h3>")

            // Type and size
            append("<p><b>${register.size.displayName} register</b></p>")

            // Description from RegisterDatabase
            if (register.description.isNotEmpty()) {
                append("<p>${register.description}</p>")
            }

            // Related registers (computed dynamically based on register family)
            getRelatedRegisters(register)?.let { related ->
                if (related.isNotEmpty()) {
                    append("<p><b>Related registers:</b></p>")
                    append("<ul>")
                    related.forEach { (size, names) ->
                        append("<li>${size}: ${names.joinToString(", ")}</li>")
                    }
                    append("</ul>")
                }
            }

            append("</div>")
        }
    }

    private fun getRelatedRegisters(register: RegisterDatabase.Register): Map<String, List<String>>? {
        val name = register.name.lowercase()

        // Determine the register family based on the name
        val family = when {
            // Traditional general-purpose registers
            name.matches(Regex("[re]?[abcd]x|[abcd][lh]")) -> getGeneralRegisterFamily(name, 'a', 'x')
            name.matches(Regex("[re]?[bs]il?")) -> getGeneralRegisterFamily(name, 's', 'i')
            name.matches(Regex("[re]?[bd]il?")) -> getGeneralRegisterFamily(name, 'd', 'i')
            name.matches(Regex("[re]?[sb]pl?")) -> getGeneralRegisterFamily(name, name[name.length - 2], 'p')

            // Extended general-purpose registers (r8-r15)
            name.matches(Regex("r(8|9|1[0-5])[dwb]?")) -> getExtendedRegisterFamily(name)

            // SIMD registers
            name.matches(Regex("[xyz]mm\\d+")) -> getSimdRegisterFamily(name)

            else -> null
        }

        if (family == null || family.size <= 1) return null

        // Group registers by size
        val grouped = mutableMapOf<String, MutableList<String>>()
        family.forEach { regName ->
            RegisterDatabase.getRegister(regName)?.let { reg ->
                grouped.getOrPut(reg.size.displayName) { mutableListOf() }.add(regName.uppercase())
            }
        }

        return if (grouped.size > 1) grouped else null
    }

    private fun getGeneralRegisterFamily(name: String, firstChar: Char, secondChar: Char): List<String> {
        val baseChar = when {
            name.contains('a') -> 'a'
            name.contains('b') -> 'b'
            name.contains('c') -> 'c'
            name.contains('d') -> 'd'
            name.contains('s') && name.contains('i') -> return listOf("rsi", "esi", "si", "sil")
            name.contains('d') && name.contains('i') -> return listOf("rdi", "edi", "di", "dil")
            name.contains('s') && name.contains('p') -> return listOf("rsp", "esp", "sp", "spl")
            name.contains('b') && name.contains('p') -> return listOf("rbp", "ebp", "bp", "bpl")
            else -> return emptyList()
        }

        return when (baseChar) {
            'a' -> listOf("rax", "eax", "ax", "al", "ah")
            'b' -> listOf("rbx", "ebx", "bx", "bl", "bh")
            'c' -> listOf("rcx", "ecx", "cx", "cl", "ch")
            'd' -> listOf("rdx", "edx", "dx", "dl", "dh")
            else -> emptyList()
        }
    }

    private fun getExtendedRegisterFamily(name: String): List<String> {
        val match = Regex("r(\\d+)").find(name) ?: return emptyList()
        val num = match.groupValues[1]
        return listOf("r$num", "r${num}d", "r${num}w", "r${num}b")
    }

    private fun getSimdRegisterFamily(name: String): List<String> {
        val match = Regex("([xyz])mm(\\d+)").find(name) ?: return emptyList()
        val num = match.groupValues[2]
        return listOf("zmm$num", "ymm$num", "xmm$num")
    }

    /**
     * Generates documentation for command-line defined macros.
     */
    private fun generateCommandLineMacroDoc(macro: NasmCommandLineMacroElement): String {
        val name = macro.name
        val value = macro.getMacroValue()

        return buildString {
            append("<div>")
            append("<h3 style='margin-top: 0;'>$name</h3>")
            append("<p><b>Command-line Macro</b> (from project settings)</p>")

            // Show the definition with normalized spacing
            append("<p><b>Definition:</b></p>")
            append("<pre>")
            if (value != null) {
                // Normalize spacing in the value (collapse multiple spaces to one)
                val normalizedValue = value.trim().replace(Regex("\\s+"), " ")
                append("%define $name $normalizedValue")
            } else {
                append("%define $name")
            }
            append("</pre>")

            append("</div>")
        }
    }

    /**
     * Generates documentation for multi-line macros.
     */
    private fun generateMacroDoc(macro: NasmMultiLineMacro): String? {
        val name = (macro as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name ?: return null
        val comments = CommentExtractor.extractComments(macro)

        return buildString {
            append("<div>")
            append("<h3 style='margin-top: 0;'>$name</h3>")
            append("<p><b>Multi-line Macro</b></p>")

            // Show comments if available (before definition, like CLion does for C macros)
            if (comments.isNotEmpty()) {
                comments.forEach { comment ->
                    if (comment.isNotEmpty()) {
                        append("<p>")
                        append(comment.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                        append("</p>")
                    }
                }
            }

            // Show the macro definition header
            val firstLine = macro.text.lines().firstOrNull()
            if (firstLine != null) {
                append("<p><b>Definition:</b></p>")
                append("<pre style='white-space: pre-wrap; word-wrap: break-word;'>")
                append(firstLine.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                append("</pre>")
            }

            append("</div>")
        }
    }

    /**
     * Generates documentation for single-line macros (%define).
     */
    private fun generateSingleLineMacroDoc(macro: NasmPpDefineStmt): String? {
        val name = (macro as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name ?: return null
        val comments = CommentExtractor.extractComments(macro)

        return buildString {
            append("<div>")
            append("<h3 style='margin-top: 0;'>$name</h3>")
            append("<p><b>Single-line Macro</b></p>")

            // Show comments if available (before definition, like CLion does for C macros)
            if (comments.isNotEmpty()) {
                comments.forEach { comment ->
                    if (comment.isNotEmpty()) {
                        append("<p>")
                        append(comment.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                        append("</p>")
                    }
                }
            }

            // Show the macro definition with proper escaping and normalized spacing
            val definitionText = macro.text.trim().replace(Regex("\\s+"), " ")
            if (definitionText.isNotEmpty()) {
                append("<p><b>Definition:</b></p>")
                append("<pre style='white-space: pre-wrap; word-wrap: break-word;'>")
                append(definitionText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                append("</pre>")
            }

            append("</div>")
        }
    }

    /**
     * Generates documentation for assign macros (%assign).
     */
    private fun generateAssignMacroDoc(macro: NasmPpAssignStmt): String? {
        val name = (macro as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name ?: return null
        val comments = CommentExtractor.extractComments(macro)

        return buildString {
            append("<div>")
            append("<h3 style='margin-top: 0;'>$name</h3>")
            append("<p><b>Assign Macro</b></p>")

            // Show comments if available (before definition, like CLion does for C macros)
            if (comments.isNotEmpty()) {
                comments.forEach { comment ->
                    if (comment.isNotEmpty()) {
                        append("<p>")
                        append(comment.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                        append("</p>")
                    }
                }
            }

            // Show the macro definition with proper escaping and normalized spacing
            val definitionText = macro.text.trim().replace(Regex("\\s+"), " ")
            if (definitionText.isNotEmpty()) {
                append("<p><b>Definition:</b></p>")
                append("<pre style='white-space: pre-wrap; word-wrap: break-word;'>")
                append(definitionText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                append("</pre>")
            }

            append("</div>")
        }
    }

    private fun generateEquDoc(equDef: NasmEquDefinition): String? {
        val name = (equDef as? dev.agb.nasmplugin.psi.NasmNamedElement)?.name ?: return null
        val comments = CommentExtractor.extractComments(equDef)

        return buildString {
            append("<div>")
            append("<h3 style='margin-top: 0;'>$name</h3>")
            append("<p><b>EQU Constant</b></p>")

            // Show comments if available (before definition, like CLion does for C macros)
            if (comments.isNotEmpty()) {
                comments.forEach { comment ->
                    if (comment.isNotEmpty()) {
                        append("<p>")
                        append(comment.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                        append("</p>")
                    }
                }
            }

            // Show the EQU definition with proper escaping and no truncation
            val definitionText = equDef.text.trim()
            if (definitionText.isNotEmpty()) {
                append("<p><b>Definition:</b></p>")
                append("<pre style='white-space: pre-wrap; word-wrap: break-word;'>")
                append(definitionText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                append("</pre>")
            }

            append("</div>")
        }
    }
}
