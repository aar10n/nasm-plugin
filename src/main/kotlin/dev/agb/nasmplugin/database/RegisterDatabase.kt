package dev.agb.nasmplugin.database

/**
 * Database of x86_64 registers with sizes and descriptions
 */
object RegisterDatabase {

    enum class RegisterSize(val displayName: String) {
        BIT_8("8-bit"),
        BIT_16("16-bit"),
        BIT_32("32-bit"),
        BIT_64("64-bit"),
        BIT_128("128-bit"),
        BIT_256("256-bit"),
        BIT_512("512-bit"),
        SEGMENT("Segment"),
        CONTROL("Control"),
        DEBUG("Debug"),
        FPU("FPU"),
        MMX("MMX")
    }

    data class Register(
        val name: String,
        val description: String,
        val size: RegisterSize
    )

    private val registerList: List<Register> by lazy {
        // Load registers from XML resource
        try {
            XmlDatabaseLoader.loadRegisters("/nasm/registers.xml")
        } catch (e: Exception) {
            System.err.println("ERROR: Failed to load registers from XML: ${e.message}")
            emptyList()
        }
    }

    private val registers: Map<String, Register> by lazy {
        registerList.associateBy { it.name.lowercase() }
    }

    @JvmStatic
    fun getRegister(name: String): Register? =
        registers[name.lowercase()]

    @JvmStatic
    fun getAllRegisters(): List<Register> = registerList

    @JvmStatic
    fun getRegistersByPrefix(prefix: String): List<Register> {
        val lowerPrefix = prefix.lowercase()
        return registerList.filter { it.name.startsWith(lowerPrefix) }
    }
}
