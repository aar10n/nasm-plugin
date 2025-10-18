package dev.agb.nasmplugin.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.search.FileTypeIndex
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.psi.*

/**
 * Provides "Navigate to Symbol" functionality for NASM assembly files.
 *
 * Enables quick navigation via Ctrl+Alt+Shift+N (or Cmd+Option+O on Mac) to:
 * - Labels
 * - Macros
 * - Constants (EQU)
 * - Global symbols
 * - Extern symbols
 */
class NasmChooseByNameContributor : ChooseByNameContributorEx {

    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?
    ) {
        val project = scope.project ?: return

        // Find all NASM files in scope
        val nasmFiles = FileTypeIndex.getFiles(NasmFileType, scope)

        // Collect all symbol names from NASM files
        val psiManager = PsiManager.getInstance(project)
        nasmFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) ?: return@forEach

            // Find all named elements
            PsiTreeUtil.findChildrenOfAnyType(
                psiFile,
                NasmLabelDef::class.java,
                NasmMultiLineMacro::class.java,
                NasmEquDefinition::class.java,
                NasmGlobalDir::class.java,
                NasmExternDir::class.java
            ).forEach { element ->
                when (element) {
                    is NasmNamedElement -> {
                        element.name?.let { processor.process(it) }
                    }
                    is NasmGlobalDir -> {
                        // Extract symbol names from global directive
                        element.symbolList?.symbolDeclList?.forEach { symbolDecl ->
                            processor.process(symbolDecl.text)
                        }
                    }
                    is NasmExternDir -> {
                        // Extract symbol names from extern directive
                        element.symbolList?.symbolDeclList?.forEach { symbolDecl ->
                            processor.process(symbolDecl.text)
                        }
                    }
                }
            }
        }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        val project = parameters.project
        val scope = parameters.searchScope

        // Find all NASM files in scope
        val nasmFiles = FileTypeIndex.getFiles(NasmFileType, scope)

        // Search for elements with the given name
        val psiManager = PsiManager.getInstance(project)
        nasmFiles.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) ?: return@forEach

            // Find all named elements with this name
            PsiTreeUtil.findChildrenOfAnyType(
                psiFile,
                NasmLabelDef::class.java,
                NasmMultiLineMacro::class.java,
                NasmEquDefinition::class.java,
                NasmGlobalDir::class.java,
                NasmExternDir::class.java
            ).forEach { element ->
                when (element) {
                    is NasmNamedElement -> {
                        if (element.name == name && element is NavigationItem) {
                            processor.process(element)
                        }
                    }
                    is NasmGlobalDir -> {
                        // Check if this global directive declares the symbol
                        element.symbolList?.symbolDeclList?.forEach { symbolDecl ->
                            if (symbolDecl.text == name) {
                                // Create navigation item for the global declaration
                                if (symbolDecl is NavigationItem) {
                                    processor.process(symbolDecl)
                                }
                            }
                        }
                    }
                    is NasmExternDir -> {
                        // Check if this extern directive declares the symbol
                        element.symbolList?.symbolDeclList?.forEach { symbolDecl ->
                            if (symbolDecl.text == name) {
                                // Create navigation item for the extern declaration
                                if (symbolDecl is NavigationItem) {
                                    processor.process(symbolDecl)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
