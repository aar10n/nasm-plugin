package dev.agb.nasmplugin.editor

import com.intellij.lang.Commenter

/**
 * Provides comment/uncomment functionality for NASM assembly files.
 * Supports line comments starting with semicolon.
 */
class NasmCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "; "

    override fun getBlockCommentPrefix(): String? = null

    override fun getBlockCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
