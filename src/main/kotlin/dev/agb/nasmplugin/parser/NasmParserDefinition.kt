package dev.agb.nasmplugin.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.agb.nasmplugin.NasmLanguage
import dev.agb.nasmplugin.lexer.NasmLexerAdapter
import dev.agb.nasmplugin.psi.NasmFile
import dev.agb.nasmplugin.psi.NasmTypes
import dev.agb.nasmplugin.psi.NasmTokenType

/**
 * Parser definition for NASM language
 */
class NasmParserDefinition : ParserDefinition {

    private val fileNodeType = IFileElementType(NasmLanguage)
    private val comments = TokenSet.create(NasmTypes.COMMENT)
    private val strings = TokenSet.create(NasmTypes.STRING)

    override fun createLexer(project: Project): Lexer = NasmLexerAdapter()

    override fun createParser(project: Project): PsiParser = NasmParser()

    override fun getFileNodeType(): IFileElementType = fileNodeType

    override fun getCommentTokens(): TokenSet = comments

    override fun getStringLiteralElements(): TokenSet = strings

    override fun createElement(node: ASTNode): PsiElement = NasmTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = NasmFile(viewProvider)
}
