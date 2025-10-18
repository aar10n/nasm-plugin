; Complex macro test file
; Tests nested macros, conditionals, and parameter handling

%define SIZE 1024
%define SHIFT(x) ((x) << 2)

; Multi-line macro with parameters
%macro PUSH_ALL 0
    push rax
    push rbx
    push rcx
    push rdx
%endmacro

%macro POP_ALL 0
    pop rdx
    pop rcx
    pop rbx
    pop rax
%endmacro

; Macro with parameter range
%macro PRINT 1-2
    mov rdi, %1
    %if %0 == 2
        mov rsi, %2
    %endif
    call printf
%endmacro

; Nested conditionals in macro
%macro ALLOCATE 1
    %if %1 < 256
        sub rsp, %1
    %elif %1 < 4096
        mov rax, %1
        sub rsp, rax
    %else
        ; Large allocation
        mov rdi, %1
        call malloc
    %endif
%endmacro

; Macro concatenation
%macro GEN_FUNC 1
    %1_start:
        push rbp
        mov rbp, rsp
    %1_end:
        pop rbp
        ret
%endmacro

; Greedy parameters
%macro LOG 1+
    mov rdi, log_fmt
    %rep %0
        push %1
        %rotate 1
    %endrep
    call printf
%endmacro

; String operations
%define MSG "Hello"
%strlen MSG_LEN MSG
%substr MSG_FIRST MSG 0 1

; Context-local labels
%push function_context
%$local_var: resq 1
%pop

; Nested macro invocation
section .text
global main

main:
    PUSH_ALL
    ALLOCATE 512
    PRINT format_string, buffer
    POP_ALL
    ret

; Test macro expansion
GEN_FUNC test_function
GEN_FUNC another_function
