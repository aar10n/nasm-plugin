; =============================================================================
; Advanced NASM Features
; Demonstrates: addressing modes, instruction prefixes, size specifiers,
;               expressions, various register sizes, control flow
; =============================================================================

section .data
    ; Constants with different number formats
    hex_num equ 0x1234          ; Hexadecimal
    dec_num equ 42              ; Decimal
    bin_num equ 0b10101010      ; Binary
    oct_num equ 0o755           ; Octal

    ; Complex constant expressions
    buffer_size equ 1024
    half_buffer equ buffer_size / 2
    total_size equ buffer_size * 2 + 16

    ; String with escape sequences
    prompt db "Enter value: ", 0
    prompt_len equ $ - prompt

    newline db 0x0a

    ; Aligned data
    align 8
    aligned_data dq 0x1122334455667788

    ; Array with different elements
    mixed_array:
        db 1, 2, 3, 4           ; bytes
        dw 0x1234               ; word
        dd 0xDEADBEEF           ; dword
        dq 0x123456789ABCDEF0   ; qword

section .bss
    ; Aligned buffer
    align 16
    aligned_buffer resb 128

    result resq 1

section .text
    global _start

; =============================================================================
; Function: demonstrate_addressing_modes
; Shows various x86_64 addressing modes
; =============================================================================
demonstrate_addressing_modes:
    ; Direct register addressing
    mov rax, rbx
    mov ecx, edx
    mov ax, bx
    mov al, bl

    ; Immediate addressing
    mov rax, 0x1234
    mov rbx, 42
    mov rcx, -1

    ; Memory addressing modes
    ; 1. Direct memory addressing
    mov rax, [aligned_data]

    ; 2. Register indirect
    lea rbx, [aligned_data]
    mov rax, [rbx]

    ; 3. Base + displacement
    mov rax, [rbx + 8]

    ; 4. Base + index
    mov rcx, 1
    mov rax, [rbx + rcx]

    ; 5. Base + index + displacement
    mov rax, [rbx + rcx + 16]

    ; 6. Base + index * scale
    mov rax, [rbx + rcx * 8]

    ; 7. Base + index * scale + displacement
    mov rax, [rbx + rcx * 8 + 16]

    ; 8. RIP-relative addressing (x86_64 specific)
    mov rax, [rel aligned_data]

    ret

; =============================================================================
; Function: demonstrate_size_specifiers
; Shows explicit size specifiers for memory operations
; =============================================================================
demonstrate_size_specifiers:
    lea rbx, [aligned_buffer]

    ; Size specifiers
    mov byte [rbx], 0xFF
    mov word [rbx + 1], 0x1234
    mov dword [rbx + 3], 0xDEADBEEF
    mov qword [rbx + 7], 0x123456789ABCDEF0

    ; Load with size specifiers
    movzx rax, byte [rbx]       ; Zero-extend byte to qword
    movzx rax, word [rbx]       ; Zero-extend word to qword
    movsx rax, byte [rbx]       ; Sign-extend byte to qword
    movsxd rax, dword [rbx]     ; Sign-extend dword to qword

    ret

; =============================================================================
; Function: demonstrate_arithmetic
; Complex arithmetic and logical operations
; =============================================================================
demonstrate_arithmetic:
    ; Basic arithmetic
    mov rax, 100
    mov rbx, 50
    add rax, rbx                ; rax = 150
    sub rax, 25                 ; rax = 125
    imul rax, 2                 ; rax = 250

    ; Division (rdx:rax / divisor)
    xor rdx, rdx                ; Clear rdx
    mov rcx, 5
    div rcx                     ; rax = rax / 5

    ; Increment/Decrement
    inc rax
    dec rbx

    ; Logical operations
    mov rax, 0b11110000
    mov rbx, 0b10101010
    and rax, rbx                ; Bitwise AND
    or rax, 0x0F                ; Bitwise OR
    xor rax, rax                ; Clear register (common idiom)
    not rax                     ; Bitwise NOT

    ; Shift operations
    mov rax, 0xFF
    shl rax, 4                  ; Shift left by 4 bits
    shr rax, 2                  ; Shift right by 2 bits
    sal rax, 1                  ; Arithmetic shift left
    sar rax, 1                  ; Arithmetic shift right

    ; Rotate operations
    rol rax, 8                  ; Rotate left
    ror rax, 4                  ; Rotate right

    ; Bit manipulation
    mov rax, 0x1234
    bt rax, 5                   ; Bit test
    bts rax, 7                  ; Bit test and set
    btr rax, 3                  ; Bit test and reset
    btc rax, 10                 ; Bit test and complement

    ret

