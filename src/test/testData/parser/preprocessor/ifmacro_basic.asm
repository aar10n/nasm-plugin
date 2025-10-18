%macro FOO 1
    nop
%endmacro

%ifmacro FOO 1
    call test
%endif
