package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.agb.nasmplugin.psi.NasmSectionAttrName
import dev.agb.nasmplugin.psi.NasmSectionDir

// Valid section attributes (without parameters)
private val VALID_ATTRIBUTES = setOf(
    "exec", "noexec",
    "write", "nowrite",
    "alloc", "noalloc",
    "progbits", "nobits",
    "tls"
)

// Attributes that take parameters
private val PARAM_ATTRIBUTES = setOf(
    "align"
)

// Conflicting attribute pairs
private val CONFLICTING_PAIRS = listOf(
    "exec" to "noexec",
    "write" to "nowrite",
    "alloc" to "noalloc",
    "progbits" to "nobits"
)

/**
 * Validates and highlights SECTION directive attributes.
 *
 * Valid section attributes include:
 * - align=<number> - Section alignment
 * - exec/noexec - Executable flag
 * - alloc/noalloc - Allocatable flag
 * - write/nowrite - Writable flag
 * - progbits/nobits - Section type
 *
 * This annotator:
 * - Validates attribute names
 * - Checks for conflicting attributes (e.g., exec and noexec)
 * - Provides semantic highlighting for attributes
 */
class NasmSectionAttributeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is NasmSectionDir -> validateSectionAttributes(element, holder)
            is NasmSectionAttrName -> highlightAttributeName(element, holder)
        }
    }

    private fun validateSectionAttributes(sectionDir: NasmSectionDir, holder: AnnotationHolder) {
        val attrs = sectionDir.sectionAttrs ?: return
        val attrList = attrs.sectionAttrList

        // Check for conflicts
        val usedAttributes = mutableSetOf<String>()

        for (attr in attrList) {
            val attrName = attr.sectionAttrName.text.lowercase()

            // Check for duplicate attributes
            if (!usedAttributes.add(attrName)) {
                holder.newAnnotation(HighlightSeverity.WARNING, "Duplicate section attribute: '$attrName'")
                    .range(attr.sectionAttrName)
                    .create()
            }

            // Check for conflicting attributes
            for ((first, second) in CONFLICTING_PAIRS) {
                if (attrName == first && usedAttributes.contains(second)) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Conflicting attributes: '$first' and '$second'")
                        .range(attr.sectionAttrName)
                        .create()
                }
                if (attrName == second && usedAttributes.contains(first)) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Conflicting attributes: '$first' and '$second'")
                        .range(attr.sectionAttrName)
                        .create()
                }
            }

            // Validate attribute name
            if (!VALID_ATTRIBUTES.contains(attrName) && !PARAM_ATTRIBUTES.contains(attrName)) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unknown section attribute: '$attrName'")
                    .range(attr.sectionAttrName)
                    .create()
            }

            // Validate attributes with parameters
            if (PARAM_ATTRIBUTES.contains(attrName)) {
                if (attr.expression == null) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Attribute '$attrName' requires a value")
                        .range(attr.sectionAttrName)
                        .create()
                }
            }
        }
    }

    private fun highlightAttributeName(attrName: NasmSectionAttrName, holder: AnnotationHolder) {
        val name = attrName.text.lowercase()

        // Apply semantic highlighting for valid attributes
        if (VALID_ATTRIBUTES.contains(name) || PARAM_ATTRIBUTES.contains(name)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(attrName)
                .textAttributes(NasmSyntaxHighlighter.DIRECTIVE)
                .create()
        }
    }
}
