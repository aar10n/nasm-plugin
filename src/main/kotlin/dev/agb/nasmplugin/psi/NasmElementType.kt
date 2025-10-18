package dev.agb.nasmplugin.psi

import com.intellij.psi.tree.IElementType
import dev.agb.nasmplugin.NasmLanguage

class NasmElementType(debugName: String) : IElementType(debugName, NasmLanguage)
