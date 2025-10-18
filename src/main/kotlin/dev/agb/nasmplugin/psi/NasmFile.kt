package dev.agb.nasmplugin.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.NasmLanguage

/**
 * PSI File representation for NASM assembly files
 */
class NasmFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, NasmLanguage) {

    override fun getFileType(): FileType = NasmFileType

    override fun toString(): String = "NASM File"
}
