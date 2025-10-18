%macro BAR 1-3
    nop
%endmacro

%ifmacro BAR 1-3
    call test
%endif
