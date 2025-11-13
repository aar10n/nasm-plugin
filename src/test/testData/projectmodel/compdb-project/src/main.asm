; Main entry point for the test program
section .data
    msg db "Hello from main", 10, 0

section .text
    global _start
    extern helper_function

%include "constants.inc"

_start:
    ; Call helper function
    call helper_function

    ; Exit
    mov rax, 60
    xor rdi, rdi
    syscall
