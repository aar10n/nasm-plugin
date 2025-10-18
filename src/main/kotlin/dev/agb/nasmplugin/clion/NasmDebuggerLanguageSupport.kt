package dev.agb.nasmplugin.clion

import com.jetbrains.cidr.execution.debugger.CidrDebuggerLanguageSupport
import com.jetbrains.cidr.execution.debugger.CidrEvaluator
import com.jetbrains.cidr.execution.debugger.CidrStackFrame

/**
 * Provides debugger language support for NASM assembly files.
 * This registers NASM as a debuggable language with CLion's native debugger (GDB/LLDB),
 * allowing breakpoints in NASM files to work correctly during debugging sessions.
 */
class NasmDebuggerLanguageSupport : CidrDebuggerLanguageSupport() {

    /**
     * Create an evaluator for expression evaluation in NASM assembly context.
     * Returns a basic CidrEvaluator that uses the default evaluation logic
     * for handling register names, memory addresses, etc.
     */
    override fun createEvaluator(frame: CidrStackFrame): CidrEvaluator {
        // Use the default CidrEvaluator implementation
        return CidrEvaluator(frame)
    }
}
