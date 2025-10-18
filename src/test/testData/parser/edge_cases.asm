; Edge cases test file
; Tests unusual but valid NASM syntax

; Labels with unusual characters
$label: nop
_start: nop
.local: nop
..global: nop
label.with.dots: nop
label$with$dollars: nop
label_with_underscores: nop
label@special: nop
label#hash: nop

; Very long label
this_is_a_very_long_label_name_that_exceeds_normal_length_expectations_but_should_still_work: nop

; Keywords as labels (when not ambiguous)
data: db 0
text: db 0

; Multiple labels on one line
label1: label2: label3: nop

; Empty lines with various whitespace




; Instructions split across lines (not officially supported but some assemblers allow)
mov \
    rax, \
    rbx

; Massive expressions
mov rax, ((((1 + 2) * 3) - 4) / 5) + ((6 * 7) - (8 / 2))

; Numbers with lots of underscores
mov rax, 1_2_3_4_5_6_7_8_9_0
mov rbx, 0x_FF_FF_FF_FF
mov rcx, 0b_1010_1010_1010_1010

; Unicode in comments
; これはコメントです
; Это комментарий
; هذا تعليق
; 这是评论

; Unicode in strings
msg1: db "Hello 世界", 0
msg2: db "Привет мир", 0
msg3: db "مرحبا بالعالم", 0

; Emoji in comments
; 🚀 This is a rocket
; 🔥 This is fire
; 💻 This is a computer

; Very long string
long_string: db "This is a very long string that goes on and on and on and on and on and on and on and on and on and on and on and on and on and on and on and on and on and on and on and on", 0

; Empty string
empty_string: db ""

; String with all escape sequences
escapes: db "Line1\nLine2\tTab\rReturn\0Null", 0

; Character literals
char1: db 'A'
char2: db '\n'
char3: db '\x41'

; Backtick strings
backtick_msg: db `This uses backticks`, 0

; Mixed quotes in data
mixed: db "Double", 'Single', `Backtick`, 0

; Question marks (uninitialized data)
uninitialized1: db ?
uninitialized2: dw ?
uninitialized_array: times 100 db ?

; DUP with nested values
matrix: dd 4 dup(1, 2, 3, 4)
nested_dup: db 10 dup(5 dup(0))

; Floating point edge cases
float_normal: dd 3.14159
float_exp: dd 1.0e10
float_exp_neg: dd 1.0e-10
float_no_int: dd .5
float_no_frac: dd 5.

; Special floating point values
special1: dq __infinity__
special2: dq __nan__
special3: dq __qnan__
special4: dq __snan__

; All numeric formats for same value
hex1: db 0xFF
hex2: db 0FFh
hex3: db $FF
dec1: db 255
dec2: db 255d
dec3: db 0d255
bin1: db 11111111b
bin2: db 0b11111111
oct1: db 377o
oct2: db 0o377

; Segment override in unusual places
mov ax, es:[bx]
mov byte fs:[0], al

; Instruction with all possible prefixes
lock xacquire add dword [counter], 1

; Times with macro parameter
%macro TEST 1
    times %1 nop
%endmacro

TEST 10

; Data with macro expansion
%define SIZE 16
buffer: resb SIZE

; EQU with expression
BUFFER_SIZE equ SIZE * 2
OFFSET equ (BASE + 0x100) & 0xFF00

; Macro with 0 parameters
%macro NOARGS 0
    nop
%endmacro

NOARGS

; Macro with many parameters
%macro MANYARGS 10
    mov rax, %1
    mov rbx, %2
    mov rcx, %3
    mov rdx, %4
    mov rsi, %5
    mov rdi, %6
    mov r8, %7
    mov r9, %8
    mov r10, %9
    mov r11, %10
%endmacro

; Empty macro
%macro EMPTY 0
%endmacro

; Macro with only comments
%macro COMMENTS_ONLY 0
    ; Just a comment
    ; Another comment
%endmacro

; Label at end of file without newline
last_label: nop