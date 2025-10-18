package dev.agb.nasmplugin.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent project-level settings for NASM plugin.
 * Stores configuration like include paths that aren't available from compilation database.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "NasmProjectSettings",
    storages = [Storage("nasmSettings.xml")]
)
class NasmProjectSettings : PersistentStateComponent<NasmProjectSettings>, ModificationTracker {

    private val modificationTracker = SimpleModificationTracker()

    /**
     * List of include paths for NASM %include directives.
     * These paths can be:
     * - Relative to project root (e.g., "include/", "lib/headers/")
     * - Absolute paths (e.g., "/usr/local/include/nasm/")
     *
     * These are used as fallback when include paths cannot be determined from
     * compile_commands.json or other build system integration.
     */
    var includePaths: MutableList<String> = mutableListOf()

    /**
     * Command-line macro definitions (equivalent to NASM -D flag).
     * Format: comma-separated list of "MACRO[=value]" entries.
     * Examples:
     * - "DEBUG" (defines DEBUG with no value)
     * - "DEBUG,VERSION=2" (defines DEBUG and VERSION=2)
     * - "OS=LINUX,ARCH=x64,DEBUG" (multiple definitions)
     *
     * These macros are treated as if they were defined with %define at the
     * start of each file.
     */
    var commandLineMacros: String = ""
        set(value) {
            field = value
            modificationTracker.incModificationCount()
        }

    override fun getState(): NasmProjectSettings = this

    override fun loadState(state: NasmProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
        modificationTracker.incModificationCount()
    }

    override fun getModificationCount(): Long = modificationTracker.modificationCount

    /**
     * Increment modification counter when settings change.
     * Call this after modifying includePaths or other settings.
     */
    fun notifyChanged() {
        modificationTracker.incModificationCount()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): NasmProjectSettings {
            return project.getService(NasmProjectSettings::class.java)
        }
    }
}
