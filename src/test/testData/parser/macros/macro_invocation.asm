; Macro invocation
%macro test 2
    mov %1, %2
%endmacro

test rax, rbx
test rcx, [data]
