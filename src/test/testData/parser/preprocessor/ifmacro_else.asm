%macro QUX 1
    nop
%endmacro

%ifmacro QUX 2
    call branch1
%else
    call branch2
%endif
