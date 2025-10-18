package dev.agb.nasmplugin.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import dev.agb.nasmplugin.psi.NasmNamedElement
import dev.agb.nasmplugin.psi.NasmTypes
import dev.agb.nasmplugin.psi.NasmElementPresentation
import dev.agb.nasmplugin.psi.getIncludedFiles
import dev.agb.nasmplugin.psi.getIncludingFiles
import dev.agb.nasmplugin.psi.findGlobalSymbols
import dev.agb.nasmplugin.refactoring.NasmElementHandlers

/**
 * Base implementation for named elements (labels, macros)
 */
abstract class NasmNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), NasmNamedElement {

    override fun getName(): String? {
        val nameIdentifier = getNameIdentifier()
        if (nameIdentifier != null) {
            val text = nameIdentifier.text
            // Remove trailing colon from label definitions
            return if (text.endsWith(":")) {
                text.dropLast(1)
            } else {
                text
            }
        }
        return null
    }

    override fun setName(newName: String): PsiElement {
        // Delegate to NasmElementHandlers which centralizes this logic
        return NasmElementHandlers.setName(this, newName)
    }

    override fun getNameIdentifier(): PsiElement? {
        // Delegate to NasmElementHandlers which centralizes this logic
        return NasmElementHandlers.getNameIdentifier(this)
    }

    override fun getPresentation(): ItemPresentation = NasmElementPresentation(this)

    override fun getTextOffset(): Int {
        val nameIdentifier = getNameIdentifier()
        return nameIdentifier?.textOffset ?: super.getTextOffset()
    }

    override fun getNavigationElement(): PsiElement {
        val nameIdentifier = getNameIdentifier()
        return nameIdentifier ?: super.getNavigationElement()
    }

    override fun getUseScope(): SearchScope {
        val containingFile = containingFile ?: return super.getUseScope()
        val defaultScope = super.getUseScope()

        // Check if this symbol is marked as global
        val isGlobalSymbol = isMarkedAsGlobal()

        // If this is a global symbol, it can be used in any NASM file in the project
        if (isGlobalSymbol) {
            return GlobalSearchScope.projectScope(project)
        }

        // Build the set of files where this symbol can be used
        val files = mutableSetOf(containingFile)

        // Add files that are included by this file (forward includes)
        // This handles: .asm includes .inc, search for symbols from .asm used in .inc
        files.addAll(containingFile.getIncludedFiles())

        // Add files that include this file (reverse includes)
        // This handles: .inc defines symbol, search for usages in .asm that includes .inc
        files.addAll(containingFile.getIncludingFiles())

        // For each file that includes this file, also add their includes
        // This handles transitive includes: A.asm includes B.inc includes C.inc
        val includingFiles = containingFile.getIncludingFiles()
        for (includingFile in includingFiles) {
            files.addAll(includingFile.getIncludedFiles())
        }

        // Convert to virtual files and create scope
        val virtualFiles = files.mapNotNull { it.virtualFile }
        if (virtualFiles.isEmpty()) {
            return defaultScope
        }

        // Create a file-based scope and union with the default scope
        val fileScope = GlobalSearchScope.filesScope(project, virtualFiles)

        // Union the scopes to include both our specific files and the default scope
        return if (defaultScope is GlobalSearchScope) {
            defaultScope.union(fileScope)
        } else {
            fileScope
        }
    }

    /**
     * Checks if this named element is marked as global in any global directive
     */
    private fun isMarkedAsGlobal(): Boolean {
        val symbolName = name ?: return false
        val file = containingFile ?: return false

        // Check if this file has a global declaration for this symbol
        val globalSymbols = file.findGlobalSymbols()
        return globalSymbols.any {
            (it as? NasmNamedElement)?.name?.equals(symbolName, ignoreCase = true) == true
        }
    }
}
