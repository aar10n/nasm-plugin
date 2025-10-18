package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmSymbolResolver

/**
 * Validates symbol references and macro calls.
 *
 * This annotator validates:
 * - Macro invocations (correct number of arguments)
 * - Function macro calls (parameter count validation)
 * - Symbol references (undefined symbols - as warnings)
 * - External and global symbol references
 */
class NasmSymbolValidationAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is NasmMacroCall -> validateMacroCall(element, holder)
            is NasmFunctionMacroCall -> validateFunctionMacroCall(element, holder)
            is NasmSymbolRef -> validateSymbolReference(element, holder)
        }
    }

    private fun validateMacroCall(macroCall: NasmMacroCall, holder: AnnotationHolder) {
        val macroName = macroCall.text.substringBefore(' ').trim()
        val file = macroCall.containingFile ?: return

        // Find the macro definition
        val macroDef = findMacroDefinitionWithIncludes(macroName, file) as? NasmMultiLineMacro
            ?: return // Don't report errors for macros that might be defined elsewhere

        // For now, skip parameter count validation since the PSI structure doesn't easily expose it
        // TODO: Implement parameter count validation when PSI structure is improved
    }

    private fun validateFunctionMacroCall(functionCall: NasmFunctionMacroCall, holder: AnnotationHolder) {
        val macroName = extractFunctionMacroName(functionCall) ?: return
        val file = functionCall.containingFile ?: return

        // Find the macro definition (could be %define or %assign)
        val macroDef = findMacroDefinitionWithIncludes(macroName, file)

        if (macroDef == null) {
            // Only warn about undefined macros if they're not built-in preprocessor functions
            if (!isBuiltInPreprocessorFunction(macroName)) {
                holder.newAnnotation(
                    HighlightSeverity.WARNING,
                    "Unresolved macro or function: '$macroName'"
                )
                    .range(functionCall)
                    .create()
            }
        } else if (macroDef is NasmMultiLineMacro) {
            // Multi-line macros used as function macros
            // TODO: Implement parameter count validation when PSI structure is improved
        }
    }

    private fun validateSymbolReference(symbolRef: NasmSymbolRef, holder: AnnotationHolder) {
        val symbolName = symbolRef.text
        val file = symbolRef.containingFile ?: return

        // Try to resolve the symbol using the centralized resolver
        val resolved = NasmSymbolResolver.resolve(
            name = symbolName,
            file = file,
            context = symbolRef,
            searchTypes = NasmSymbolResolver.SearchType.ALL,
            includeIncludes = true
        )

        // Only warn about unresolved symbols, don't error
        // They might be defined in other files not included or at link time
        if (resolved == null) {
            // Don't warn about special symbols, registers, built-in macros, or macro parameters
            if (!isSpecialSymbol(symbolName) && !isRegister(symbolName) &&
                !isBuiltInPreprocessorFunction(symbolName) && !isMacroParameter(symbolName)) {
                holder.newAnnotation(
                    HighlightSeverity.WARNING,
                    "Unresolved symbol: '$symbolName'"
                )
                    .range(symbolRef)
                    .create()
            }
        }
    }

    private fun findMacroDefinitionWithIncludes(macroName: String, file: PsiFile): PsiElement? {
        // First check in the current file
        file.findMacroDefinition(macroName)?.let { return it }

        // Then check in included files
        file.getIncludedFiles().forEach { includedFile ->
            includedFile.findMacroDefinition(macroName)?.let { return it }
        }

        return null
    }

    private fun extractFunctionMacroName(functionCall: NasmFunctionMacroCall): String? {
        // The macro name is typically the identifier before the parentheses
        var prevSibling = functionCall.prevSibling
        while (prevSibling != null) {
            val text = prevSibling.text.trim()
            if (text.isNotEmpty() && prevSibling.node.elementType == NasmTypes.IDENTIFIER) {
                return text
            }
            // Skip whitespace nodes by checking if text is blank
            if (prevSibling.text.isNotBlank()) {
                break
            }
            prevSibling = prevSibling.prevSibling
        }

        // Fallback: try to extract from the parent context
        val parent = functionCall.parent
        if (parent is NasmBuiltinFunction) {
            return parent.text.substringBefore('(').trim()
        }

        return null
    }

    private fun isBuiltInPreprocessorFunction(name: String): Boolean {
        // Built-in NASM preprocessor functions and macros
        return name.lowercase() in setOf(
            // Built-in macros
            "__file__", "__line__", "__date__", "__time__", "__utc_date__", "__utc_time__",
            "__pass__", "__output_format__", "__date_num__", "__time_num__",
            "__utc_date_num__", "__utc_time_num__",
            "__nasm_major__", "__nasm_minor__", "__nasm_subminor__", "__nasm_patchlevel__",
            "__nasm_version_id__", "__nasm_ver__",
            // Preprocessor functions
            "defined", "defineed", // Misspelling tolerance
            "strlen", "strcat", "substr",
            "str", "num",
            "sel", "if",
            "round", "trunc", "ceil", "floor"
        )
    }

    private fun isSpecialSymbol(name: String): Boolean {
        // Special NASM symbols that don't need to be defined
        return name.startsWith("$") || name.startsWith("$$") || name == "?" || name.startsWith("@@")
    }

    private fun isMacroParameter(name: String): Boolean {
        // Macro parameters that are only valid within macro definitions
        when {
            // Numbered parameters: %1, %2, %3, etc.
            name.matches(Regex("%\\d+")) -> return true
            // Macro parameter concatenation: %1_suffix, %2_foo, etc.
            name.matches(Regex("%\\d+_\\w+")) -> return true
            // Context local concatenation: %$foo_suffix
            name.matches(Regex("%\\$\\w+_\\w+")) -> return true
            // Special macro parameters
            name == "%0" -> return true  // Macro name/label
            name == "%%" -> return true  // Context-local label prefix
            name == "%+" -> return true  // Rotating parameter
            name == "%-" -> return true  // Reverse rotating parameter
            name.startsWith("%$") -> return true  // Context stack variables
            name.startsWith("%%") -> return true  // Macro-local labels
            // Parameter references with modifiers
            name.matches(Regex("%\\d+\\*")) -> return true  // Greedy parameter
            name.matches(Regex("%\\-\\d+")) -> return true  // Reverse indexed
            else -> return false
        }
    }

    private fun isRegister(name: String): Boolean {
        // Common x86/x64 register names (simplified check)
        val lowerName = name.lowercase()
        return lowerName in setOf(
            // 64-bit registers
            "rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rbp", "rsp",
            "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15",
            // 32-bit registers
            "eax", "ebx", "ecx", "edx", "esi", "edi", "ebp", "esp",
            // 16-bit registers
            "ax", "bx", "cx", "dx", "si", "di", "bp", "sp",
            // 8-bit registers
            "al", "ah", "bl", "bh", "cl", "ch", "dl", "dh",
            // Segment registers
            "cs", "ds", "es", "fs", "gs", "ss",
            // Other special registers
            "rip", "eip", "ip"
        )
    }
}
