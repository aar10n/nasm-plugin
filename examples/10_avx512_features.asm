; AVX512 Braced Syntax Test
; Tests decorator syntax: {k1}, {z}, {1to16}, {sae}, {rn-sae}

BITS 64
DEFAULT REL

section .data
    align 64
buffer: times 64 db 0

section .text
global _start

_start:
    ; Test 1: Single masking decorator
    vaddps zmm0{k1}, zmm1, zmm2

    ; Test 2: Double decorators - masking + zeroing
    vaddps zmm0{k1}{z}, zmm1, zmm2

    ; Test 3: Memory operand with broadcast
    vaddps zmm0, zmm1, [buffer]{1to16}

    ; Test 4: Masking + zeroing + broadcast
    vaddps zmm0{k2}{z}, zmm1, [buffer]{1to16}

    ; Test 5: Different mask registers
    vmulps zmm3{k3}, zmm4, zmm5
    vsubps zmm6{k4}{z}, zmm7, zmm8

    ; Test 6: Mask registers as regular operands
    kmovw k1, eax
    kmovb k2, ebx
    kmovq k3, rdx
    kmovd k4, ecx

    ; Test 7: Various broadcasts
    vaddps zmm10, zmm11, [buffer]{1to2}
    vaddps zmm12, zmm13, [buffer]{1to4}
    vaddps zmm14, zmm15, [buffer]{1to8}

    ; Test 8: All ZMM registers
    vmovaps zmm0, zmm1
    vmovaps zmm16, zmm17
    vmovaps zmm31, [buffer]

    ; Test 9: Multiple instructions with decorators
    vfmadd231ps zmm20{k5}, zmm21, zmm22
    vfmadd231ps zmm23{k6}{z}, zmm24, zmm25

    ret
