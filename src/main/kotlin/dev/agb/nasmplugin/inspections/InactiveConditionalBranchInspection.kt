package dev.agb.nasmplugin.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import dev.agb.nasmplugin.psi.NasmSourceLine
import dev.agb.nasmplugin.psi.isInInactiveConditionalBranch

/**
 * Inspection that detects and highlights code in inactive preprocessor branches.
 *
 * This inspection evaluates conditional directives (%ifdef, %ifndef, %if, etc.) based on:
 * - Command-line macro definitions from project settings
 * - Macro definitions in the file (%define, %assign)
 * - Macro definitions in included files
 *
 * Code determined to be in an inactive branch is greyed out and excluded from
 * reference resolution, similar to how unused symbols are displayed.
 *
 * Users can disable this inspection if they want to see all conditional branches
 * equally, for example when working on cross-platform assembly code.
 *
 * Example:
 * ```nasm
 * %ifdef DEBUG
 * ; This code is active if DEBUG is defined in settings
 * call debug_log
 * %else
 * ; This code is inactive if DEBUG is defined (greyed out)
 * nop
 * %endif
 * ```
 */
class InactiveConditionalBranchInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // Only check top-level source lines to avoid duplicate annotations
                if (element !is NasmSourceLine) {
                    return
                }

                // Check if this element is in an inactive conditional branch
                if (element.isInInactiveConditionalBranch()) {
                    // Register the problem with LIKE_UNUSED_SYMBOL highlighting
                    // This gives it the standard greyed-out appearance
                    holder.registerProblem(
                        element,
                        "Code in inactive preprocessor branch",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL
                    )
                }
            }
        }
    }

    override fun getDisplayName(): String = "Inactive preprocessor branch"

    override fun getGroupDisplayName(): String = "NASM"

    override fun getShortName(): String = "NasmInactiveConditionalBranch"

    override fun getStaticDescription(): String? {
        return "Reports code inside inactive preprocessor conditional branches based on current macro definitions."
    }

    // Enable by default
    override fun isEnabledByDefault(): Boolean = true
}
