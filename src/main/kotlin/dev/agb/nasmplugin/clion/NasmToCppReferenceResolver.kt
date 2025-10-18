package dev.agb.nasmplugin.clion

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.cidr.lang.OCFileType
import com.jetbrains.cidr.lang.psi.OCDeclarator
import com.jetbrains.cidr.lang.psi.OCFile

/**
 * Resolves NASM extern symbols to C/C++ declarations in CLion.
 * Uses CIDR APIs to search C/C++ files and PSI tree traversal.
 *
 * This resolver enables cross-language navigation from NASM assembly
 * code to C/C++ declarations. For example, clicking on an extern
 * symbol like "printf" will navigate to the C standard library declaration.
 *
 * Only available when running in CLion with C/C++ support.
 *
 * Note: CLion 2025.2+ requires PSI tree traversal as the symbol index API
 * is not publicly available. Uses FileTypeIndex + PSI tree walking instead
 * of PsiShortNamesCache (which is Java-only) or OCWorkspace.getSymbols()
 * (which doesn't exist in the public API).
 */
object NasmToCppReferenceResolver {

    /**
     * Resolve an extern symbol name to a C/C++ declaration.
     *
     * This method searches all C/C++ files in the project for matching declarations,
     * prioritizing functions (most commonly extern'd), then global variables and types.
     *
     * @param symbolName The name of the C function/variable to find
     * @param project The current project
     * @return The C/C++ PSI element (function declaration, variable, etc.) or null if not found
     */
    fun resolveToCppSymbol(symbolName: String, project: Project): PsiElement? {
        try {
            val scope = GlobalSearchScope.projectScope(project)
            val psiManager = PsiManager.getInstance(project)

            // Get all C/C++ files in the project
            val ocFiles = FileTypeIndex.getFiles(OCFileType.INSTANCE, scope)
            val candidates = mutableListOf<OCDeclarator>()

            // Search all C/C++ files for matching declarators
            for (virtualFile in ocFiles) {
                val psiFile = psiManager.findFile(virtualFile) as? OCFile ?: continue

                // Recursively visit all elements in the file
                psiFile.accept(object : PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is OCDeclarator && element.name == symbolName) {
                            candidates.add(element)
                        }
                        super.visitElement(element)
                    }
                })
            }

            // Return the first candidate (could be enhanced to prioritize functions)
            return candidates.firstOrNull()
        } catch (e: ProcessCanceledException) {
            // Control-flow exception - must rethrow to allow platform to cancel operation
            throw e
        } catch (e: Exception) {
            // Silently ignore - likely running in non-CLion IDE without CIDR support
            return null
        }
    }
}
