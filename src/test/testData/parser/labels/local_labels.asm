; Local labels test
main:
    .loop:
        dec rcx
        jnz .loop
    ret
