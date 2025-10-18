package dev.agb.nasmplugin.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.agb.nasmplugin.psi.*

/**
 * Provides semantic highlighting for keywords used as symbol names.
 *
 * This annotator ensures that keywords like "write" (SECTION_ATTR_KW) are highlighted
 * as identifiers when used in symbol name contexts (e.g., "extern write").
 */
class NasmSymbolNameAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Check if this is a SYMBOL_NAME element containing a keyword token
        if (element is NasmSymbolName) {
            highlightKeywordAsIdentifier(element, holder)
        }
    }

    private fun highlightKeywordAsIdentifier(symbolName: NasmSymbolName, holder: AnnotationHolder) {
        // Find any keyword tokens that should be highlighted as identifiers
        val node = symbolName.node

        // Check for SECTION_ATTR_KW tokens
        val sectionAttrNode = node.findChildByType(NasmTypes.SECTION_ATTR_KW)
        if (sectionAttrNode != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(sectionAttrNode.textRange)
                .textAttributes(NasmSyntaxHighlighter.IDENTIFIER)
                .create()
        }

        // Check for SIZE_SPEC tokens
        val sizeSpecNode = node.findChildByType(NasmTypes.SIZE_SPEC)
        if (sizeSpecNode != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(sizeSpecNode.textRange)
                .textAttributes(NasmSyntaxHighlighter.IDENTIFIER)
                .create()
        }
    }
}
