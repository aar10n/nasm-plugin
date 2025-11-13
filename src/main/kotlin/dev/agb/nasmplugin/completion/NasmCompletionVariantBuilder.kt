package dev.agb.nasmplugin.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import dev.agb.nasmplugin.psi.*
import dev.agb.nasmplugin.psi.impl.NasmIconProvider
import dev.agb.nasmplugin.psi.impl.ElementType
import dev.agb.nasmplugin.settings.CommandLineMacroProvider

/**
 * Utility class for building completion variants from NASM elements.
 * Provides a centralized way to extract completion items from NASM files
 * for both code completion and reference resolution.
 */
object NasmCompletionVariantBuilder {

    enum class VariantType {
        LABELS,
        EQU_CONSTANTS,
        MULTI_LINE_MACROS,
        SINGLE_LINE_MACROS,
        ASSIGNS,
        COMMAND_LINE_MACROS,
        EXTERNS
    }

    /**
     * Adds completion variants from a file to the provided list.
     * @param list The list to add variants to
     * @param file The file to extract variants from
     * @param fileSuffix Optional suffix to indicate the source file (e.g., " [included.asm]")
     * @param types The types of variants to include (defaults to all)
     * @param excludeLocalLabels If true, local labels (starting with .) are excluded
     */
    fun addVariantsFromFile(
        list: MutableList<LookupElementBuilder>,
        file: PsiFile,
        fileSuffix: String = "",
        types: Set<VariantType> = VariantType.entries.toSet(),
        excludeLocalLabels: Boolean = false
    ) {
        // Define variant configurations
        data class VariantConfig(
            val type: VariantType,
            val elementType: ElementType,
            val typeText: String,
            val caseSensitive: Boolean = true,
            val finder: (PsiFile) -> List<*>
        )

        val variantConfigs = listOf(
            VariantConfig(
                type = VariantType.LABELS,
                elementType = ElementType.LABEL,
                typeText = "label",
                finder = PsiFile::findLabels
            ),
            VariantConfig(
                type = VariantType.EQU_CONSTANTS,
                elementType = ElementType.CONSTANT,
                typeText = "equ",
                finder = PsiFile::findEquDefinitions
            ),
            VariantConfig(
                type = VariantType.MULTI_LINE_MACROS,
                elementType = ElementType.MACRO,
                typeText = "%macro",
                caseSensitive = false,
                finder = PsiFile::findMultiLineMacros
            ),
            VariantConfig(
                type = VariantType.SINGLE_LINE_MACROS,
                elementType = ElementType.DEFINE,
                typeText = "%define",
                caseSensitive = false,
                finder = PsiFile::findPpDefineStmts
            ),
            VariantConfig(
                type = VariantType.ASSIGNS,
                elementType = ElementType.ASSIGN,
                typeText = "assign",
                caseSensitive = false,
                finder = PsiFile::findPpAssignStmts
            ),
            VariantConfig(
                type = VariantType.EXTERNS,
                elementType = ElementType.EXTERN,
                typeText = "extern",
                finder = PsiFile::findExternSymbols
            )
        )

        // Track added names to avoid duplicates
        val addedNames = mutableSetOf<String>()

        // Process each variant type
        variantConfigs.forEach { config ->
            if (config.type in types) {
                config.finder(file).forEach { element ->
                    (element as? NasmNamedElement)?.name?.takeIf { it.isNotEmpty() }?.let { name ->
                        // Skip local labels if requested (for global directive context)
                        if (excludeLocalLabels && config.type == VariantType.LABELS && name.startsWith(".")) {
                            return@forEach
                        }

                        // Skip duplicates
                        if (!addedNames.add(name)) {
                            return@forEach
                        }

                        val builder = LookupElementBuilder.create(element, name)
                            .withIcon(NasmIconProvider.getIconFor(config.elementType))
                            .withTypeText("${config.typeText}$fileSuffix")

                        val finalBuilder = if (!config.caseSensitive) {
                            builder.withCaseSensitivity(false)
                        } else {
                            builder
                        }

                        list.add(finalBuilder)
                    }
                }
            }
        }
    }

    /**
     * Builds a list of completion variants from a file and its includes.
     */
    fun buildVariants(
        containingFile: PsiFile,
        types: Set<VariantType> = VariantType.entries.toSet(),
        excludeLocalLabels: Boolean = false
    ): Array<Any> {
        val variants = buildList {
            // Add command-line macros first (they have lower precedence than source definitions)
            if (VariantType.COMMAND_LINE_MACROS in types) {
                addCommandLineMacroVariants(this, containingFile)
            }

            // Add variants from current file
            addVariantsFromFile(this, containingFile, "", types, excludeLocalLabels)

            // Add elements from included files
            containingFile.getIncludedFiles().forEach { includedFile ->
                val fileIndicator = " [${includedFile.name}]"
                addVariantsFromFile(this, includedFile, fileIndicator, types, excludeLocalLabels)
            }
        }

        return variants.toTypedArray()
    }

    /**
     * Adds command-line macro variants from project settings and compilation database.
     * This includes both global macros and per-file macros from the project model.
     */
    private fun addCommandLineMacroVariants(
        list: MutableList<LookupElementBuilder>,
        file: PsiFile
    ) {
        val project = file.project
        val provider = CommandLineMacroProvider.getInstance(project)

        // Pass the virtual file to get both global and per-file macros
        val virtualFile = file.virtualFile ?: file.originalFile?.virtualFile
        val macros = provider.getCommandLineMacros(virtualFile)

        macros.forEach { macro ->
            val builder = LookupElementBuilder.create(macro.name)
                .withIcon(NasmIconProvider.getIconFor(ElementType.DEFINE))
                .withTypeText("-D (command-line)")
                .withTailText(macro.value?.let { " = $it" } ?: "", true)
                .withCaseSensitivity(false)

            list.add(builder)
        }
    }
}