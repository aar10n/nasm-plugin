package dev.agb.nasmplugin.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmSymbolResolver

/**
 * Handles Cmd+Click navigation for NASM elements.
 * - Resolves include files from both the current directory and compilation database include paths
 * - Resolves identifier references to labels, EQU constants, and macros
 */
class NasmGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        sourceElement ?: return null

        // Handle %include navigation - check if we're in an include directive
        val includeDir = PsiTreeUtil.getParentOfType(sourceElement, NasmIncludeDir::class.java)
        if (includeDir != null) {
            // Try to find a STRING token within the include directive
            val stringToken = when {
                sourceElement.node.elementType == NasmTypes.STRING -> sourceElement
                else -> includeDir.node.findChildByType(NasmTypes.STRING)?.psi
            }

            stringToken?.let { token ->
                val text = token.text ?: return@let
                // Safely extract the include path from quoted string
                val includePath = when {
                    text.length >= 2 && (text.startsWith("\"") && text.endsWith("\"")) ->
                        text.substring(1, text.length - 1)
                    text.length >= 2 && (text.startsWith("'") && text.endsWith("'")) ->
                        text.substring(1, text.length - 1)
                    else -> text
                }

                // Only proceed if we have a valid include path
                if (includePath.isNotBlank()) {
                    val containingFile = sourceElement.containingFile ?: return@let
                    containingFile.resolveIncludeFile(includePath)?.let { targetFile ->
                        return arrayOf(targetFile)
                    }
                }
            }
        }

        // Handle IDENTIFIER token navigation
        if (sourceElement.node?.elementType == NasmTypes.IDENTIFIER) {
            // Check if this identifier is part of a named element declaration
            // If so, return null to let IntelliJ's built-in "Show Usages" handle it
            val namedElement = PsiTreeUtil.getParentOfType(sourceElement, NasmNamedElement::class.java)
            if (namedElement != null && namedElement.nameIdentifier == sourceElement) {
                // This is a declaration - don't handle it here
                return null
            }

            // This is a reference/usage - try to find its declaration
            val identifierText = sourceElement.text ?: return null
            val containingFile = sourceElement.containingFile ?: return null

            // Check if this identifier is part of a symbol_ref
            val symbolRef = PsiTreeUtil.getParentOfType(sourceElement, NasmSymbolRef::class.java)
            if (symbolRef != null) {
                // Use the symbol_ref's reference resolution
                symbolRef.reference?.resolve()?.let { target ->
                    return arrayOf(target)
                }
            }

            // Otherwise, use centralized symbol resolver for all symbol types
            // This handles labels, macros, EQU constants, data definitions, etc.
            val resolved = NasmSymbolResolver.resolve(
                name = identifierText,
                file = containingFile,
                context = sourceElement,  // Pass element for local label context
                searchTypes = NasmSymbolResolver.SearchType.ALL,
                includeIncludes = true
            )

            if (resolved != null) {
                return arrayOf(resolved)
            }
        }

        return null
    }
}
