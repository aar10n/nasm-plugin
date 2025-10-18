; =============================================================================
; Parser Test Cases - Combined from various small test files
; Tests specific parsing edge cases and constructs
; =============================================================================

section .data

; ----------------------------------------------------------------------------
; Test: Labels with space directives (resb/resq/etc)
; ----------------------------------------------------------------------------
section .bss
    ; These labels should be parsed correctly
    aligned_buffer resb 128     ; Reserve 128 bytes
    result resq 1               ; Reserve 1 quadword (8 bytes)
    data_array resd 100         ; Reserve 100 doublewords
    small_buffer resw 64        ; Reserve 64 words

    ; Labels with colons also work
    another_buffer: resb 256

; ----------------------------------------------------------------------------
; Test: Preprocessor expressions in various contexts
; ----------------------------------------------------------------------------
section .data

; Assign with expression using variable 'i'
%assign i 5
%assign test i * 2

; DB with expressions
db i * 2                        ; Identifier in expression
db (i * 2)                      ; Parenthesized expression

; REP directive with DB
%rep 3
    db 0
%endrep

; REP with variable expressions
%assign j 0
%rep 3
    db j * 2
    %assign j j + 1
%endrep

; ----------------------------------------------------------------------------
; Test: Macro parameter special cases
; ----------------------------------------------------------------------------

; Macro with %0 parameter count
%macro param_count_test 3
    ; %0 = parameter count
    db %0
%endmacro

; Macro with string and expression combinations
%macro string_expr_test 0
    db "hello", "world"         ; Multiple strings
    db '0'                      ; Single character
    db '0' + 3                  ; Character + expression
    db "0" + 3                  ; String + expression
    db 48 + 3                   ; Numeric expression
%endmacro

; Macro with various comment styles
%macro comment_test 0
    ; Single comment
    db "test1"
    ; Multiple
    ; consecutive
    ; comments
    db "test2"
%endmacro

; Macro with explicit parameter form
%macro explicit_params 3
    ; %{1} = explicit form
    db "Param count: ", '0' + %0
    mov rax, %1
%endmacro

; ----------------------------------------------------------------------------
; Test: Conditional preprocessing directives
; ----------------------------------------------------------------------------

; IFEMPTY directive
%ifempty
    ; This should not be assembled (empty condition)
%endif

%macro ifempty_test 0
    db "ifempty test"
%endmacro

; ----------------------------------------------------------------------------
; Test: PERCPU offsets (segment override patterns)
; ----------------------------------------------------------------------------
section .text

; Struct percpu_offsets - testing gs: segment override parsing
%define PERCPU_INTR_LEVEL     gs:0x04

test_segment_override:
    mov ax, PERCPU_INTR_LEVEL   ; Should parse gs: prefix correctly
    ret

; ----------------------------------------------------------------------------
; Test: Character literals as immediate operands
; ----------------------------------------------------------------------------
char_literal_tests:
    ; Single character literals
    mov ah, 'a'                  ; Single character
    mov al, 'b'

    ; Multi-character literals (packed)
    mov bx, 'AB'                 ; Two-character literal
    mov ecx, 'ABCD'              ; Four-character literal
    mov rdx, 'test'              ; Four-char literal in 64-bit register

    ; Character literals in expressions
    mov rax, 'A' + 1             ; Should be 0x41 + 1 = 0x42 ('B')
    mov rbx, 'Z' - 'A'           ; Should be 25

    ; Character literals in comparisons
    cmp al, 'x'
    je .found_x

    ; Character literal with TIMES
    times 10 db 'X'

.found_x:
    ret

; ----------------------------------------------------------------------------
; Test: Include directive parsing
; ----------------------------------------------------------------------------
; Note: These test the parser's ability to handle %include
; The actual files may not exist in all environments

%ifdef TEST_INCLUDES
    %include "test_simple.inc"
    %include "common.inc"
    %include "macros.inc"
%endif

; ----------------------------------------------------------------------------
; Main entry point (minimal)
; ----------------------------------------------------------------------------
global _start
_start:
    ; Use some of the test macros
    param_count_test 1, 2, 3
    string_expr_test
    comment_test
    explicit_params 100, 200, 300
    ifempty_test

    ; Test character literals
    call char_literal_tests

    ; Test segment override
    call test_segment_override

    ; Exit
    mov rax, 60                  ; sys_exit
    xor rdi, rdi
    syscall