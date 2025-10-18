package dev.agb.nasmplugin

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Icon constants for NASM plugin.
 */
object NasmIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/nasm-file.svg", NasmIcons::class.java)

    @JvmField
    val NASM: Icon = IconLoader.getIcon("/icons/nasm.svg", NasmIcons::class.java)
}
