%macro TEST 1
    nop
%endmacro

%ifmacro TEST 2
    call branch1
%elifmacro TEST 1
    call branch2
%else
    call branch3
%endif