; =============================================================================
; Function: demonstrate_control_flow
; Various control flow instructions
; =============================================================================
demonstrate_control_flow:
    mov rcx, 10

.loop_start:
    dec rcx
    jz .loop_end                ; Jump if zero

    ; Conditional jumps
    cmp rcx, 5
    je .equal                   ; Jump if equal
    jne .not_equal              ; Jump if not equal
    jg .greater                 ; Jump if greater (signed)
    jl .less                    ; Jump if less (signed)
    ja .above                   ; Jump if above (unsigned)
    jb .below                   ; Jump if below (unsigned)

.equal:
.not_equal:
.greater:
.less:
.above:
.below:
    jmp .loop_start             ; Unconditional jump

.loop_end:
    ; Test and conditional move
    mov rax, 100
    mov rbx, 200
    cmp rax, rbx
    cmovg rax, rbx              ; Conditional move if greater

    ; Set byte on condition
    xor rax, rax
    cmp rbx, 200
    sete al                     ; Set AL to 1 if equal, 0 otherwise

    ret

; =============================================================================
; Function: demonstrate_stack_operations
; Stack frame and calling convention
; =============================================================================
demonstrate_stack_operations:
    ; Standard function prologue
    push rbp
    mov rbp, rsp
    sub rsp, 32                 ; Allocate 32 bytes of stack space

    ; Save callee-saved registers
    push rbx
    push r12
    push r13
    push r14
    push r15

    ; Use stack space
    mov qword [rbp - 8], 0x1234
    mov qword [rbp - 16], 0x5678

    ; Restore callee-saved registers
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx

    ; Standard function epilogue
    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Function: demonstrate_string_operations
; String/array operations with REP prefix
; =============================================================================
demonstrate_string_operations:
    ; Clear buffer using REP STOSB
    lea rdi, [aligned_buffer]
    mov rcx, 128
    xor rax, rax
    rep stosb                   ; Repeat STOSB while RCX > 0

    ; Copy data using REP MOVSB
    lea rsi, [mixed_array]
    lea rdi, [aligned_buffer]
    mov rcx, 20
    rep movsb

    ; Compare strings using REP CMPSB
    lea rsi, [mixed_array]
    lea rdi, [aligned_buffer]
    mov rcx, 20
    repe cmpsb                  ; Repeat while equal and RCX > 0

    ; Scan for value using REP SCASB
    lea rdi, [aligned_buffer]
    mov rcx, 128
    mov al, 0xFF
    repne scasb                 ; Repeat while not equal and RCX > 0

    ret

; =============================================================================
; Main program
; =============================================================================
_start:
    call demonstrate_addressing_modes
    call demonstrate_size_specifiers
    call demonstrate_arithmetic
    call demonstrate_control_flow
    call demonstrate_stack_operations
    call demonstrate_string_operations

    ; Exit
    mov rax, 60
    xor rdi, rdi
    syscall

; =============================================================================
; Data after code section
; =============================================================================
section .data
    ; TIMES directive - repeat initialization
    zeros times 64 db 0
    ones times 32 db 0xFF

    ; Complex initialization
    lookup_table:
        %assign i 0
        %rep 256
            db i * 2
            %assign i i + 1
        %endrep
