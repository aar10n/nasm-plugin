package dev.agb.nasmplugin

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmMacroBodyIdentifierReference
import dev.agb.nasmplugin.psi.impl.NasmPsiUtils.isInMacroDefinitionBody

/**
 * Contributes references for NASM elements
 * This ensures IntelliJ knows to look for references in our PSI elements
 */
class NasmReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Register for symbol references (labels, data, equ constants)
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(NasmSymbolRef::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    return (element as? NasmSymbolRef)?.reference?.let { ref ->
                        arrayOf(ref)
                    } ?: PsiReference.EMPTY_ARRAY
                }
            }
        )

        // Note: NasmSymbolDecl provides its own references via getReferences() mixin
        // DO NOT register it here - it causes duplicates and doesn't fix find usages

        // Note: NasmMacroArg provides its own references via getReferences() mixin

        // Register for macro calls
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(NasmMacroCall::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    return (element as? NasmMacroCall)?.reference?.let { ref ->
                        arrayOf(ref)
                    } ?: PsiReference.EMPTY_ARRAY
                }
            }
        )

        // Register for instructions (which might be macro calls)
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(NasmInstruction::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    return (element as? NasmInstruction)?.reference?.let { ref ->
                        arrayOf(ref)
                    } ?: PsiReference.EMPTY_ARRAY
                }
            }
        )

        // Register for IDENTIFIER tokens within macro definition bodies
        // Note: macro references in conditional directives (%ifdef, %ifndef) are now
        // handled by the macro_ref grammar element with its mixin NasmMacroReferenceImpl
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withLanguage(NasmLanguage),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    // Only process IDENTIFIER tokens
                    if (element.node?.elementType != NasmTypes.IDENTIFIER) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    // Create references for identifiers within macro definition bodies
                    if (element.isInMacroDefinitionBody()) {
                        return arrayOf(NasmMacroBodyIdentifierReference(element))
                    }

                    return PsiReference.EMPTY_ARRAY
                }
            }
        )

        // Note: %include file navigation is handled by NasmGotoDeclarationHandler
        // Note: macro references in conditional directives are handled by macro_ref grammar element
    }
}
