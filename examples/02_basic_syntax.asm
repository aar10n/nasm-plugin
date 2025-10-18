%include "filename"
mov rax, 'x'

; Simple NASM test file
section .data
    msg db "Hello, World!", 0x0a
    len equ $ - msg

section .text
    global _start

_start:
    ; Write message to stdout
    mov rax, 1          ; sys_write
    mov rdi, 1          ; stdout
    mov rsi, msg        ; message address
    mov rdx, len        ; message length
    syscall

    ; Exit program
    mov rax, 60         ; sys_exit
    xor rdi, rdi        ; return code 0
    syscall
