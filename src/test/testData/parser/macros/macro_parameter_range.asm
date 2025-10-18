; Macro with parameter range
%macro print 1-2
    mov rdi, %1
    %if %0 == 2
        mov rsi, %2
    %endif
%endmacro
