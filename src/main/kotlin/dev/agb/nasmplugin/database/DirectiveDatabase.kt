package dev.agb.nasmplugin.database

/**
 * Database of NASM directives
 */
object DirectiveDatabase {

    data class Directive(
        val name: String,
        val description: String
    )

    private val directiveList: List<Directive> by lazy {
        // Load directives from XML resource
        try {
            XmlDatabaseLoader.loadDirectives("/nasm/directives.xml")
        } catch (e: Exception) {
            System.err.println("ERROR: Failed to load directives from XML: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private val directives: Map<String, Directive> by lazy {
        directiveList.associateBy { it.name.lowercase() }
    }

    @JvmStatic
    fun getDirective(name: String): Directive? =
        directives[name.lowercase()]

    @JvmStatic
    fun getAllDirectives(): List<Directive> = directiveList

    @JvmStatic
    fun getDirectivesByPrefix(prefix: String): List<Directive> {
        val lowerPrefix = prefix.lowercase()
        return directiveList.filter { it.name.startsWith(lowerPrefix) }
    }
}
