; AVX-512 feature test file
; Tests AVX-512 decorators, masking, and broadcast

section .text

; Basic AVX-512 instructions
vaddps zmm0, zmm1, zmm2
vaddpd zmm3, zmm4, zmm5
vmulps zmm6, zmm7, zmm8

; Masking with k registers
vaddps zmm0 {k1}, zmm1, zmm2
vaddpd zmm3 {k2}, zmm4, zmm5
vmulps zmm6 {k3}, zmm7, zmm8

; Zeroing masking
vaddps zmm0 {k1}{z}, zmm1, zmm2
vaddpd zmm3 {k2}{z}, zmm4, zmm5
vmulps zmm6 {k3}{z}, zmm7, zmm8

; Broadcasting
vaddps zmm0, zmm1, [rax]{1to16}
vaddpd zmm2, zmm3, [rbx]{1to8}
vmulps zmm4, zmm5, qword [rcx]{1to16}

; Broadcasting with masking
vaddps zmm0 {k1}, zmm1, [rax]{1to16}
vaddpd zmm2 {k2}{z}, zmm3, [rbx]{1to8}

; Rounding modes
vcvtpd2ps ymm0, zmm1, {rn-sae}
vcvtps2pd zmm2, ymm3, {rd-sae}
vrndscaleps zmm4, zmm5, 0, {ru-sae}
vsqrtps zmm6, zmm7, {rz-sae}

; SAE (Suppress All Exceptions)
vaddps zmm0, zmm1, zmm2, {sae}
vmulpd zmm3, zmm4, zmm5, {sae}

; Combining masking, zeroing, and SAE
vaddps zmm0 {k1}{z}, zmm1, zmm2, {sae}

; Combining masking and broadcast
vaddps zmm0 {k1}, zmm1, [rax]{1to16}
vfmadd213ps zmm2 {k2}{z}, zmm3, [rbx]{1to16}

; Memory operands with full decoration
vaddps zmm0 {k1}, zmm1, zmmword [rax + rbx * 8]{1to16}
vfmadd132pd zmm2 {k2}{z}, zmm3, qword [rcx + rdx * 4 + 0x100]{1to8}

; Mask register operations
kandw k1, k2, k3
korq k4, k5, k6
knotb k7, k0
kxorw k1, k2, k3

; Mask register moves
kmovw k1, eax
kmovq k2, rax
kmovb eax, k3
kmovd rax, k4

; All broadcast sizes
vaddps zmm0, zmm1, [rax]{1to2}
vaddps zmm0, zmm1, [rax]{1to4}
vaddps zmm0, zmm1, [rax]{1to8}
vaddps zmm0, zmm1, [rax]{1to16}
vaddps zmm0, zmm1, [rax]{1to32}

; All rounding modes
vcvtpd2ps ymm0, zmm1, {rn-sae}  ; Round to nearest
vcvtpd2ps ymm0, zmm1, {rd-sae}  ; Round down
vcvtpd2ps ymm0, zmm1, {ru-sae}  ; Round up
vcvtpd2ps ymm0, zmm1, {rz-sae}  ; Round toward zero

; Complex instruction with all features
vfmadd213ps zmm0 {k1}{z}, zmm1, zmmword [rax + rbx * 8 + 0x100]{1to16}

; EVEX prefix forcing
{evex} add rax, rbx
{evex} mov rcx, [rdx]

; Multiple decorators in one instruction
vaddps zmm0 {k1}{z}, zmm1, [rax]{1to16}, {sae}

section .data
align 64
data: times 16 dd 1.0
