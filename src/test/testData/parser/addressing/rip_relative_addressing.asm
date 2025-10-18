; RIP-relative addressing
mov rax, [rel data]
lea rbx, [rip + label]
