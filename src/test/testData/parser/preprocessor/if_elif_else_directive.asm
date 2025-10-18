; If-elif-else directive
%if SIZE == 8
    mov rax, 1
%elif SIZE == 16
    mov rax, 2
%elif SIZE == 32
    mov rax, 3
%else
    mov rax, 0
%endif
