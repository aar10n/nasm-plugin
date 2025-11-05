package dev.agb.nasmplugin.settings

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.psi.PsiManager
import com.intellij.ui.dsl.builder.*
import com.intellij.util.FileContentUtil
import dev.agb.nasmplugin.NasmFileType

/**
 * Settings page for NASM plugin configuration.
 * Appears under Settings > Languages & Frameworks > NASM Assembly.
 */
class NasmSettingsConfigurable(private val project: Project) : BoundConfigurable("NASM Assembly") {

    private val settings = NasmProjectSettings.getInstance(project)

    override fun createPanel(): DialogPanel {
        return panel {
            group("Include Paths") {
                row {
                    label("Include search paths for %include directives:")
                        .applyToComponent {
                            toolTipText = "Paths where NASM will search for included files. " +
                                        "One path per line. Can be relative to project root or absolute."
                        }
                }
                row {
                    textArea()
                        .bindText(
                            getter = { settings.includePaths.joinToString("\n") },
                            setter = { value ->
                                val newPaths = value
                                    .split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                settings.includePaths.clear()
                                settings.includePaths.addAll(newPaths)
                                settings.notifyChanged()
                            }
                        )
                        .rows(4)
                        .columns(60)
                        .comment("Enter one path per line.")
                }
            }

            group("Preprocessor Macros") {
                row {
                    label("Command-line macro definitions (equivalent to -D flag):")
                        .applyToComponent {
                            toolTipText = "Macros that are defined as if passed via -D on the command line"
                        }
                }
                row {
                    textField()
                        .bindText(
                            getter = { settings.commandLineMacros },
                            setter = { value -> settings.commandLineMacros = value.trim() }
                        )
                        .columns(60)
                        .comment(
                            "<html>Enter comma-separated macro definitions. Examples: DEBUG DEBUG,VERSION=2 " +
                            "OS=LINUX,ARCH=x64,DEBUG Format: MACRO[=value][,MACRO[=value],...]<br>" +
                            "These are treated as if defined with %define at the start of each file.</html>"
                        )
                }
            }
        }
    }

    override fun apply() {
        super.apply()
        // Trigger re-analysis of open NASM files to reflect the settings changes
        // This ensures that changes to command-line macros and include paths
        // are immediately reflected in the editor (e.g., conditional branch highlighting)
        val nasmFiles = FileEditorManager.getInstance(project).openFiles.filter { virtualFile ->
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            psiFile?.fileType == NasmFileType
        }

        if (nasmFiles.isNotEmpty()) {
            FileContentUtil.reparseFiles(project, nasmFiles, true)
        }
    }

    override fun reset() {
        super.reset()
    }

    override fun isModified(): Boolean {
        return super.isModified()
    }
}
