; =============================================================================
; Macro Indirection Examples - Demonstrates %[...] construct
; The %[...] construct forces macro expansion in contexts where it wouldn't
; normally occur, allowing for dynamic macro names and advanced metaprogramming
; =============================================================================

section .data
    msg db "Macro indirection test", 0x0a
    len equ $ - msg

section .text
    global _start

; =============================================================================
; Example 1: Using %[...] with __BITS__ to create bit-width specific macros
; =============================================================================

; Define macros for different bit widths
%define Foo16 word
%define Foo32 dword
%define Foo64 qword

; Use macro indirection to select the right one based on current bits
; This expands to: Foo%[__BITS__] which becomes Foo64 in 64-bit mode
; then Foo64 expands to qword
%define CurrentSize Foo%[__BITS__]

; =============================================================================
; Example 2: Indirect macro definition using %xdefine
; =============================================================================

%define Quux 42

; Without %[...], Bar would be defined as the literal text "Quux"
; With %[...], Bar is defined as 42 (the expanded value of Quux)
%xdefine Bar %[Quux]

; =============================================================================
; Example 3: Dynamic macro name construction
; =============================================================================

%define FUNC_PREFIX MyFunc
%define FUNC_SUFFIX _64

; Construct macro name dynamically
%define FullName FUNC_PREFIX%[FUNC_SUFFIX]

; =============================================================================
; Example 4: Conditional macro expansion based on context
; =============================================================================

%ifdef DEBUG
    %define MODE Debug
%else
    %define MODE Release
%endif

%define PrintDebug db "Debug mode", 0x0a
%define PrintRelease db "Release mode", 0x0a

; Use macro indirection to select the right message
; Print%[MODE] expands to either PrintDebug or PrintRelease
%define ModeMsg Print%[MODE]

; =============================================================================
; Example 5: Using %[...] in operands
; =============================================================================

_start:
    ; Example: Use macro indirection in expressions
    ; The %[__BITS__] in CurrentSize gets expanded during preprocessing
    mov rax, qword [msg]  ; CurrentSize would expand to qword in 64-bit mode

    ; Example: Use expanded value in immediate operand
    mov rbx, Bar            ; rbx = 42

    ; Example: Complex expression with macro indirection
    %assign OFFSET 10
    mov rcx, %[OFFSET] + 5  ; rcx = 15

    ; Exit
    mov rax, 60
    xor rdi, rdi
    syscall

; =============================================================================
; Example 6: Using %[...] with %rep for loop unrolling
; =============================================================================

section .bss
    buffer resb 256

section .text

%assign i 0
%rep 4
    ; Use macro indirection in data definition
    %define Label%[i] buffer + i * 64

    ; Generate labels dynamically
    ; Note: label_%[i]: syntax not yet supported in parser
    ; label_%[i]:
    nop

    %assign i i+1
%endrep

; =============================================================================
; Example 7: Nested macro indirection
; =============================================================================

%define A B
%define B C
%define C 99

; %[%[A]] -> %[B] -> C -> 99
%define DeepExpand %[%[A]]

test_nested:
    mov rax, DeepExpand     ; rax = 99
    ret

; =============================================================================
; Example 8: Using %[...] to create type-generic macros
; =============================================================================

%macro LOAD_VALUE 2
    ; %1 = register, %2 = size (8, 16, 32, 64)
    ; TODO: This construct not yet fully supported by parser
    ; %define LOAD_OP%[%2] mov
    ; LOAD_OP%[%2] %1, [buffer]
    mov %1, [buffer]
%endmacro

load_test:
    LOAD_VALUE rax, 64      ; Expands to: mov rax, [buffer]
    LOAD_VALUE ax, 16       ; Expands to: mov ax, [buffer]
    ret
