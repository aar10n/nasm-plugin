package dev.agb.nasmplugin.completion

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/**
 * Provides templates for NASM syntax elements (directives, instructions, data declarations, etc.).
 * Templates are loaded from syntax-templates.xml resource file.
 */
object SyntaxTemplates {

    /**
     * Functional interface for template creation
     */
    fun interface TemplateCreator {
        fun createTemplate(editor: Editor, project: Project, tailOffset: Int)
    }

    /**
     * Get template creator for a specific category and name
     */
    @JvmStatic
    fun getTemplate(category: TemplateCategory, name: String): TemplateCreator? {
        val template = SyntaxTemplateLoader.instance.getTemplate(category, name) ?: return null

        return TemplateCreator { editor, project, tailOffset ->
            SyntaxTemplateLoader.instance.applyTemplate(template, editor, project, tailOffset)
        }
    }

    /**
     * Get template creator for a directive (convenience method)
     */
    @JvmStatic
    fun getDirectiveTemplate(directiveName: String): TemplateCreator? {
        return getTemplate(TemplateCategory.DIRECTIVE, directiveName)
    }

    /**
     * Get template creator for an instruction (convenience method)
     */
    @JvmStatic
    fun getInstructionTemplate(instructionName: String): TemplateCreator? {
        return getTemplate(TemplateCategory.INSTRUCTION, instructionName)
    }

    /**
     * Get template creator for a data declaration (convenience method)
     */
    @JvmStatic
    fun getDataTemplate(dataName: String): TemplateCreator? {
        return getTemplate(TemplateCategory.DATA, dataName)
    }

    /**
     * Check if a template exists
     */
    @JvmStatic
    fun hasTemplate(category: TemplateCategory, name: String): Boolean {
        return SyntaxTemplateLoader.instance.hasTemplate(category, name)
    }

    /**
     * Check if a directive template exists (convenience method)
     */
    @JvmStatic
    fun hasDirectiveTemplate(directiveName: String): Boolean {
        return hasTemplate(TemplateCategory.DIRECTIVE, directiveName)
    }

    /**
     * Check if an instruction template exists (convenience method)
     */
    @JvmStatic
    fun hasInstructionTemplate(instructionName: String): Boolean {
        return hasTemplate(TemplateCategory.INSTRUCTION, instructionName)
    }

    /**
     * Check if a data template exists (convenience method)
     */
    @JvmStatic
    fun hasDataTemplate(dataName: String): Boolean {
        return hasTemplate(TemplateCategory.DATA, dataName)
    }
}
