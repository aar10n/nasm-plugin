%macro BAZ 2
    nop
%endmacro

%ifnmacro BAZ 1
    ; BAZ is not defined with 1 param
    call alternative
%endif
