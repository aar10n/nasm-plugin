%macro THREAD_FRAME 1-2
    push %1
%endmacro

%ifmacro THREAD_FRAME 1-2
    ; Macro is defined with 1-2 params
    THREAD_FRAME rdi
%elifmacro THREAD_FRAME 1
    ; Macro is defined with exactly 1 param
    THREAD_FRAME rdi
%elifnmacro THREAD_FRAME 1-2
    ; Macro is NOT defined with 1-2 params
    nop
%else
    ; Fallback
    nop
%endif
