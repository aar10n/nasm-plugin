; AVX-512 instructions with masking and broadcasting
vaddps zmm0, zmm1, zmm2
vaddps zmm0 {k1}, zmm1, zmm2
vaddps zmm0 {k1}{z}, zmm1, zmm2
vaddpd zmm0, zmm1, [rax]{1to8}
vcvtpd2ps ymm0, zmm1, {rn-sae}
