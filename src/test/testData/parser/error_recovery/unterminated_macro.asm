; Test unterminated macro - should have errors but not completely fail
%macro test 0
    mov rax, 1
; Missing %endmacro
mov rbx, 2
