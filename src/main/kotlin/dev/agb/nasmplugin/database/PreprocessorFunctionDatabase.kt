package dev.agb.nasmplugin.database

/**
 * Database of NASM preprocessor functions (used in operand/argument contexts)
 */
object PreprocessorFunctionDatabase {

    data class PreprocessorFunction(
        val name: String,
        val description: String
    )

    private val functionList: List<PreprocessorFunction> by lazy {
        // Load preprocessor functions from XML resource
        try {
            XmlDatabaseLoader.loadPreprocessorFunctions("/nasm/preprocessor-functions.xml")
        } catch (e: Exception) {
            System.err.println("ERROR: Failed to load preprocessor functions from XML: ${e.message}")
            emptyList()
        }
    }

    private val functions: Map<String, PreprocessorFunction> by lazy {
        functionList.associateBy { it.name.lowercase() }
    }

    @JvmStatic
    fun getPreprocessorFunction(name: String): PreprocessorFunction? =
        functions[name.lowercase()]

    @JvmStatic
    fun getAllPreprocessorFunctions(): List<PreprocessorFunction> = functionList

    @JvmStatic
    fun getPreprocessorFunctionsByPrefix(prefix: String): List<PreprocessorFunction> {
        val lowerPrefix = prefix.lowercase()
        return functionList.filter { it.name.startsWith(lowerPrefix) }
    }
}
