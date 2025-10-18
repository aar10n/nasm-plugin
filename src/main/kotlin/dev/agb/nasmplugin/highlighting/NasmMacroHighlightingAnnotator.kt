package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmSymbolResolver

/**
 * Provides semantic highlighting for NASM macro definitions and invocations.
 *
 * This annotator highlights:
 * - Macro definitions (%macro, %define, %assign)
 * - Macro invocations in code
 * - Macro parameters
 * - Identifiers that resolve to macros
 */
class NasmMacroHighlightingAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            // Highlight macro definitions
            is NasmMultiLineMacro -> highlightMacroDefinition(element, holder)
            is NasmPpDefineStmt -> highlightMacroDefinition(element, holder)
            is NasmPpAssignStmt -> highlightMacroDefinition(element, holder)

            // Highlight macro invocations
            is NasmInvocation -> highlightInvocation(element, holder)
            is NasmMacroCall -> highlightMacroCall(element, holder)
            is NasmFunctionMacroCall -> highlightFunctionMacroCall(element, holder)

            // Highlight instructions that are actually macros
            is NasmInstruction -> highlightInstructionIfMacro(element, holder)

            // Highlight symbol references that resolve to macros
            is NasmSymbolRef -> highlightSymbolIfMacro(element, holder)

            // Also check any element that implements NasmSymbolReference
            is NasmSymbolReference -> {
                highlightSymbolReferenceIfMacro(element, holder)
            }

            // Check identifiers in operands (handles cases where PSI structure is complex)
            is NasmOperand -> highlightMacrosInOperand(element, holder)

            // Highlight macro references in conditional directives (%ifdef, %ifndef, etc.)
            is NasmMacroRef -> highlightMacroReference(element, holder)

            // Highlight symbol declarations (extern, global) that resolve to macros
            is NasmSymbolDecl -> highlightSymbolDeclIfMacro(element, holder)

            // Highlight preprocessor tokens (identifiers in macro definition bodies)
            is NasmPreprocessorToken -> highlightPreprocessorTokenIfMacro(element, holder)
        }
    }

    private fun highlightMacroDefinition(element: NasmNamedElement, holder: AnnotationHolder) {
        element.nameIdentifier?.let { nameId ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(nameId.textRange)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightInvocation(element: NasmInvocation, holder: AnnotationHolder) {
        val identifierNode = element.node.findChildByType(NasmTypes.IDENTIFIER) ?: return
        val name = identifierNode.text

        // Check if it resolves to a macro (including in included files)
        val containingFile = element.containingFile
        val macroDefinition = containingFile?.findMacroDefinitionWithIncludes(name)

        if (macroDefinition != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(identifierNode.textRange)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightMacroCall(element: NasmMacroCall, holder: AnnotationHolder) {
        // Find the macro name (usually the first identifier-like token)
        val identifierNode = element.node.findChildByType(NasmTypes.IDENTIFIER)
        if (identifierNode != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(identifierNode.textRange)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightFunctionMacroCall(element: NasmFunctionMacroCall, holder: AnnotationHolder) {
        // The macro name is typically before the parentheses
        element.reference?.let { ref ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(ref.rangeInElement.shiftRight(element.textRange.startOffset))
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightInstructionIfMacro(instruction: NasmInstruction, holder: AnnotationHolder) {
        // Get the mnemonic element from the instruction
        val mnemonic = instruction.mnemonic
        val mnemonicText = mnemonic.text.trim()

        // Check if this "instruction" is actually a macro (including in included files)
        val containingFile = instruction.containingFile
        val macroDefinition = containingFile?.findMacroDefinitionWithIncludes(mnemonicText)

        if (macroDefinition != null && macroDefinition is NasmMultiLineMacro) {
            // It's a multi-line macro being used as an instruction
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(mnemonic)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightSymbolIfMacro(element: NasmSymbolRef, holder: AnnotationHolder) {
        // Check if the symbol reference resolves to a macro definition
        val symbolName = element.text
        val containingFile = element.containingFile
        val resolved = NasmSymbolResolver.resolveMacro(symbolName, containingFile, true)

        if (resolved != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightSymbolReferenceIfMacro(element: NasmSymbolReference, holder: AnnotationHolder) {
        // Check if the symbol reference resolves to a macro definition
        val symbolName = (element as PsiElement).text
        val containingFile = element.containingFile
        val resolved = NasmSymbolResolver.resolveMacro(symbolName, containingFile, true)

        if (resolved != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    private fun highlightMacrosInOperand(operand: NasmOperand, holder: AnnotationHolder) {
        // Find all IDENTIFIER tokens within the operand
        val identifiers = mutableListOf<PsiElement>()
        collectIdentifiers(operand, identifiers)

        for (identifier in identifiers) {
            val text = identifier.text.trim()
            if (text.isEmpty()) continue

            // Check if this identifier is a macro
            val containingFile = operand.containingFile
            val resolved = NasmSymbolResolver.resolveMacro(text, containingFile, true)

            if (resolved != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier)
                    .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                    .create()
            }
        }
    }

    private fun collectIdentifiers(element: PsiElement, identifiers: MutableList<PsiElement>) {
        // If this is an identifier token, add it
        if (element.node?.elementType == NasmTypes.IDENTIFIER) {
            identifiers.add(element)
        }

        // Recursively search children
        for (child in element.children) {
            collectIdentifiers(child, identifiers)
        }
    }

    /**
     * Highlights macro references in conditional directives (%ifdef, %ifndef, etc.)
     * when they resolve to macro definitions.
     */
    private fun highlightMacroReference(element: NasmMacroRef, holder: AnnotationHolder) {
        // Check if this macro reference resolves to a macro definition
        val reference = element.reference
        val resolved = reference?.resolve()

        if (resolved != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                .create()
        }
    }

    /**
     * Highlights symbol declarations (extern, global) that resolve to macros.
     * For example: %define PRINTF printf; extern PRINTF
     */
    private fun highlightSymbolDeclIfMacro(element: NasmSymbolDecl, holder: AnnotationHolder) {
        // Get the symbol name (before any colon for type annotation)
        val symbolName = element.text.substringBefore(':').trim()
        if (symbolName.isEmpty()) return

        // Check if this symbol resolves to a macro
        val containingFile = element.containingFile
        val resolved = NasmSymbolResolver.resolveMacro(symbolName, containingFile, true)

        if (resolved != null) {
            // Find the IDENTIFIER token to highlight (not the whole element)
            val identifierNode = element.node.findChildByType(NasmTypes.IDENTIFIER)
            if (identifierNode != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifierNode.textRange)
                    .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                    .create()
            }
        }
    }

    /**
     * Highlights preprocessor tokens that are identifiers referencing macros.
     * This handles cases like: %define ONE 1; %define TWO (ONE + ONE)
     * where ONE in the definition of TWO should be highlighted.
     */
    private fun highlightPreprocessorTokenIfMacro(element: NasmPreprocessorToken, holder: AnnotationHolder) {
        // Check if this preprocessor token has a reference (via NasmPreprocessorTokenMixin)
        val reference = element.reference ?: return

        // Try to resolve the reference
        val resolved = reference.resolve() ?: return

        // Check if the resolved element is a macro definition
        if (resolved is NasmPpDefineStmt || resolved is NasmPpAssignStmt || resolved is NasmMultiLineMacro) {
            // Find the IDENTIFIER child token to highlight
            val identifierNode = element.node.findChildByType(NasmTypes.IDENTIFIER)
            if (identifierNode != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifierNode.textRange)
                    .textAttributes(NasmSyntaxHighlighter.MACRO_INVOCATION)
                    .create()
            }
        }
    }
}
