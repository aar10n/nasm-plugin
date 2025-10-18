package dev.agb.nasmplugin.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import dev.agb.nasmplugin.psi.*

/**
 * Mixin for symbol declarations in global/extern statements that provides both naming and reference support.
 * Symbol declarations like "global my_label" should create a reference to the actual label definition.
 */
abstract class NasmSymbolDeclMixin(node: ASTNode) : NasmNamedElementImpl(node), NasmSymbolDecl {

    override fun getUseScope(): SearchScope {
        // Symbol declarations in global/extern statements should be searchable across the entire project
        // because they represent cross-file symbol visibility declarations
        return GlobalSearchScope.projectScope(project)
    }

    override fun getReference(): PsiReference? {
        // Don't use caching - always create a fresh reference
        // The IntelliJ platform will handle caching at a higher level
        return SymbolDeclReference(this)
    }

    override fun getReferences(): Array<PsiReference> {
        val ref = reference
        return if (ref != null) arrayOf(ref) else PsiReference.EMPTY_ARRAY
    }

    /**
     * Reference from a symbol declaration (in global/extern) to the actual symbol definition
     */
    private class SymbolDeclReference(element: PsiElement) :
        PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength)) {

        override fun resolve(): PsiElement? {
            val symbolName = element.text.substringBefore(':') // Handle "name:type" format
            val containingFile = element.containingFile ?: return null

            // Determine the search strategy based on context
            val searchTypes = if (isInExternDeclaration()) {
                // For extern declarations:
                // - Search for the actual label/symbol definition (not other extern declarations)
                // - First search in current file and includes for the label definition
                // - If not found locally, search for global labels in other assembly files
                // - Exclude EXTERNS to prevent resolving to itself
                NasmSymbolResolver.SearchType.ALL - NasmSymbolResolver.SearchType.EXTERNS
            } else if (isInGlobalDeclaration()) {
                // For global declarations:
                // - Search for the actual label definition in current file and includes only
                // - Don't search other files - the global directive just exports an existing local label
                NasmSymbolResolver.SearchType.ALL - setOf(
                    NasmSymbolResolver.SearchType.GLOBAL_LABELS,
                    NasmSymbolResolver.SearchType.EXTERNS
                )
            } else {
                // Default search
                NasmSymbolResolver.SearchType.ALL
            }

            // First, try to resolve as NASM symbol
            val nasmSymbol = NasmSymbolResolver.resolve(
                name = symbolName,
                file = containingFile,
                context = element,  // Pass element for local label context
                searchTypes = searchTypes,
                includeIncludes = true
            )

            if (nasmSymbol != null) {
                return nasmSymbol
            }

            // If not found in NASM and this is an extern declaration, try C/C++ resolution
            if (isInExternDeclaration()) {
                return resolveToCppSymbol(symbolName)
            }

            return null
        }

        /**
         * Check if this symbol declaration is part of an extern directive.
         */
        private fun isInExternDeclaration(): Boolean {
            val symbolList = element.parent ?: return false
            val externDir = symbolList.parent ?: return false
            return externDir is NasmExternDir
        }

        /**
         * Check if this symbol declaration is part of a global directive.
         */
        private fun isInGlobalDeclaration(): Boolean {
            val symbolList = element.parent ?: return false
            val globalDir = symbolList.parent ?: return false
            return globalDir is NasmGlobalDir
        }

        /**
         * Try to resolve to C/C++ symbol using CLion CIDR APIs.
         * Returns null if CLion support is not available or symbol not found.
         */
        private fun resolveToCppSymbol(symbolName: String): PsiElement? {
            val project = element.project

            // Only available in CLion - gracefully handle missing CIDR classes
            return try {
                // Check if C/C++ support is available
                val resolverClass = Class.forName("dev.agb.nasmplugin.clion.NasmToCppReferenceResolver")
                val resolveMethod = resolverClass.getMethod("resolveToCppSymbol", String::class.java, com.intellij.openapi.project.Project::class.java)
                val resolverInstance = resolverClass.kotlin.objectInstance
                resolveMethod.invoke(resolverInstance, symbolName, project) as? PsiElement
            } catch (e: ClassNotFoundException) {
                // CIDR classes not available (not running in CLion)
                null
            } catch (e: NoClassDefFoundError) {
                null
            } catch (e: Exception) {
                // Any other error - log and return null
                null
            }
        }

        override fun getVariants(): Array<Any> = emptyArray()
    }
}