package dev.agb.nasmplugin

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import dev.agb.nasmplugin.lexer.NasmLexerAdapter
import dev.agb.nasmplugin.psi.*

/**
 * Provides Find Usages functionality for NASM elements
 */
class NasmFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            NasmLexerAdapter(),
            TokenSet.create(NasmTypes.IDENTIFIER),
            TokenSet.create(NasmTypes.COMMENT),
            TokenSet.create(NasmTypes.STRING)
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is NasmNamedElement

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return when (element) {
            is NasmLabelDef -> "label"
            is NasmEquDefinition -> "equ constant"
            is NasmMultiLineMacro -> "macro"
            is NasmPpDefineStmt -> "preprocessor define"
            is NasmPpAssignStmt -> "preprocessor assignment"
            else -> ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? NasmNamedElement)?.name ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        getDescriptiveName(element)
}
