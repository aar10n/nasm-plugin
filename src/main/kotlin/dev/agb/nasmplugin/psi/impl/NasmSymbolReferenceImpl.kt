package dev.agb.nasmplugin.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import dev.agb.nasmplugin.psi.*

/**
 * Implementation for symbol references (data labels, equ constants) that provides go-to-definition support
 */
abstract class NasmSymbolReferenceImpl(node: ASTNode) : CachedReferenceProvider(node), NasmSymbolReference {

    override fun createReference(): PsiReference = SymbolReference(this)

    private class SymbolReference(element: PsiElement) :
        PsiReferenceBase<PsiElement>(element, TextRange.from(0, element.textLength)) {

        private val LOG = Logger.getInstance(SymbolReference::class.java)

        override fun resolve(): PsiElement? {
            val symbolName = element.text
            val containingFile = element.containingFile ?: return null

            // Use centralized resolver for all symbol types
            val nasmResult = NasmSymbolResolver.resolve(
                name = symbolName,
                file = containingFile,
                context = element,  // Pass element for local label context
                searchTypes = NasmSymbolResolver.SearchType.ALL,
                includeIncludes = true
            )

            // If found in NASM, return it
            if (nasmResult != null) {
                return nasmResult
            }

            // If not found in NASM, try to resolve to C/C++ symbol (for extern references)
            // This enables Find Usages to work from C to NASM
            return tryResolveToCpp(symbolName, element)
        }

        private fun tryResolveToCpp(symbolName: String, element: PsiElement): PsiElement? {
            return try {
                // Use reflection to check if CLion/CIDR classes are available
                val resolverClass = Class.forName("dev.agb.nasmplugin.clion.NasmToCppReferenceResolver")
                val method = resolverClass.getMethod("resolveToCppSymbol", String::class.java, com.intellij.openapi.project.Project::class.java)
                val resolverObject = resolverClass.getField("INSTANCE").get(null)
                method.invoke(resolverObject, symbolName, element.project) as? PsiElement
            } catch (e: Exception) {
                // CLion not available or class not found - just return null
                null
            }
        }

        override fun getVariants(): Array<Any> {
            // Completion is handled by NasmCompletionContributor
            // Returning empty array here prevents duplicate completions
            return emptyArray()
        }
    }
}
