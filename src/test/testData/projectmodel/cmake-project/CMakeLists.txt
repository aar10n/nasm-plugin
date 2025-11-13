cmake_minimum_required(VERSION 3.20)
project(nasm_test_project ASM_NASM)

enable_language(ASM_NASM)

# Set NASM compiler flags
set(CMAKE_ASM_NASM_FLAGS "-f elf64 -g -DDEBUG=1 -DVERSION=100")

# Configure NASM compilation with include path
set(CMAKE_ASM_NASM_COMPILE_OBJECT
    "<CMAKE_ASM_NASM_COMPILER> <FLAGS> -I${CMAKE_SOURCE_DIR}/include <SOURCE> -o <OBJECT>")

# Enable compile_commands.json generation
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Add NASM sources
add_library(test_lib STATIC
    src/main.asm
    src/helper.asm
)
