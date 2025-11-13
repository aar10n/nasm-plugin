package dev.agb.nasmplugin.clion

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import dev.agb.nasmplugin.NasmFileType
import dev.agb.nasmplugin.clion.projectmodel.NasmCompilationInfo
import dev.agb.nasmplugin.clion.projectmodel.NasmProjectModelService

/**
 * Action to show compiler information for a NASM file.
 * Creates a virtual text file displaying compilation flags, include paths, and macro definitions
 * in a format similar to CLion's native "Show Compiler Info" action.
 *
 * Text, description, and icon are defined in plugin-clion.xml.
 */
class ShowNasmCompilerInfoAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.project

        // Enable only for NASM files that are actually compiled
        val isAvailable = if (project != null && file != null && file.fileType == NasmFileType) {
            // Check if this file has compilation information
            val service = NasmProjectModelService.getInstance(project)
            service.getCompilationInfo(file) != null
        } else {
            false
        }

        e.presentation.isEnabledAndVisible = isAvailable
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val service = NasmProjectModelService.getInstance(project)
        val compilationInfo = service.getCompilationInfo(file) ?: return // Should always exist due to update() check

        val content = buildCompilerInfoContent(file, compilationInfo)

        // Create a virtual file to display the info
        val virtualFile = LightVirtualFile(
            "Compiler Info for '${file.name}' in '${file.parent?.name}'",
            com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE,
            content
        ).apply {
            isWritable = true  // Make it writable like CLion's version
        }

        // Open the virtual file in the editor
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }

    private fun buildCompilerInfoContent(file: VirtualFile, info: NasmCompilationInfo): String {
        return buildString {
            appendLine("Compiler info for '${file.name}' in configuration '${file.parent?.name}'")
            appendLine()

            // Compiler kind
            appendLine("Compiler kind: NASM")
            appendLine()

            // Working directory
            appendLine("Working directory:")
            appendLine("  ${info.workingDirectory}")
            appendLine()

            // Output file
            if (info.outputFile != null) {
                appendLine("Output file:")
                appendLine("  ${info.outputFile}")
                appendLine()
            }

            // Compiler switches
            appendLine("Compiler switches:")
            if (info.compilerArguments.isEmpty()) {
                appendLine("  (none)")
            } else {
                for (arg in info.compilerArguments) {
                    appendLine("  $arg")
                }
            }
            appendLine()

            // Include paths
            if (info.includePaths.isNotEmpty()) {
                appendLine("Include paths:")
                for (includePath in info.includePaths) {
                    appendLine("  $includePath")
                }
                appendLine()
            }

            // Compiler info section
            appendLine("Compiler info:")
            appendLine()

            // Defines
            if (info.macroDefinitions.isNotEmpty()) {
                appendLine("Defines:")
                for ((name, value) in info.macroDefinitions) {
                    if (value != null) {
                        appendLine("#define $name $value")
                    } else {
                        appendLine("#define $name")
                    }
                }
            } else {
                appendLine("Defines:")
                appendLine("  (none)")
            }
        }
    }
}
