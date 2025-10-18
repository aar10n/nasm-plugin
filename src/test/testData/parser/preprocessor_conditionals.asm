; Preprocessor conditional test file
; Tests all conditional directives and combinations

%define DEBUG 1
%define PLATFORM_LINUX 1
%define SIZE 1024

; Basic %if
%if DEBUG
    section .rodata
    debug_msg: db "Debug mode enabled", 10, 0
%endif

; %if with comparison
%if SIZE > 512
    ; Large buffer mode
    %define BUFFER_SIZE SIZE * 2
%endif

; %if %else
%if PLATFORM_LINUX
    extern write
    extern exit
%else
    extern WriteFile
    extern ExitProcess
%endif

; %if %elif %else
%if SIZE == 256
    %define MODE "small"
%elif SIZE == 512
    %define MODE "medium"
%elif SIZE == 1024
    %define MODE "large"
%else
    %define MODE "custom"
%endif

; %ifdef
%ifdef DEBUG
    %define LOG_ENABLED 1
%endif

; %ifndef
%ifndef RELEASE
    %define ASSERTIONS_ENABLED 1
%endif

; Nested conditionals
%if DEBUG
    %ifdef LOG_ENABLED
        %define VERBOSE_LOGGING 1
    %endif
%endif

; %ifidn - token comparison
%define TYPE int
%ifidn TYPE, int
    mov rax, 4
%else
    mov rax, 8
%endif

; %ifidni - case-insensitive token comparison
%define ARCH X64
%ifidni ARCH, x64
    bits 64
%endif

; %ifnum - numeric test
%define VALUE 42
%ifnum VALUE
    mov rax, VALUE
%endif

; %ifstr - string test
%define MESSAGE "Hello"
%ifstr MESSAGE
    section .rodata
    msg: db MESSAGE, 0
%endif

; %ifid - identifier test
%define NAME label
%ifid NAME
    NAME: nop
%endif

; %ifempty - empty test
%macro TEST 0-1
    %ifempty %1
        ; No argument provided
        mov rax, 0
    %else
        ; Argument provided
        mov rax, %1
    %endif
%endmacro

; %ifmacro - macro defined test
%ifmacro TEST 0-1
    ; TEST macro is defined
    TEST
    TEST 42
%endif

; Multiple %elif
%if SIZE < 100
    %define CATEGORY "tiny"
%elif SIZE < 500
    %define CATEGORY "small"
%elif SIZE < 1000
    %define CATEGORY "medium"
%elif SIZE < 5000
    %define CATEGORY "large"
%else
    %define CATEGORY "huge"
%endif

; Deeply nested conditionals
%if DEBUG
    %ifdef VERBOSE_LOGGING
        %if SIZE > 512
            %ifdef PLATFORM_LINUX
                ; All conditions met
                section .text
                global debug_init
                debug_init:
                    mov rax, 1
                    ret
            %endif
        %endif
    %endif
%endif

; %ifctx - context test
%push mycontext
%ifctx mycontext
    ; In mycontext
    %$var: resq 1
%endif
%pop

; %ifenv - environment variable test
%ifenv PATH
    ; PATH is defined
%endif

; Negated conditionals
%ifnnum "string"
    ; Not a number
%endif

%ifnstr 123
    ; Not a string
%endif

%ifnid 123
    ; Not an identifier
%endif

%ifnempty SIZE
    ; SIZE is not empty
%endif

; Complex expression in %if
%if (SIZE > 100) && (SIZE < 2000)
    mov rax, SIZE
%endif

%if (DEBUG == 1) || (VERBOSE_LOGGING == 1)
    call log_function
%endif

; Comparison operators
%if SIZE == 1024
    nop
%endif

%if SIZE != 512
    nop
%endif

%if SIZE < 2048
    nop
%endif

%if SIZE <= 1024
    nop
%endif

%if SIZE > 512
    nop
%endif

%if SIZE >= 1024
    nop
%endif

; Bitwise operations in conditionals
%if (SIZE & 0xFF) == 0
    ; SIZE is aligned to 256
%endif

%if (SIZE | 0xF) == SIZE
    ; Low nibble is all ones
%endif

; Arithmetic in conditionals
%if (SIZE * 2) > 1024
    %define DOUBLE_SIZE_LARGE 1
%endif

%if (SIZE / 2) < 1000
    %define HALF_SIZE_SMALL 1
%endif
