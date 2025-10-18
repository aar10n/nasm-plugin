%macro NOPARAM 0
    nop
%endmacro

%ifmacro NOPARAM 0
    call test
%endif
