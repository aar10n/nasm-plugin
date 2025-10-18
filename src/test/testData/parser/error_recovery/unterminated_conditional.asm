; Test unterminated conditional - should have errors but not completely fail
%if DEBUG
    mov rax, 1
; Missing %endif
mov rbx, 2
