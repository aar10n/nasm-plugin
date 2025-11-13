NASM = nasm
NASM_FLAGS = -f elf64 -g -DDEBUG=1 -DVERSION=100 -Iinclude
SOURCES = src/main.asm src/helper.asm
OBJECTS = $(SOURCES:.asm=.o)
TARGET = program

all: $(TARGET)

$(TARGET): $(OBJECTS)
	ld -o $(TARGET) $(OBJECTS)

%.o: %.asm
	$(NASM) $(NASM_FLAGS) $< -o $@

clean:
	rm -f $(OBJECTS) $(TARGET)

.PHONY: all clean
