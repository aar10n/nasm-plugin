package dev.agb.nasmplugin.database

/**
 * Centralized database for x86/x86-64 assembly instructions, loaded from an XML resource.
 */
object InstructionDatabase {

    /**
     * Instruction category classification
     */
    enum class Category {
        DATA_MOVEMENT,
        ARITHMETIC,
        LOGIC,
        CONTROL_FLOW,
        STRING,
        BIT_MANIPULATION,
        SYSTEM,
        FLOATING_POINT,
        SIMD,
        OTHER
    }

    /**
     * Operand type specification for instruction validation
     */
    enum class OperandType {
        REG,        // Register (any size)
        REG8,       // 8-bit register
        REG16,      // 16-bit register
        REG32,      // 32-bit register
        REG64,      // 64-bit register
        MEM,        // Memory reference
        IMM,        // Immediate value
        IMM8,       // 8-bit immediate
        IMM16,      // 16-bit immediate
        IMM32,      // 32-bit immediate
        IMM64,      // 64-bit immediate
        LABEL,      // Label reference
        R_M,        // Register or memory
        R_M8,       // Register or memory (8-bit)
        R_M16,      // Register or memory (16-bit)
        R_M32,      // Register or memory (32-bit)
        R_M64,      // Register or memory (64-bit)
        SREG,       // Segment register
        CREG,       // Control register
        DREG,       // Debug register
        XMM,        // XMM register (SSE)
        YMM,        // YMM register (AVX)
        ZMM,        // ZMM register (AVX-512)
        MM,         // MMX register
        ST,         // FPU register
        ANY         // Any operand type (for flexible instructions)
    }

    /**
     * Operand specification - represents one operand in an instruction variant
     */
    data class OperandSpec(
        val type: OperandType,
        val optional: Boolean = false
    )

    /**
     * Instruction variant - represents one valid operand combination
     * For example, MOV has variants like (REG64, REG64), (REG64, IMM64), (REG64, MEM), etc.
     */
    data class InstructionVariant(
        val operands: List<OperandSpec>
    ) {
        val operandCount: Int get() = operands.count { !it.optional }
        val maxOperandCount: Int get() = operands.size
    }

    /**
     * Documentation information for an instruction
     */
    data class InstructionDocumentation(
        val summary: String,
        val description: String,
        val operation: String,
        val flagsAffected: String,
        val notes: String
    ) {
        val isEmpty: Boolean
            get() = summary.isEmpty() && description.isEmpty() && operation.isEmpty() &&
                    flagsAffected.isEmpty() && notes.isEmpty()
    }

    /**
     * Unified instruction entry with both specification and documentation
     */
    data class UnifiedInstruction(
        val name: String,
        val description: String,
        val category: Category,
        val variants: List<InstructionVariant>,
        val documentation: InstructionDocumentation?
    ) {
        /**
         * Returns true if this instruction has detailed documentation
         */
        val hasDocumentation: Boolean
            get() = documentation != null && !documentation.isEmpty

        /**
         * Returns true if this instruction accepts any number/type of operands
         * (used for instructions without specified variants)
         */
        val acceptsAnyOperands: Boolean
            get() = variants.isEmpty()
    }

    private val instructionList: List<UnifiedInstruction> by lazy {
        // Load unified instructions from XML resource
        try {
            XmlDatabaseLoader.loadUnifiedInstructions("/nasm/instructions.xml")
        } catch (e: Exception) {
            System.err.println("ERROR: Failed to load unified instructions from XML: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private val instructions: Map<String, UnifiedInstruction> by lazy {
        instructionList.associateBy { it.name.lowercase() }
    }

    @JvmStatic
    fun getInstruction(name: String): UnifiedInstruction? =
        instructions[name.lowercase()]

    @JvmStatic
    fun getAllInstructions(): List<UnifiedInstruction> = instructionList

    @JvmStatic
    fun getInstructionsByPrefix(prefix: String): List<UnifiedInstruction> {
        val lowerPrefix = prefix.lowercase()
        return instructionList.filter { it.name.startsWith(lowerPrefix) }
    }

    @JvmStatic
    fun getInstructionsWithDocumentation(): List<UnifiedInstruction> =
        instructionList.filter { it.hasDocumentation }
}
