package dev.agb.nasmplugin.psi.impl

import com.intellij.icons.AllIcons
import dev.agb.nasmplugin.NasmIcons
import javax.swing.Icon

/**
 * Centralized icon provider for NASM elements.
 * Ensures consistent icon usage across the plugin.
 */
object NasmIconProvider {
    /**
     * Gets the appropriate icon for the given element type.
     * Uses distinct icons to help users visually differentiate between element types.
     */
    fun getIconFor(elementType: ElementType): Icon {
        return when (elementType) {
            ElementType.LABEL -> AllIcons.Nodes.Method  // Labels are like functions
            ElementType.MACRO -> AllIcons.Nodes.Function  // Macros are like functions
            ElementType.DEFINE -> AllIcons.Nodes.Field  // Preprocessor defines are like fields
            ElementType.ASSIGN -> AllIcons.Nodes.Variable  // Assigns are numeric variables
            ElementType.CONSTANT -> AllIcons.Nodes.Constant  // EQU constants
            ElementType.EXTERN -> AllIcons.Nodes.PpLib  // External symbols
            ElementType.DEFAULT -> NasmIcons.NASM  // Default NASM icon
        }
    }
}

/**
 * Types of NASM elements for icon selection.
 */
enum class ElementType {
    LABEL,
    MACRO,
    DEFINE,
    ASSIGN,
    CONSTANT,
    EXTERN,
    DEFAULT
}