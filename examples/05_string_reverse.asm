; =============================================================================
; String Reversal Program - Real-world example
; Demonstrates: syscalls, functions, pointer manipulation, buffer operations
; =============================================================================

section .data
    ; Input/Output strings
    input_prompt db "Enter a string (max 255 chars): ", 0
    input_prompt_len equ $ - input_prompt

    output_msg db "Reversed string: ", 0
    output_msg_len equ $ - output_msg

    newline db 0x0a
    newline_len equ $ - newline

    ; Error messages
    error_msg db "Error: String too long!", 0x0a
    error_msg_len equ $ - error_msg

    ; Constants
    MAX_INPUT_SIZE equ 256
    STDIN equ 0
    STDOUT equ 1
    SYS_READ equ 0
    SYS_WRITE equ 1
    SYS_EXIT equ 60

section .bss
    input_buffer resb MAX_INPUT_SIZE
    output_buffer resb MAX_INPUT_SIZE
    bytes_read resq 1

section .text
    global _start

; =============================================================================
; Macro: print_string
; Print a string to stdout
; Parameters: %1 = address, %2 = length
; =============================================================================
%macro print_string 2
    mov rax, SYS_WRITE
    mov rdi, STDOUT
    mov rsi, %1
    mov rdx, %2
    syscall
%endmacro

; =============================================================================
; Function: read_input
; Read a line from stdin
; Input: rdi = buffer address, rsi = max size
; Output: rax = bytes read
; =============================================================================
global read_input
read_input:
    push rbp
    mov rbp, rsp

    ; Save parameters
    mov r12, rdi                ; r12 = buffer address
    mov r13, rsi                ; r13 = max size

    ; Read from stdin
    mov rax, SYS_READ
    mov rdi, STDIN
    mov rsi, r12
    mov rdx, r13
    syscall

    ; Remove newline if present
    cmp rax, 0
    jle .done

    mov rcx, rax
    dec rcx
    mov byte [r12 + rcx], 0     ; Replace newline with null terminator

.done:
    ; Return bytes read (excluding newline)
    dec rax

    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Function: string_length
; Calculate length of null-terminated string
; Input: rdi = string address
; Output: rax = length
; =============================================================================
global string_length
string_length:
    push rbp
    mov rbp, rsp

    xor rax, rax                ; Length counter
    mov rcx, rdi                ; Current position

.loop:
    cmp byte [rcx], 0           ; Check for null terminator
    je .done
    inc rax
    inc rcx
    jmp .loop

.done:
    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Function: reverse_string
; Reverse a string in-place
; Input: rdi = string address, rsi = length
; Modifies: string in place
; =============================================================================
reverse_string:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    push r13

    ; Setup pointers
    mov r12, rdi                ; r12 = start pointer
    mov r13, rdi
    add r13, rsi
    dec r13                     ; r13 = end pointer (last char)

    ; Check if string is empty or single char
    cmp rsi, 1
    jle .done

.swap_loop:
    ; Check if pointers have crossed
    cmp r12, r13
    jge .done

    ; Swap characters
    mov al, byte [r12]          ; al = *start
    mov bl, byte [r13]          ; bl = *end
    mov byte [r12], bl          ; *start = bl
    mov byte [r13], al          ; *end = al

    ; Move pointers
    inc r12
    dec r13

    jmp .swap_loop

.done:
    pop r13
    pop r12
    pop rbx
    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Function: copy_string
; Copy string from source to destination
; Input: rdi = destination, rsi = source, rdx = length
; =============================================================================
copy_string:
    push rbp
    mov rbp, rsp
    push rcx
    push rsi
    push rdi

    mov rcx, rdx                ; Counter

.copy_loop:
    cmp rcx, 0
    je .done

    mov al, byte [rsi]
    mov byte [rdi], al

    inc rsi
    inc rdi
    dec rcx

    jmp .copy_loop

.done:
    ; Null terminate
    mov byte [rdi], 0

    pop rdi
    pop rsi
    pop rcx
    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Function: validate_input
; Check if input is within acceptable range
; Input: rax = input length
; Output: rax = 1 if valid, 0 if invalid
; =============================================================================
validate_input:
    push rbp
    mov rbp, rsp

    ; Check if length is 0
    cmp rax, 0
    jle .invalid

    ; Check if length exceeds maximum
    cmp rax, MAX_INPUT_SIZE - 1
    jg .invalid

    mov rax, 1                  ; Valid
    jmp .done

.invalid:
    xor rax, rax                ; Invalid

.done:
    mov rsp, rbp
    pop rbp
    ret

; =============================================================================
; Main program
; =============================================================================
_start:
    ; Print input prompt
    print_string input_prompt, input_prompt_len

    ; Read user input
    lea rdi, [input_buffer]
    mov rsi, MAX_INPUT_SIZE
    call read_input

    ; Save length
    mov [bytes_read], rax

    ; Validate input
    call validate_input
    cmp rax, 0
    je .error_exit

    ; Copy input to output buffer
    lea rdi, [output_buffer]
    lea rsi, [input_buffer]
    mov rdx, [bytes_read]
    call copy_string

    ; Reverse the string in output buffer
    lea rdi, [output_buffer]
    mov rsi, [bytes_read]
    call reverse_string

    ; Print output message
    print_string output_msg, output_msg_len

    ; Print reversed string
    mov rax, SYS_WRITE
    mov rdi, STDOUT
    lea rsi, [output_buffer]
    mov rdx, [bytes_read]
    syscall

    ; Print newline
    print_string newline, newline_len

    ; Exit successfully
    mov rax, SYS_EXIT
    xor rdi, rdi
    syscall

.error_exit:
    ; Print error message
    print_string error_msg, error_msg_len

    ; Exit with error code
    mov rax, SYS_EXIT
    mov rdi, 1
    syscall
