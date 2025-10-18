package dev.agb.nasmplugin.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.*

/**
 * Provides code folding for NASM assembly files.
 *
 * Supports folding for:
 * - Multi-line macro definitions (%macro...%endmacro)
 * - Conditional blocks (%if...%endif)
 * - Section directives (section .text...next section)
 * - Repetition blocks (%rep...%endrep) - handled via RepBlock
 * - Data blocks (multiple consecutive data definitions)
 */
class NasmFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        PsiTreeUtil.findChildrenOfAnyType(
            root,
            NasmMultiLineMacro::class.java,
            NasmIfDir::class.java,
            NasmSectionDir::class.java,
            NasmRepBlock::class.java,
            NasmDataDef::class.java
        ).forEach { element ->
            when (element) {
                is NasmMultiLineMacro -> addMacroFolding(element, descriptors, document)
                is NasmIfDir -> addConditionalFolding(element, descriptors, document)
                is NasmSectionDir -> addSectionFolding(element, descriptors, document)
                is NasmRepBlock -> addRepBlockFolding(element, descriptors, document)
                is NasmDataDef -> {} // Handle data blocks separately
            }
        }

        // Add data block folding (consecutive data definitions)
        addDataBlockFolding(root, descriptors, document)

        return descriptors.toTypedArray()
    }

    /**
     * Add folding for multi-line macro definitions
     */
    private fun addMacroFolding(macro: NasmMultiLineMacro, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        val startOffset = macro.textRange.startOffset
        val endOffset = macro.textRange.endOffset

        // Only fold if macro spans multiple lines
        if (document.getLineNumber(startOffset) < document.getLineNumber(endOffset)) {
            val macroName = macro.name ?: "macro"
            descriptors.add(
                FoldingDescriptor(
                    macro.node,
                    TextRange(startOffset, endOffset),
                    null,
                    "%macro $macroName..."
                )
            )
        }
    }

    /**
     * Add folding for conditional blocks (%if/%ifdef/%ifndef...%endif)
     */
    private fun addConditionalFolding(ifDir: NasmIfDir, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        // Find the corresponding %endif
        val endifElement = findEndif(ifDir)
        if (endifElement != null) {
            val startOffset = ifDir.textRange.startOffset
            val endOffset = endifElement.textRange.endOffset

            // Only fold if spans multiple lines
            if (document.getLineNumber(startOffset) < document.getLineNumber(endOffset)) {
                val conditionText = ifDir.text.take(20).replace("\n", " ")
                descriptors.add(
                    FoldingDescriptor(
                        ifDir.node,
                        TextRange(startOffset, endOffset),
                        null,
                        "$conditionText..."
                    )
                )
            }
        }
    }

    /**
     * Add folding for sections
     */
    private fun addSectionFolding(section: NasmSectionDir, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        // Find the next section or end of file
        val nextSection = findNextSection(section)
        val startOffset = section.textRange.endOffset
        val endOffset = nextSection?.textRange?.startOffset ?: section.containingFile.textRange.endOffset

        // Only fold if there's meaningful content and spans multiple lines
        if (endOffset > startOffset && document.getLineNumber(startOffset) < document.getLineNumber(endOffset - 1)) {
            val sectionName = section.sectionName?.text ?: ".section"
            descriptors.add(
                FoldingDescriptor(
                    section.node,
                    TextRange(startOffset, endOffset),
                    null,
                    "section $sectionName..."
                )
            )
        }
    }

    /**
     * Add folding for repetition blocks (%rep...%endrep)
     */
    private fun addRepBlockFolding(repBlock: NasmRepBlock, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        val startOffset = repBlock.textRange.startOffset
        val endOffset = repBlock.textRange.endOffset

        // Only fold if spans multiple lines
        if (document.getLineNumber(startOffset) < document.getLineNumber(endOffset)) {
            descriptors.add(
                FoldingDescriptor(
                    repBlock.node,
                    TextRange(startOffset, endOffset),
                    null,
                    "%rep..."
                )
            )
        }
    }

    /**
     * Add folding for consecutive data definition blocks
     */
    private fun addDataBlockFolding(root: PsiElement, descriptors: MutableList<FoldingDescriptor>, document: Document) {
        val dataDefinitions = PsiTreeUtil.findChildrenOfType(root, NasmDataDef::class.java)

        var blockStart: NasmDataDef? = null
        var blockEnd: NasmDataDef? = null
        var consecutiveCount = 0

        dataDefinitions.sortedBy { it.textRange.startOffset }.forEach { data ->
            if (blockStart == null) {
                blockStart = data
                blockEnd = data
                consecutiveCount = 1
            } else {
                val prevLine = document.getLineNumber(blockEnd!!.textRange.endOffset)
                val currentLine = document.getLineNumber(data.textRange.startOffset)

                // Check if consecutive (within 1 line)
                if (currentLine - prevLine <= 1) {
                    blockEnd = data
                    consecutiveCount++
                } else {
                    // End of block, add folding if 3+ consecutive data definitions
                    if (consecutiveCount >= 3) {
                        addDataBlockDescriptor(blockStart, blockEnd, descriptors)
                    }
                    blockStart = data
                    blockEnd = data
                    consecutiveCount = 1
                }
            }
        }

        // Handle last block
        if (consecutiveCount >= 3 && blockStart != null && blockEnd != null) {
            addDataBlockDescriptor(blockStart, blockEnd, descriptors)
        }
    }

    private fun addDataBlockDescriptor(start: NasmDataDef, end: NasmDataDef, descriptors: MutableList<FoldingDescriptor>) {
        val range = TextRange(start.textRange.startOffset, end.textRange.endOffset)
        descriptors.add(
            FoldingDescriptor(
                start.node,
                range,
                null,
                "data block..."
            )
        )
    }

    /**
     * Find the corresponding %endif for an %if directive
     */
    private fun findEndif(ifDir: NasmIfDir): NasmEndifDir? {
        var current: PsiElement? = ifDir.nextSibling
        var depth = 1

        while (current != null && depth > 0) {
            when (current) {
                is NasmIfDir -> depth++
                is NasmEndifDir -> {
                    depth--
                    if (depth == 0) return current
                }
            }
            current = current.nextSibling
        }

        return null
    }

    /**
     * Find the next section directive
     */
    private fun findNextSection(section: NasmSectionDir): NasmSectionDir? {
        var current: PsiElement? = section.nextSibling

        while (current != null) {
            if (current is NasmSectionDir) {
                return current
            }
            current = current.nextSibling
        }

        return null
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Don't collapse by default - let users decide
        return false
    }
}
