package dev.agb.nasmplugin.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.light.LightElement
import dev.agb.nasmplugin.NasmLanguage
import dev.agb.nasmplugin.psi.NasmNamedElement
import dev.agb.nasmplugin.psi.NasmElementPresentation
import dev.agb.nasmplugin.settings.CommandLineMacro

/**
 * Light PSI element representing a command-line defined macro (from -D flag / project settings).
 * This allows command-line macros to participate in reference resolution, find usages,
 * and documentation generation.
 */
class NasmCommandLineMacroElement(
    private val macro: CommandLineMacro,
    private val containingFile: PsiFile
) : LightElement(containingFile.manager, NasmLanguage), NasmNamedElement {

    override fun getName(): String = macro.name

    override fun setName(name: String): PsiElement {
        // Command-line macros are not renameable through the IDE
        // since they come from project settings
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        // No real identifier token exists for command-line macros
        return null
    }

    override fun getPresentation(): ItemPresentation {
        return NasmElementPresentation(this)
    }

    override fun toString(): String {
        return "NasmCommandLineMacro($name)"
    }

    override fun getContainingFile(): PsiFile {
        return containingFile
    }

    override fun getText(): String {
        // Return the macro definition as it would appear in NASM source
        // This is used for documentation generation
        return "%define ${macro.definitionText}"
    }

    /**
     * Returns the macro value, or null if undefined.
     */
    fun getMacroValue(): String? = macro.value

    /**
     * Returns the underlying CommandLineMacro.
     */
    fun getCommandLineMacro(): CommandLineMacro = macro

    /**
     * Returns true since this represents a single-line macro definition.
     */
    fun isSingleLineMacro(): Boolean = true
}
