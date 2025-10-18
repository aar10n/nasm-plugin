package dev.agb.nasmplugin.completion

/**
 * Represents different completion contexts in NASM code.
 * Each context defines which symbol types are relevant and their priority.
 */
sealed class CompletionContext {
    /** The types of symbols that should be shown in this context */
    abstract val allowedSymbolTypes: Set<NasmCompletionVariantBuilder.VariantType>

    /** Priority for user-defined symbols in this context */
    abstract val userSymbolPriority: Double

    /** Whether to show registers in this context */
    open val showRegisters: Boolean = false

    /** Whether to show instructions in this context */
    open val showInstructions: Boolean = false

    /** Whether to show directives in this context */
    open val showDirectives: Boolean = false

    /** Whether to show preprocessor functions in this context */
    open val showPreprocessorFunctions: Boolean = false

    /**
     * Jump/Call target context - after jump or call instructions
     * Example: `jmp <cursor>`, `call <cursor>`
     */
    object JumpTarget : CompletionContext() {
        override val allowedSymbolTypes = setOf(
            NasmCompletionVariantBuilder.VariantType.LABELS,
            NasmCompletionVariantBuilder.VariantType.MULTI_LINE_MACROS
        )
        override val userSymbolPriority = 95.0
    }

    /**
     * Memory operand context - inside square brackets
     * Example: `mov rax, [<cursor>]`
     */
    object MemoryOperand : CompletionContext() {
        override val allowedSymbolTypes = setOf(
            NasmCompletionVariantBuilder.VariantType.LABELS,
            NasmCompletionVariantBuilder.VariantType.EQU_CONSTANTS,
            NasmCompletionVariantBuilder.VariantType.SINGLE_LINE_MACROS,
            NasmCompletionVariantBuilder.VariantType.ASSIGNS
        )
        override val userSymbolPriority = 92.0
        override val showRegisters = true
        override val showPreprocessorFunctions = true
    }

    /**
     * Data instruction operand - after mov, add, etc.
     * Example: `mov rax, <cursor>`
     */
    object DataOperand : CompletionContext() {
        override val allowedSymbolTypes = NasmCompletionVariantBuilder.VariantType.entries.toSet()
        override val userSymbolPriority = 90.0
        override val showRegisters = true
        override val showPreprocessorFunctions = true
    }

    /**
     * Global directive context - after `global` keyword
     * Example: `global <cursor>`
     */
    object GlobalDirective : CompletionContext() {
        override val allowedSymbolTypes = setOf(
            NasmCompletionVariantBuilder.VariantType.LABELS,
            NasmCompletionVariantBuilder.VariantType.MULTI_LINE_MACROS
        )
        override val userSymbolPriority = 100.0
    }

    /**
     * Extern directive context - after `extern` keyword
     * Example: `extern <cursor>`
     */
    object ExternDirective : CompletionContext() {
        override val allowedSymbolTypes = setOf(
            NasmCompletionVariantBuilder.VariantType.EXTERNS
        )
        override val userSymbolPriority = 100.0
    }

    /**
     * Line start context - at the beginning of a line
     * Example: `\n<cursor>`
     */
    object LineStart : CompletionContext() {
        override val allowedSymbolTypes = setOf(
            NasmCompletionVariantBuilder.VariantType.LABELS,
            NasmCompletionVariantBuilder.VariantType.EQU_CONSTANTS,
            NasmCompletionVariantBuilder.VariantType.SINGLE_LINE_MACROS,
            NasmCompletionVariantBuilder.VariantType.MULTI_LINE_MACROS,
            NasmCompletionVariantBuilder.VariantType.ASSIGNS,
            NasmCompletionVariantBuilder.VariantType.EXTERNS
        )
        override val userSymbolPriority = 105.0  // Higher priority for macros at line start
        override val showInstructions = true
        override val showDirectives = true
    }

    /**
     * After percent sign - typing preprocessor directive
     * Example: `%<cursor>`
     */
    object AfterPercent : CompletionContext() {
        override val allowedSymbolTypes = emptySet<NasmCompletionVariantBuilder.VariantType>()
        override val userSymbolPriority = 0.0
        override val showDirectives = true
        override val showPreprocessorFunctions = true
    }

    /**
     * General/unknown context - fallback when context is unclear
     */
    object General : CompletionContext() {
        override val allowedSymbolTypes = NasmCompletionVariantBuilder.VariantType.entries.toSet()
        override val userSymbolPriority = 850.0
        override val showRegisters = true
        override val showInstructions = true
        override val showDirectives = true
        override val showPreprocessorFunctions = true
    }
}
