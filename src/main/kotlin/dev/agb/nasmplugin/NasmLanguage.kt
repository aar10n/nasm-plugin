package dev.agb.nasmplugin

import com.intellij.lang.Language

/**
 * NASM assembly language definition.
 */
object NasmLanguage : Language("NASM") {
    private fun readResolve(): Any = NasmLanguage
}
