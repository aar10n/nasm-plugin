; =============================================================================
; Macro Examples - Demonstrates NASM macro capabilities
; Demonstrates: %define, %macro/%endmacro, multi-line macros, parameters
; =============================================================================

section .data
    ; Using %define for constants
    %define STDOUT 1
    %define STDIN 0
    %define SYS_WRITE 1
    %define SYS_READ 0
    %define SYS_EXIT 60

    msg1 db "Simple macro test", 0x0a
    len1 equ $ - msg1

    msg2 db "Multi-parameter macro", 0x0a
    len2 equ $ - msg2

    msg3 db "Nested operations", 0x0a
    len3 equ $ - msg3

section .bss
    input_buffer resb 256

section .text
    global _start

; =============================================================================
; Simple single-line macro
; =============================================================================
%define NEWLINE 0x0a

; =============================================================================
; Multi-line macro with parameters
; Macro: print - Print a string to stdout
; Parameters: %1 = message address, %2 = message length
; =============================================================================
%macro print 2
    mov rax, SYS_WRITE
    mov rdi, STDOUT
    mov rsi, %1
    mov rdx, %2
    syscall
%endmacro

; =============================================================================
; Multi-line macro with default parameter
; Macro: exit - Exit program with return code
; Parameters: %1 = exit code (default 0)
; =============================================================================
%macro exit 0-1 0
    mov rax, SYS_EXIT
    mov rdi, %1
    syscall
%endmacro

; =============================================================================
; Macro with multiple operations
; Macro: save_regs - Save commonly used registers
; =============================================================================
%macro save_regs 0
    push rax
    push rbx
    push rcx
    push rdx
    push rsi
    push rdi
%endmacro

; =============================================================================
; Macro: restore_regs - Restore saved registers
; =============================================================================
%macro restore_regs 0
    pop rdi
    pop rsi
    pop rdx
    pop rcx
    pop rbx
    pop rax
%endmacro

; =============================================================================
; Advanced macro with conditional assembly
; Macro: debug_print - Print message only if DEBUG is defined
; =============================================================================
%ifdef DEBUG
    %macro debug_print 2
        print %1, %2
    %endmacro
%else
    %macro debug_print 2
        ; No-op when DEBUG not defined
    %endmacro
%endif

%if DEBUG
    %define DEBUG_PRINT(msg, len) debug_print msg, len

    %macro assert 3 ; <condition>, <error message>, <error length>
        ; Simple assert macro
        cmp %1, 0
        jne %%assert_fail
        jmp %%assert_end
    %%assert_fail:
        mov rax, SYS_WRITE
        mov rdi, STDOUT
        mov rsi, %2
        mov rdx, %3
        syscall
        exit 1
    %%assert_end:
    %endmacro
%else
    %define DEBUG_PRINT(msg, len)

    %macro assert 3
    ; No-op when DEBUG not defined
    %%assert_end:
    %endmacro
%endif

%define DARWIN 1
%define LINUX  2

%define PLATFORM DARWIN
%if PLATFORM == DARWIN
    ; Macros specific to macOS can be defined here
    %define MACRO_FOR_DARWIN 1
%endif
%if PLATFORM == LINUX
    ; Macros specific to Linux can be defined here
    %define MACRO_FOR_LINUX 1
%endif

; =============================================================================
; Macro with arithmetic operations
; Macro: add_immediate - Add immediate value to register
; =============================================================================
%macro add_immediate 2
    add %1, %2
%endmacro

; =============================================================================
; Variadic macro - accepts variable number of parameters
; Macro: push_all - Push multiple registers onto stack
; =============================================================================
%macro push_all 1-*
    %rep %0
        push %1
        %rotate 1
    %endrep
%endmacro

%macro pop_all 1-*
    %rep %0
        %rotate -1
        pop %1
    %endrep
%endmacro

; =============================================================================
; Main program
; =============================================================================
_start:
    ; Use simple print macro
    print msg1, len1

    ; Use print macro with second message
    print msg2, len2

    ; Demonstrate save/restore macros
    save_regs

    ; Do some work that might modify registers
    mov rax, 42
    mov rbx, 100
    add rax, rbx

    restore_regs

    ; Use variadic macro
    push_all rax, rbx, rcx, rdx

    ; Do something
    print msg3, len3

    ; Restore with variadic macro
    pop_all rdx, rcx, rbx, rax

    ; Debug print (only if DEBUG defined)
    debug_print msg1, len1

    ; Exit with return code 0 using macro
    exit 0

; =============================================================================
; Conditional assembly examples
; =============================================================================
%ifdef EXTRA_FEATURES

extra_function:
    ; This code only compiled if EXTRA_FEATURES is defined
    print msg1, len1
    ret

%endif

; =============================================================================
; Macro repeat example
; =============================================================================
section .data
    ; Generate array using %rep
    repeated_data:
        %assign i 0
        %rep 10
            db i
            %assign i i+1
        %endrep
