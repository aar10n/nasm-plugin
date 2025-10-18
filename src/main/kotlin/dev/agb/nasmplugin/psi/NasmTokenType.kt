package dev.agb.nasmplugin.psi

import com.intellij.psi.tree.IElementType
import dev.agb.nasmplugin.NasmLanguage

class NasmTokenType(debugName: String) : IElementType(debugName, NasmLanguage) {

    override fun toString(): String = "NasmTokenType.${super.toString()}"
}
