package dev.agb.nasmplugin.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.agb.nasmplugin.NasmIcons
import javax.swing.Icon

/**
 * Color settings page for NASM syntax highlighting customization
 */
class NasmColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = NasmIcons.NASM

    override fun getHighlighter(): SyntaxHighlighter = NasmSyntaxHighlighter()

    override fun getDemoText(): String = """
        |; NASM x86_64 Assembly Example
        |section .data
        |    msg db "Hello, World!", 0x0a
        |    len equ $ - msg
        |
        |section .bss
        |    buffer resb 64
        |
        |section .text
        |    global _start
        |
        |%macro <macro_name>print</macro_name> 2
        |    <instruction>mov</instruction> rax, 1
        |    <instruction>mov</instruction> rdi, 1
        |    <instruction>mov</instruction> rsi, %1
        |    <instruction>mov</instruction> rdx, %2
        |    <instruction>syscall</instruction>
        |%endmacro
        |
        |%define <macro_name>MAX_SIZE</macro_name> 100
        |
        |_start:
        |    ; Print message
        |    <macro_name>print</macro_name> msg, len
        |
        |    ; Use defined constant
        |    <instruction>mov</instruction> rax, <macro_name>MAX_SIZE</macro_name>
        |
        |    ; RIP-relative addressing
        |    <instruction>lea</instruction> rdi, [rel buffer]
        |
        |    ; String operations with prefixes
        |    rep <instruction>movsb</instruction>
        |    repe <instruction>cmpsb</instruction>
        |
        |    ; Atomic operations with size modifier
        |    lock <instruction>inc</instruction> qword [buffer]
        |
        |    ; DUP construct
        |    times 10 db 0
        |
        |    ; Exit
        |    <instruction>mov</instruction> rax, 60
        |    <instruction>xor</instruction> rdi, rdi
        |    <instruction>syscall</instruction>
    """.trimMargin()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        mapOf(
            "macro_name" to NasmSyntaxHighlighter.MACRO_INVOCATION,
            "instruction" to NasmSyntaxHighlighter.INSTRUCTION
        )

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = arrayOf(
        AttributesDescriptor("Comment", NasmSyntaxHighlighter.COMMENT),
        AttributesDescriptor("Instruction", NasmSyntaxHighlighter.INSTRUCTION),
        AttributesDescriptor("Instruction prefix", NasmSyntaxHighlighter.INSTRUCTION_PREFIX),
        AttributesDescriptor("Register", NasmSyntaxHighlighter.REGISTER),
        AttributesDescriptor("Directive", NasmSyntaxHighlighter.DIRECTIVE),
        AttributesDescriptor("Label", NasmSyntaxHighlighter.LABEL),
        AttributesDescriptor("Number", NasmSyntaxHighlighter.NUMBER),
        AttributesDescriptor("String", NasmSyntaxHighlighter.STRING),
        AttributesDescriptor("Operator", NasmSyntaxHighlighter.OPERATOR),
        AttributesDescriptor("Identifier", NasmSyntaxHighlighter.IDENTIFIER),
        AttributesDescriptor("Macro directive", NasmSyntaxHighlighter.MACRO),
        AttributesDescriptor("Macro name", NasmSyntaxHighlighter.MACRO_INVOCATION),
        AttributesDescriptor("Modifier keyword", NasmSyntaxHighlighter.MODIFIER)
    )

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "NASM Assembly"
}
