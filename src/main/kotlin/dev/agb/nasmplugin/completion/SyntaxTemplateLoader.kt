package dev.agb.nasmplugin.completion

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Data class representing a template segment
 */
sealed class TemplateSegment {
    data class Text(val text: String) : TemplateSegment()
    data class Variable(val name: String, val default: String) : TemplateSegment()
}

/**
 * Categories of syntax templates
 */
enum class TemplateCategory {
    DIRECTIVE,
    INSTRUCTION,
    DATA,
    MACRO,
    OTHER
}

fun String.toTemplateCategory(): TemplateCategory {
    return when (this.lowercase()) {
        "directive" -> TemplateCategory.DIRECTIVE
        "instruction" -> TemplateCategory.INSTRUCTION
        "data" -> TemplateCategory.DATA
        "macro" -> TemplateCategory.MACRO
        else -> TemplateCategory.OTHER
    }
}

/**
 * Data class representing a syntax template
 */
data class SyntaxTemplate(
    val category: TemplateCategory,
    val name: String,
    val segments: List<TemplateSegment>
)

/**
 * Loads syntax templates from XML resource file
 */
class SyntaxTemplateLoader private constructor() {
    // Map of category -> (name -> template)
    private val templates: Map<TemplateCategory, Map<String, SyntaxTemplate>> by lazy {
        loadTemplates()
    }

    companion object {
        @JvmField
        val instance: SyntaxTemplateLoader = SyntaxTemplateLoader()
    }

    /**
     * Get template for a specific category and name
     */
    fun getTemplate(category: TemplateCategory, name: String): SyntaxTemplate? {
        return templates[category]?.get(name.lowercase())
    }

    /**
     * Check if a template exists
     */
    fun hasTemplate(category: TemplateCategory, name: String): Boolean {
        return templates[category]?.containsKey(name.lowercase()) == true
    }

    /**
     * Apply a template in the editor
     */
    fun applyTemplate(
        template: SyntaxTemplate,
        editor: Editor,
        project: Project,
        tailOffset: Int
    ) {
        // If no segments, just move cursor
        if (template.segments.isEmpty()) {
            editor.caretModel.moveToOffset(tailOffset)
            return
        }

        editor.caretModel.moveToOffset(tailOffset)
        val templateManager = TemplateManager.getInstance(project)
        val liveTemplate = templateManager.createTemplate("", "")
        liveTemplate.isToReformat = false

        // Build the template from segments
        for (segment in template.segments) {
            when (segment) {
                is TemplateSegment.Text -> {
                    liveTemplate.addTextSegment(segment.text)
                }
                is TemplateSegment.Variable -> {
                    liveTemplate.addVariable(
                        segment.name,
                        ConstantNode(segment.default),
                        ConstantNode(segment.default),
                        true
                    )
                }
            }
        }

        liveTemplate.addEndVariable()
        templateManager.startTemplate(editor, liveTemplate)
    }

    /**
     * Load templates from XML resource
     */
    private fun loadTemplates(): Map<TemplateCategory, Map<String, SyntaxTemplate>> {
        val result = mutableMapOf<TemplateCategory, MutableMap<String, SyntaxTemplate>>()

        try {
            val inputStream = SyntaxTemplateLoader::class.java
                .getResourceAsStream("/nasm/syntax-templates.xml")
                ?: throw RuntimeException("Could not find syntax-templates.xml resource")

            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.parse(inputStream)
            doc.documentElement.normalize()

            val templateNodes = doc.getElementsByTagName("template")
            for (i in 0 until templateNodes.length) {
                val templateNode = templateNodes.item(i) as Element
                val categoryStr = templateNode.getAttribute("category")
                val name = templateNode.getAttribute("name")

                val category = categoryStr.toTemplateCategory()

                val segments = mutableListOf<TemplateSegment>()
                val segmentNodes = templateNode.getElementsByTagName("segment")

                for (j in 0 until segmentNodes.length) {
                    val segmentNode = segmentNodes.item(j) as Element
                    val type = segmentNode.getAttribute("type")

                    when (type) {
                        "text" -> {
                            val text = segmentNode.textContent
                            segments.add(TemplateSegment.Text(text))
                        }
                        "variable" -> {
                            val varName = segmentNode.getAttribute("name")
                            val default = segmentNode.getAttribute("default")
                            segments.add(TemplateSegment.Variable(varName, default))
                        }
                    }
                }

                val template = SyntaxTemplate(category, name, segments)
                result.getOrPut(category) { mutableMapOf() }[name.lowercase()] = template
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty map on error rather than crashing
        }

        return result
    }

    /**
     * Get all template names for a category
     */
    fun getAllTemplateNames(category: TemplateCategory): Set<String> {
        return templates[category]?.keys ?: emptySet()
    }

    /**
     * Get all directive template names (convenience method)
     */
    fun getAllDirectiveTemplateNames(): Set<String> {
        return getAllTemplateNames(TemplateCategory.DIRECTIVE)
    }
}
