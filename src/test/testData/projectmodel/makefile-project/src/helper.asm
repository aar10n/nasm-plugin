; Helper functions for the test program
section .data
    helper_msg db "Helper function called", 10, 0

section .text
    global helper_function

%include "constants.inc"

helper_function:
    ; Do something
    mov rax, 1
    ret
