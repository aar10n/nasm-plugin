package dev.agb.nasmplugin.refactoring

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.NasmLanguage
import dev.agb.nasmplugin.psi.*

/**
 * Strategy pattern implementation for handling different types of named elements.
 * This reduces the complexity in NasmNamedElementImpl by delegating element-specific
 * logic to dedicated handlers.
 */
object NasmElementHandlers {

    interface ElementHandler {
        fun createDummyFileText(oldName: String?, newName: String): String
        fun extractNewIdentifier(dummyFile: PsiElement): PsiElement?
        fun findNameIdentifier(element: PsiElement): PsiElement?
    }

    private class LabelDefHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            // The newName might already include the dot prefix - don't add it again
            return if (newName.startsWith(".") || newName.startsWith("..")) {
                "$newName:"
            } else {
                // If newName doesn't have a dot, preserve the old label type
                when {
                    oldName?.startsWith("..") == true -> "..$newName:"
                    oldName?.startsWith(".") == true -> ".$newName:"
                    else -> "$newName:"
                }
            }
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmLabelDef::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            val labelDef = element as? NasmLabelDef ?: return null

            // Check global label
            labelDef.globalLabel?.let { globalLabel ->
                // Try direct children first (IDENTIFIER, MACRO_PARAM_REF)
                globalLabel.node.findChildByType(NasmTypes.IDENTIFIER)?.let { return it.psi }
                globalLabel.node.findChildByType(NasmTypes.MACRO_PARAM_REF)?.let { return it.psi }

                // Check if there's a CONTEXT_REF child (for MACRO_LOCAL_REF and CONTEXT_LOCAL_REF)
                globalLabel.node.findChildByType(NasmTypes.CONTEXT_REF)?.let { contextRefNode ->
                    // Look inside CONTEXT_REF for the actual token
                    contextRefNode.findChildByType(NasmTypes.MACRO_LOCAL_REF)?.let { return it.psi }
                    contextRefNode.findChildByType(NasmTypes.CONTEXT_LOCAL_REF)?.let { return it.psi }
                }
            }

            // Check local label
            labelDef.localLabel?.let { localLabel ->
                localLabel.node.findChildByType(NasmTypes.IDENTIFIER)?.let { return it.psi }
                localLabel.node.findChildByType(NasmTypes.MACRO_LOCAL_REF)?.let { return it.psi }
            }

            return null
        }
    }

    private class PpDefineStmtHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "%define $newName 1"
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmPpDefineStmt::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            val defineStmt = element as? NasmPpDefineStmt ?: return null

            defineStmt.macroName?.let { macroName ->
                // Get IDENTIFIER from macro_name
                return macroName.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
                    ?: macroName.node.findChildByType(NasmTypes.PREPROCESSOR_DIRECTIVE)?.psi
            }

            // Fallback: if macroName is null, try to find IDENTIFIER as direct child
            return element.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
        }
    }

    private class PpAssignStmtHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "%assign $newName 1"
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmPpAssignStmt::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            return element.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
        }
    }

    private class MultiLineMacroHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "%macro $newName 0\n%endmacro"
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmMultiLineMacro::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            // For NasmMultiLineMacro, get the macro_name element
            val multiLineMacro = element as? NasmMultiLineMacro ?: return null
            val macroName = multiLineMacro.macroName

            // The macro_name element contains the IDENTIFIER as its first (and only) child
            val firstChild = macroName.firstChild
            if (firstChild != null && firstChild.node?.elementType == NasmTypes.IDENTIFIER) {
                return firstChild
            }

            // Alternative: try getting first node child
            val nodeChildren = macroName.node.getChildren(null)
            if (nodeChildren.isNotEmpty() && nodeChildren[0].elementType == NasmTypes.IDENTIFIER) {
                return nodeChildren[0].psi
            }

            // Fallback: return the macro_name element itself if it has text
            return if (macroName.text.isNotEmpty()) macroName else null
        }
    }

    private class EquDefinitionHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "$newName equ 0"
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmEquDefinition::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            return element.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
        }
    }

    private class SymbolDeclHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "global $newName"
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            return PsiTreeUtil.findChildrenOfType(dummyFile, NasmSymbolDecl::class.java)
                .firstOrNull()?.nameIdentifier
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            val symbolDecl = element as? NasmSymbolDecl ?: return null

            // Get the first symbol_name child
            val symbolName = symbolDecl.symbolNameList.firstOrNull() ?: return null

            // Look for IDENTIFIER inside the symbol_name
            return symbolName.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
                ?: symbolName.node.findChildByType(NasmTypes.SECTION_ATTR_KW)?.psi
                ?: symbolName.node.findChildByType(NasmTypes.SIZE_SPEC)?.psi
        }
    }

    private class DefaultHandler : ElementHandler {
        override fun createDummyFileText(oldName: String?, newName: String): String {
            return "$newName:"  // Default to label syntax
        }

        override fun extractNewIdentifier(dummyFile: PsiElement): PsiElement? {
            // Fallback: find any IDENTIFIER with matching text
            return PsiTreeUtil.collectElementsOfType(dummyFile, PsiElement::class.java)
                .find { it.node?.elementType == NasmTypes.IDENTIFIER }
        }

        override fun findNameIdentifier(element: PsiElement): PsiElement? {
            // Try various token types
            return element.node.findChildByType(NasmTypes.IDENTIFIER)?.psi
                ?: element.node.findChildByType(NasmTypes.MACRO_LOCAL_REF)?.psi
                ?: element.node.findChildByType(NasmTypes.CONTEXT_LOCAL_REF)?.psi
        }
    }

    private val handlers = mapOf<Class<*>, ElementHandler>(
        NasmLabelDef::class.java to LabelDefHandler(),
        NasmPpDefineStmt::class.java to PpDefineStmtHandler(),
        NasmPpAssignStmt::class.java to PpAssignStmtHandler(),
        NasmMultiLineMacro::class.java to MultiLineMacroHandler(),
        NasmEquDefinition::class.java to EquDefinitionHandler(),
        NasmSymbolDecl::class.java to SymbolDeclHandler()
    )

    private val defaultHandler = DefaultHandler()

    fun getHandler(element: PsiElement): ElementHandler {
        // Check for interface/superclass matches
        for ((clazz, handler) in handlers) {
            if (clazz.isAssignableFrom(element::class.java)) {
                return handler
            }
        }
        return defaultHandler
    }

    /**
     * Sets a new name for the given named element.
     */
    fun setName(element: PsiElement, newName: String): PsiElement {
        val nameIdentifier = getNameIdentifier(element) ?: return element
        val handler = getHandler(element)
        val oldName = (element as? NasmNamedElement)?.name

        val dummyFileText = handler.createDummyFileText(oldName, newName)

        try {
            val dummyFile = PsiFileFactory.getInstance(element.project).createFileFromText(
                "dummy.nasm",
                NasmLanguage,
                dummyFileText
            )

            val newIdentifier = handler.extractNewIdentifier(dummyFile)
            if (newIdentifier != null) {
                nameIdentifier.replace(newIdentifier)
            }
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }

        return element
    }

    /**
     * Gets the name identifier for the given named element.
     */
    fun getNameIdentifier(element: PsiElement): PsiElement? {
        return getHandler(element).findNameIdentifier(element)
    }
}