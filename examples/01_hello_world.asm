; =============================================================================
; Basic Hello World - Simple NASM x86_64 program
; Demonstrates: basic sections, data declarations, simple instructions
; =============================================================================

section .data
    ; String literals with various escape sequences
    msg db "Hello, World!", 0x0a, 0x00
    msg_len equ $ - msg         ; Calculate length using $ operator

    ; Different data sizes
    byte_val db 0xFF            ; 1 byte
    word_val dw 0x1234          ; 2 bytes
    dword_val dd 0xDEADBEEF     ; 4 bytes
    qword_val dq 0x123456789ABCDEF0  ; 8 bytes

    ; Multiple values
    array db 1, 2, 3, 4, 5
    numbers dd 100, 200, 300, 400

section .bss
    ; Uninitialized data
    buffer resb 64              ; Reserve 64 bytes
    counter resq 1              ; Reserve 1 qword (8 bytes)

section .text
    global _start

_start:
    ; Write message to stdout using syscall
    mov rax, 1                  ; sys_write
    mov rdi, 1                  ; stdout
    mov rsi, msg                ; message address
    mov rdx, msg_len            ; message length
    syscall

    ; Exit program
    mov rax, 60                 ; sys_exit
    xor rdi, rdi                ; return code 0
    syscall
