; Comprehensive addressing mode test file
; Tests all x86_64 addressing modes

section .text

; Direct addressing
mov rax, [0x1000]
mov rbx, [data]

; Indirect addressing
mov rax, [rbx]
mov rcx, [rsp]
mov rdx, [r15]

; Displacement addressing
mov rax, [rbx + 8]
mov rcx, [rsp - 16]
mov rdx, [rbp + 0x20]
mov rsi, [r12 + 0x100]

; Base + index
mov rax, [rbx + rcx]
mov rdx, [rsp + rax]

; Scaled index
mov rax, [rbx + rcx * 2]
mov rdx, [rsi + rdi * 4]
mov r8, [r9 + r10 * 8]

; Base + scaled index
mov rax, [rbx + rcx * 4 + 0x10]
mov rdx, [rsp + rax * 8 + 0x20]
mov r11, [r12 + r13 * 2 + 0x100]

; RIP-relative addressing
mov rax, [rel data]
lea rbx, [rip + label]
mov rcx, [rip + offset]

; Segment override
mov rax, [fs:0x28]
mov rbx, [gs:rcx]
mov rcx, fs:[rdx + 0x10]

; Size specifiers
mov byte [rax], 1
mov word [rbx], 0x1234
mov dword [rcx], 0x12345678
mov qword [rdx], 0x123456789ABCDEF0

; Far pointer
jmp 0x10:0x1000
call far [rbx]

; Complex expressions
mov rax, [rbx + rcx * 8 + (SIZE * 2)]
lea rdi, [rsp + rax * 4 + OFFSET]
mov rdx, qword [rbp - 8 * INDEX + BASE]

; AVX-512 addressing with broadcast
vmovdqa32 zmm0, [rax]{1to16}
vaddps zmm1, zmm2, [rbx + rcx * 4]{1to16}

; Multiple segment overrides (last one wins)
mov rax, fs:[gs:rbx]  ; gs: should be used

; Addressing with all register types
mov rax, [rax]
mov rbx, [ebx]  ; 32-bit register in address
mov rcx, [r8]
mov rdx, [r15d]  ; 32-bit extended register

section .data
data: dq 0
label: dq 0
offset: dq 0
SIZE equ 16
OFFSET equ 0x100
INDEX equ 4
BASE equ 0x1000
