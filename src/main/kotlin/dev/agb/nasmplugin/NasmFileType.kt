package dev.agb.nasmplugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type for NASM assembly files (.asm, .nasm, .inc, .mac).
 */
object NasmFileType : LanguageFileType(NasmLanguage) {

    override fun getName(): String = "NASM Assembly"

    override fun getDescription(): String = "NASM assembly file"

    override fun getDefaultExtension(): String = "asm"

    override fun getIcon(): Icon = NasmIcons.FILE
}
