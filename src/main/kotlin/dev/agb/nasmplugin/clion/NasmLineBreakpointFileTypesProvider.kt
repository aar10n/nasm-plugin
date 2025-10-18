package dev.agb.nasmplugin.clion

import com.intellij.openapi.fileTypes.FileType
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrLineBreakpointFileTypesProvider
import dev.agb.nasmplugin.NasmFileType

/**
 * Tells CLion's debugger that NASM files can have line breakpoints.
 * This is required for the CIDR debugging framework to recognize NASM files
 * as valid sources for setting breakpoints during native debugging sessions.
 */
class NasmLineBreakpointFileTypesProvider : CidrLineBreakpointFileTypesProvider {

    override fun getFileTypes(): Set<FileType> {
        return setOf(NasmFileType)
    }
}
