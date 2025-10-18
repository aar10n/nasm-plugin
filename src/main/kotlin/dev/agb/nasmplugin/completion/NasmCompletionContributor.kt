package dev.agb.nasmplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import dev.agb.nasmplugin.NasmLanguage
import dev.agb.nasmplugin.database.DirectiveDatabase
import dev.agb.nasmplugin.database.InstructionDatabase
import dev.agb.nasmplugin.database.PreprocessorFunctionDatabase
import dev.agb.nasmplugin.database.RegisterDatabase

/**
 * Provides code completion for NASM assembly language
 */
class NasmCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(NasmLanguage),
            NasmCompletionProvider()
        )
    }

    private class NasmCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Create a deduplicated result set for this completion session
            val deduplicatedResult = result.withPrefixMatcher(result.prefixMatcher)

            // Track added items locally for this completion session
            val addedItems = mutableSetOf<String>()

            doAddCompletions(parameters, context, deduplicatedResult, addedItems)
        }

        private fun doAddCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
            addedItems: MutableSet<String>
        ) {
            val position = parameters.position
            val prefix = result.prefixMatcher.prefix.lowercase()

            // Check if we're inside an %include directive string
            if (isInIncludeDirective(parameters)) {
                addIncludeFileCompletions(parameters, result)
                result.stopHere()
                return
            }

            // Special check for after % - IntelliJ inserts a dummy identifier, so we need to check the original position
            val isAfterPercent = parameters.originalPosition?.let { origPos ->
                // Check if the original position itself is a % character
                origPos.text == "%"
            } ?: false

            // Detect completion context using rule-based system
            val completionContext = if (isAfterPercent) {
                CompletionContext.AfterPercent
            } else {
                CompletionContextDetector.detectContext(position)
            }

            // Add completions based on detected context
            addCompletionsForContext(position, prefix, result, completionContext, addedItems)
        }

        /**
         * Add completions for a specific context.
         * This is the central dispatch method that uses context properties to determine what to show.
         */
        private fun addCompletionsForContext(
            position: PsiElement,
            prefix: String,
            result: CompletionResultSet,
            ctx: CompletionContext,
            addedItems: MutableSet<String>
        ) {
            // Handle special case: after percent sign (show without % prefix)
            if (ctx == CompletionContext.AfterPercent) {
                addPreprocessorCompletionsAfterPercent(prefix, result)
                result.stopHere()  // Make sure no other completions are added
                return
            }

            // Add completions based on context flags
            if (ctx.showInstructions) {
                addInstructionCompletions(prefix, result)
            }

            if (ctx.showRegisters) {
                addRegisterCompletions(prefix, result)
            }

            if (ctx.showDirectives) {
                addDirectiveCompletions(prefix, result)
            }

            if (ctx.showPreprocessorFunctions) {
                addPreprocessorFunctionCompletions(prefix, result)
            }

            // Add user-defined symbols based on context-specific allowed types
            if (ctx.allowedSymbolTypes.isNotEmpty()) {
                // For global directive, exclude local labels (starting with .)
                // Also exclude extern symbols (they are external, not defined here)
                val excludeLocalLabels = ctx == CompletionContext.GlobalDirective
                val filteredTypes = if (ctx == CompletionContext.GlobalDirective) {
                    // Don't include EXTERNS in global directive context
                    ctx.allowedSymbolTypes - NasmCompletionVariantBuilder.VariantType.EXTERNS
                } else {
                    ctx.allowedSymbolTypes
                }

                addUserDefinedSymbols(
                    position = position,
                    result = result,
                    types = filteredTypes,
                    priority = ctx.userSymbolPriority,
                    excludeLocalLabels = excludeLocalLabels
                )
            }
        }

        private fun isInIncludeDirective(parameters: CompletionParameters): Boolean {
            val originalPosition = parameters.originalPosition ?: parameters.position
            val originalFile = parameters.originalFile
            val originalOffset = originalPosition.textOffset

            // Get the line containing the cursor
            val lineStartOffset = findLineStart(originalFile.text, originalOffset)
            val lineEndOffset = findLineEnd(originalFile.text, originalOffset)
            val currentLine = originalFile.text.substring(lineStartOffset, lineEndOffset)

            // Check if the line contains an %include directive
            val includePattern = Regex("%include\\s+[\"']?")
            return includePattern.find(currentLine) != null
        }

        private fun findLineStart(text: String, offset: Int): Int {
            var pos = offset.coerceIn(0, text.length)
            while (pos > 0 && text[pos - 1] != '\n') {
                pos--
            }
            return pos
        }

        private fun findLineEnd(text: String, offset: Int): Int {
            var pos = offset.coerceIn(0, text.length)
            while (pos < text.length && text[pos] != '\n') {
                pos++
            }
            return pos
        }

        private fun addIncludeFileCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
            val project = parameters.position.project
            val currentFile = parameters.originalFile.virtualFile ?: return
            val currentDir = currentFile.parent ?: return

            // Get prefix matcher to filter results
            val prefixMatcher = result.prefixMatcher
            val prefix = prefixMatcher.prefix

            // Adjust prefix if it starts with a quote
            val adjustedPrefix = prefix.trimStart('"', '\'', '<')

            val prefixedResult = if (adjustedPrefix != prefix) {
                result.withPrefixMatcher(adjustedPrefix)
            } else {
                result
            }

            // Parse the path to determine if we're navigating into a subdirectory
            val lastSlashIndex = adjustedPrefix.lastIndexOf('/')
            val (pathPrefix, filePrefix) = if (lastSlashIndex >= 0) {
                adjustedPrefix.substring(0, lastSlashIndex + 1) to adjustedPrefix.substring(lastSlashIndex + 1)
            } else {
                "" to adjustedPrefix
            }

            // Update prefix matcher to match only the filename part
            val filePrefixResult = if (filePrefix != adjustedPrefix) {
                result.withPrefixMatcher(filePrefix)
            } else {
                prefixedResult
            }

            // Track items we've already added to avoid duplicates
            val addedItems = mutableSetOf<String>()

            // Get configured include paths from settings
            val includePaths = try {
                project.getService(dev.agb.nasmplugin.navigation.NasmIncludePathResolver::class.java)
                    ?.getNasmIncludePaths(currentFile) ?: emptyList()
            } catch (e: Exception) {
                // Service might not be available
                emptyList()
            }

            // Build list of base directories to search (current dir + include paths)
            val localFileSystem = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
            val baseDirs = mutableListOf<Pair<com.intellij.openapi.vfs.VirtualFile, String>>()

            // Add current directory first (highest priority)
            baseDirs.add(currentDir to "Current directory")

            // Add include paths
            includePaths.forEach { includePath ->
                val includeDir = localFileSystem.findFileByPath(includePath)
                if (includeDir != null) {
                    baseDirs.add(includeDir to includeDir.name)
                }
            }

            // For each base directory, resolve the path prefix and show contents
            baseDirs.forEach { (baseDir, locationName) ->
                val targetDir = if (pathPrefix.isEmpty()) {
                    baseDir
                } else {
                    // Navigate to the subdirectory specified in the path
                    baseDir.findFileByRelativePath(pathPrefix)
                } ?: return@forEach

                // Add directories first
                targetDir.children
                    .filter { it.isDirectory }
                    .filter { filePrefixResult.prefixMatcher.prefixMatches(it.name) }
                    .forEach { dir ->
                        val fullPath = pathPrefix + dir.name + "/"
                        if (fullPath !in addedItems) {
                            val element = LookupElementBuilder.create(fullPath)
                                .withIcon(AllIcons.Nodes.Folder)
                                .withTypeText(locationName)
                                .withInsertHandler { context, _ ->
                                    // Don't close the completion popup after selecting a directory
                                    context.laterRunnable = Runnable {
                                        com.intellij.codeInsight.AutoPopupController.getInstance(context.project)
                                            .scheduleAutoPopup(context.editor)
                                    }
                                }
                            filePrefixResult.addElement(PrioritizedLookupElement.withPriority(element, 95.0))
                            addedItems.add(fullPath)
                        }
                    }

                // Add files, with .inc and .mac ranked highest
                targetDir.children
                    .filter { !it.isDirectory }
                    .filter { it.extension in listOf("inc", "mac", "asm", "s", "nasm") }
                    .filter { it.name != currentFile.name || pathPrefix.isNotEmpty() }
                    .filter { filePrefixResult.prefixMatcher.prefixMatches(it.name) }
                    .forEach { file ->
                        val fullPath = pathPrefix + file.name
                        if (fullPath !in addedItems) {
                            // Determine priority based on extension
                            val priority = when (file.extension) {
                                "inc" -> 100.0
                                "mac" -> 100.0
                                "asm" -> 85.0
                                "nasm" -> 85.0
                                "s" -> 80.0
                                else -> 75.0
                            }

                            val element = LookupElementBuilder.create(fullPath)
                                .withIcon(AllIcons.FileTypes.Text)
                                .withTypeText(locationName)
                            filePrefixResult.addElement(PrioritizedLookupElement.withPriority(element, priority))
                            addedItems.add(fullPath)
                        }
                    }
            }
        }

        private fun createDirectiveInsertHandler(directiveName: String): InsertHandler<LookupElement> {
            return InsertHandler { context, _ ->
                // Get the template for this directive
                val template = SyntaxTemplates.getDirectiveTemplate(directiveName)
                val project = context.editor.project
                if (template != null && project != null) {
                    // Apply the template using the creator
                    template.createTemplate(context.editor, project, context.tailOffset)
                }
            }
        }

        /**
         * Add user-defined symbols (labels, constants, macros, etc.) to completion
         */
        private fun addUserDefinedSymbols(
            position: PsiElement,
            result: CompletionResultSet,
            types: Set<NasmCompletionVariantBuilder.VariantType> = NasmCompletionVariantBuilder.VariantType.entries.toSet(),
            priority: Double = 850.0,
            excludeLocalLabels: Boolean = false
        ) {
            val containingFile = position.containingFile ?: return

            // Build variants once for all user symbols
            val variants = NasmCompletionVariantBuilder.buildVariants(
                containingFile = containingFile,
                types = types,
                excludeLocalLabels = excludeLocalLabels
            )

            // Add to results with specified priority
            variants.filterIsInstance<LookupElementBuilder>()
                .forEach { builder ->
                    result.addElement(PrioritizedLookupElement.withPriority(builder, priority))
                }
        }

        /**
         * Add preprocessor completions after % sign (without the % prefix)
         */
        private fun addPreprocessorCompletionsAfterPercent(prefix: String, result: CompletionResultSet) {
            // Show directives
            DirectiveDatabase.getAllDirectives()
                .filter { it.name.startsWith("%") &&
                         it.name.substring(1).startsWith(prefix, ignoreCase = true) }
                .forEach { directive ->
                    val displayName = directive.name.substring(1)
                    val builder = LookupElementBuilder.create(displayName)
                        .withTailText(" - ${directive.description}", true)
                        .withIcon(AllIcons.Nodes.Artifact)
                        .withCaseSensitivity(false)
                        .bold()

                    val withHandler = if (SyntaxTemplates.hasDirectiveTemplate(directive.name)) {
                        builder.withInsertHandler(createDirectiveInsertHandler(directive.name))
                    } else {
                        builder
                    }

                    result.addElement(PrioritizedLookupElement.withPriority(withHandler, 1000.0))
                }

            // Show preprocessor functions
            PreprocessorFunctionDatabase.getAllPreprocessorFunctions()
                .filter { it.name.startsWith("%") &&
                         it.name.substring(1).startsWith(prefix, ignoreCase = true) }
                .forEach { function ->
                    val displayName = function.name.substring(1)
                    val builder = LookupElementBuilder.create(displayName)
                        .withTailText(" - ${function.description}", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withCaseSensitivity(false)
                        .bold()

                    val withHandler = if (SyntaxTemplates.hasDirectiveTemplate(function.name)) {
                        builder.withInsertHandler(createDirectiveInsertHandler(function.name))
                    } else {
                        builder
                    }

                    result.addElement(PrioritizedLookupElement.withPriority(withHandler, 900.0))
                }
        }

        /**
         * Add instruction completions
         */
        private fun addInstructionCompletions(prefix: String, result: CompletionResultSet) {
            InstructionDatabase.getAllInstructions()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .forEach { instruction ->
                    val builder = LookupElementBuilder.create(instruction.name)
                        .withTypeText(instruction.category.toString())
                        .withTailText(" - ${instruction.description}", true)
                        .withIcon(AllIcons.Nodes.Function)
                        .withCaseSensitivity(false)
                        .bold()

                    result.addElement(PrioritizedLookupElement.withPriority(builder, 100.0))
                }
        }

        /**
         * Add register completions
         */
        private fun addRegisterCompletions(prefix: String, result: CompletionResultSet) {
            RegisterDatabase.getAllRegisters()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .forEach { register ->
                    val builder = LookupElementBuilder.create(register.name)
                        .withTypeText(register.size.displayName)
                        .withTailText(" - ${register.description}", true)
                        .withIcon(AllIcons.Nodes.Variable)
                        .withCaseSensitivity(false)
                        .bold()

                    result.addElement(PrioritizedLookupElement.withPriority(builder, 100.0))
                }
        }

        /**
         * Add directive completions (with % prefix intact)
         */
        private fun addDirectiveCompletions(prefix: String, result: CompletionResultSet) {
            DirectiveDatabase.getAllDirectives()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .forEach { directive ->
                    val builder = LookupElementBuilder.create(directive.name)
                        .withTailText(" - ${directive.description}", true)
                        .withIcon(AllIcons.Nodes.Artifact)
                        .withCaseSensitivity(false)

                    val withHandler = if (SyntaxTemplates.hasDirectiveTemplate(directive.name)) {
                        builder.withInsertHandler(createDirectiveInsertHandler(directive.name))
                    } else {
                        builder
                    }

                    result.addElement(PrioritizedLookupElement.withPriority(withHandler, 80.0))
                }
        }

        /**
         * Add preprocessor function completions (with % prefix intact)
         */
        private fun addPreprocessorFunctionCompletions(prefix: String, result: CompletionResultSet) {
            PreprocessorFunctionDatabase.getAllPreprocessorFunctions()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .forEach { function ->
                    val builder = LookupElementBuilder.create(function.name)
                        .withTailText(" - ${function.description}", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withCaseSensitivity(false)

                    result.addElement(PrioritizedLookupElement.withPriority(builder, 10.0))
                }
        }
    }
}
