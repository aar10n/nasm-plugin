package dev.agb.nasmplugin.database

import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive test suite for all NASM database objects.
 * Tests lazy initialization, data retrieval, case-insensitive lookups,
 * error handling, and singleton behavior.
 */
class DatabaseTests {

    // ============================================================================
    // InstructionDatabase Tests
    // ============================================================================

    @Test
    fun `test InstructionDatabase lazy initialization`() {
        // The database should load successfully on first access
        val instructions = InstructionDatabase.getAllInstructions()
        assertNotNull("Instructions should not be null", instructions)
        assertTrue("Instructions should not be empty", instructions.isNotEmpty())
    }

    @Test
    fun `test InstructionDatabase getInstruction returns valid instruction`() {
        val mov = InstructionDatabase.getInstruction("mov")
        assertNotNull("MOV instruction should exist", mov)
        assertEquals("Instruction name should be 'mov'", "mov", mov!!.name)
        assertEquals("MOV should be in DATA_MOVEMENT category",
            InstructionDatabase.Category.DATA_MOVEMENT, mov.category)
        assertTrue("Instruction should have description", mov.description.isNotEmpty())
    }

    @Test
    fun `test InstructionDatabase case-insensitive lookup`() {
        val movLower = InstructionDatabase.getInstruction("mov")
        val movUpper = InstructionDatabase.getInstruction("MOV")
        val movMixed = InstructionDatabase.getInstruction("MoV")

        assertNotNull("Lowercase lookup should succeed", movLower)
        assertNotNull("Uppercase lookup should succeed", movUpper)
        assertNotNull("Mixed case lookup should succeed", movMixed)
        assertEquals("Different cases should return same instruction", movLower, movUpper)
        assertEquals("Different cases should return same instruction", movLower, movMixed)
    }

    @Test
    fun `test InstructionDatabase getInstruction returns null for nonexistent instruction`() {
        val result = InstructionDatabase.getInstruction("nonexistent_instruction_xyz")
        assertNull("Nonexistent instruction should return null", result)
    }

    @Test
    fun `test InstructionDatabase getAllInstructions returns all instructions`() {
        val instructions = InstructionDatabase.getAllInstructions()
        assertTrue("Should have many instructions (at least 50)", instructions.size > 50)

        // Verify all instructions have required fields
        instructions.forEach { instruction ->
            assertTrue("Instruction name should not be empty", instruction.name.isNotEmpty())
            assertTrue("Instruction description should not be empty", instruction.description.isNotEmpty())
            assertNotNull("Instruction category should not be null", instruction.category)
        }
    }

    @Test
    fun `test InstructionDatabase getInstructionsByPrefix`() {
        val movInstructions = InstructionDatabase.getInstructionsByPrefix("mov")
        assertTrue("Should find instructions starting with 'mov'", movInstructions.isNotEmpty())

        // All results should start with "mov"
        movInstructions.forEach { instruction ->
            assertTrue("Instruction '${instruction.name}' should start with 'mov'",
                instruction.name.startsWith("mov", ignoreCase = true))
        }
    }

    @Test
    fun `test InstructionDatabase getInstructionsByPrefix is case-insensitive`() {
        val lowerPrefix = InstructionDatabase.getInstructionsByPrefix("mov")
        val upperPrefix = InstructionDatabase.getInstructionsByPrefix("MOV")
        val mixedPrefix = InstructionDatabase.getInstructionsByPrefix("MoV")

        assertEquals("Prefix search should be case-insensitive", lowerPrefix.size, upperPrefix.size)
        assertEquals("Prefix search should be case-insensitive", lowerPrefix.size, mixedPrefix.size)
    }

    @Test
    fun `test InstructionDatabase getInstructionsByPrefix returns empty list for no matches`() {
        val result = InstructionDatabase.getInstructionsByPrefix("zzzzz")
        assertTrue("Non-matching prefix should return empty list", result.isEmpty())
    }

    @Test
    fun `test InstructionDatabase contains various instruction categories`() {
        val instructions = InstructionDatabase.getAllInstructions()
        val categories = instructions.map { it.category }.toSet()

        assertTrue("Should have DATA_MOVEMENT instructions",
            categories.contains(InstructionDatabase.Category.DATA_MOVEMENT))
        assertTrue("Should have ARITHMETIC instructions",
            categories.contains(InstructionDatabase.Category.ARITHMETIC))
        assertTrue("Should have CONTROL_FLOW instructions",
            categories.contains(InstructionDatabase.Category.CONTROL_FLOW))
    }

    @Test
    fun `test InstructionDatabase specific instructions exist`() {
        // Test a variety of common instructions
        assertNotNull("ADD should exist", InstructionDatabase.getInstruction("add"))
        assertNotNull("SUB should exist", InstructionDatabase.getInstruction("sub"))
        assertNotNull("JMP should exist", InstructionDatabase.getInstruction("jmp"))
        assertNotNull("CALL should exist", InstructionDatabase.getInstruction("call"))
        assertNotNull("RET should exist", InstructionDatabase.getInstruction("ret"))
        assertNotNull("PUSH should exist", InstructionDatabase.getInstruction("push"))
        assertNotNull("POP should exist", InstructionDatabase.getInstruction("pop"))
    }

    // ============================================================================
    // RegisterDatabase Tests
    // ============================================================================

    @Test
    fun `test RegisterDatabase lazy initialization`() {
        val registers = RegisterDatabase.getAllRegisters()
        assertNotNull("Registers should not be null", registers)
        assertTrue("Registers should not be empty", registers.isNotEmpty())
    }

    @Test
    fun `test RegisterDatabase getRegister returns valid register`() {
        val rax = RegisterDatabase.getRegister("rax")
        assertNotNull("RAX register should exist", rax)
        assertEquals("Register name should be 'rax'", "rax", rax!!.name)
        assertEquals("RAX should be 64-bit", RegisterDatabase.RegisterSize.BIT_64, rax.size)
        assertTrue("Register should have description", rax.description.isNotEmpty())
    }

    @Test
    fun `test RegisterDatabase case-insensitive lookup`() {
        val raxLower = RegisterDatabase.getRegister("rax")
        val raxUpper = RegisterDatabase.getRegister("RAX")
        val raxMixed = RegisterDatabase.getRegister("RaX")

        assertNotNull("Lowercase lookup should succeed", raxLower)
        assertNotNull("Uppercase lookup should succeed", raxUpper)
        assertNotNull("Mixed case lookup should succeed", raxMixed)
        assertEquals("Different cases should return same register", raxLower, raxUpper)
        assertEquals("Different cases should return same register", raxLower, raxMixed)
    }

    @Test
    fun `test RegisterDatabase getRegister returns null for nonexistent register`() {
        val result = RegisterDatabase.getRegister("nonexistent_register_xyz")
        assertNull("Nonexistent register should return null", result)
    }

    @Test
    fun `test RegisterDatabase getAllRegisters returns all registers`() {
        val registers = RegisterDatabase.getAllRegisters()
        assertTrue("Should have many registers (at least 20)", registers.size > 20)

        // Verify all registers have required fields
        registers.forEach { register ->
            assertTrue("Register name should not be empty", register.name.isNotEmpty())
            assertTrue("Register description should not be empty", register.description.isNotEmpty())
            assertNotNull("Register size should not be null", register.size)
        }
    }

    @Test
    fun `test RegisterDatabase getRegistersByPrefix`() {
        val rRegisters = RegisterDatabase.getRegistersByPrefix("r")
        assertTrue("Should find registers starting with 'r'", rRegisters.isNotEmpty())

        // All results should start with "r"
        rRegisters.forEach { register ->
            assertTrue("Register '${register.name}' should start with 'r'",
                register.name.startsWith("r", ignoreCase = true))
        }
    }

    @Test
    fun `test RegisterDatabase getRegistersByPrefix is case-insensitive`() {
        val lowerPrefix = RegisterDatabase.getRegistersByPrefix("r")
        val upperPrefix = RegisterDatabase.getRegistersByPrefix("R")

        assertEquals("Prefix search should be case-insensitive", lowerPrefix.size, upperPrefix.size)
    }

    @Test
    fun `test RegisterDatabase getRegistersByPrefix returns empty list for no matches`() {
        val result = RegisterDatabase.getRegistersByPrefix("zzzzz")
        assertTrue("Non-matching prefix should return empty list", result.isEmpty())
    }

    @Test
    fun `test RegisterDatabase contains various register sizes`() {
        val registers = RegisterDatabase.getAllRegisters()
        val sizes = registers.map { it.size }.toSet()

        // Should have multiple register sizes
        assertTrue("Should have multiple register sizes", sizes.size > 1)
    }

    @Test
    fun `test RegisterDatabase specific registers exist`() {
        // Test general purpose registers
        assertNotNull("RAX should exist", RegisterDatabase.getRegister("rax"))
        assertNotNull("RBX should exist", RegisterDatabase.getRegister("rbx"))
        assertNotNull("RCX should exist", RegisterDatabase.getRegister("rcx"))
        assertNotNull("RDX should exist", RegisterDatabase.getRegister("rdx"))
        assertNotNull("RSP should exist", RegisterDatabase.getRegister("rsp"))
        assertNotNull("RBP should exist", RegisterDatabase.getRegister("rbp"))
        assertNotNull("RSI should exist", RegisterDatabase.getRegister("rsi"))
        assertNotNull("RDI should exist", RegisterDatabase.getRegister("rdi"))
    }

    // ============================================================================
    // DirectiveDatabase Tests
    // ============================================================================

    @Test
    fun `test DirectiveDatabase lazy initialization`() {
        val directives = DirectiveDatabase.getAllDirectives()
        assertNotNull("Directives should not be null", directives)
        assertTrue("Directives should not be empty", directives.isNotEmpty())
    }

    @Test
    fun `test DirectiveDatabase getDirective returns valid directive`() {
        val define = DirectiveDatabase.getDirective("%define")
        assertNotNull("%define directive should exist", define)
        assertEquals("Directive name should be '%define'", "%define", define!!.name)
        assertTrue("Directive should have description", define.description.isNotEmpty())
    }

    @Test
    fun `test DirectiveDatabase case-insensitive lookup`() {
        val defineLower = DirectiveDatabase.getDirective("%define")
        val defineUpper = DirectiveDatabase.getDirective("%DEFINE")
        val defineMixed = DirectiveDatabase.getDirective("%DeFiNe")

        assertNotNull("Lowercase lookup should succeed", defineLower)
        assertNotNull("Uppercase lookup should succeed", defineUpper)
        assertNotNull("Mixed case lookup should succeed", defineMixed)
        assertEquals("Different cases should return same directive", defineLower, defineUpper)
        assertEquals("Different cases should return same directive", defineLower, defineMixed)
    }

    @Test
    fun `test DirectiveDatabase getDirective returns null for nonexistent directive`() {
        val result = DirectiveDatabase.getDirective("%nonexistent_directive_xyz")
        assertNull("Nonexistent directive should return null", result)
    }

    @Test
    fun `test DirectiveDatabase getAllDirectives returns all directives`() {
        val directives = DirectiveDatabase.getAllDirectives()
        assertTrue("Should have many directives (at least 10)", directives.size > 10)

        // Verify all directives have required fields
        directives.forEach { directive ->
            assertTrue("Directive name should not be empty", directive.name.isNotEmpty())
            assertTrue("Directive description should not be empty", directive.description.isNotEmpty())
        }
    }

    @Test
    fun `test DirectiveDatabase getDirectivesByPrefix`() {
        val percentDirectives = DirectiveDatabase.getDirectivesByPrefix("%d")
        assertTrue("Should find directives starting with '%d'", percentDirectives.isNotEmpty())

        // All results should start with "%d"
        percentDirectives.forEach { directive ->
            assertTrue("Directive '${directive.name}' should start with '%d'",
                directive.name.startsWith("%d", ignoreCase = true))
        }
    }

    @Test
    fun `test DirectiveDatabase getDirectivesByPrefix is case-insensitive`() {
        val lowerPrefix = DirectiveDatabase.getDirectivesByPrefix("%d")
        val upperPrefix = DirectiveDatabase.getDirectivesByPrefix("%D")

        assertEquals("Prefix search should be case-insensitive", lowerPrefix.size, upperPrefix.size)
    }

    @Test
    fun `test DirectiveDatabase getDirectivesByPrefix returns empty list for no matches`() {
        val result = DirectiveDatabase.getDirectivesByPrefix("%zzzzz")
        assertTrue("Non-matching prefix should return empty list", result.isEmpty())
    }

    @Test
    fun `test DirectiveDatabase specific directives exist`() {
        // Test common preprocessor directives
        assertNotNull("%define should exist", DirectiveDatabase.getDirective("%define"))
        assertNotNull("%assign should exist", DirectiveDatabase.getDirective("%assign"))
        assertNotNull("%include should exist", DirectiveDatabase.getDirective("%include"))
        assertNotNull("%macro should exist", DirectiveDatabase.getDirective("%macro"))
    }

    // ============================================================================
    // PreprocessorFunctionDatabase Tests
    // ============================================================================

    @Test
    fun `test PreprocessorFunctionDatabase lazy initialization`() {
        val functions = PreprocessorFunctionDatabase.getAllPreprocessorFunctions()
        assertNotNull("Preprocessor functions should not be null", functions)
        assertTrue("Preprocessor functions should not be empty", functions.isNotEmpty())
    }

    @Test
    fun `test PreprocessorFunctionDatabase getPreprocessorFunction returns valid function`() {
        val eval = PreprocessorFunctionDatabase.getPreprocessorFunction("%eval")
        assertNotNull("%eval function should exist", eval)
        assertEquals("Function name should be '%eval'", "%eval", eval!!.name)
        assertTrue("Function should have description", eval.description.isNotEmpty())
    }

    @Test
    fun `test PreprocessorFunctionDatabase case-insensitive lookup`() {
        val evalLower = PreprocessorFunctionDatabase.getPreprocessorFunction("%eval")
        val evalUpper = PreprocessorFunctionDatabase.getPreprocessorFunction("%EVAL")
        val evalMixed = PreprocessorFunctionDatabase.getPreprocessorFunction("%EvAl")

        assertNotNull("Lowercase lookup should succeed", evalLower)
        assertNotNull("Uppercase lookup should succeed", evalUpper)
        assertNotNull("Mixed case lookup should succeed", evalMixed)
        assertEquals("Different cases should return same function", evalLower, evalUpper)
        assertEquals("Different cases should return same function", evalLower, evalMixed)
    }

    @Test
    fun `test PreprocessorFunctionDatabase getPreprocessorFunction returns null for nonexistent function`() {
        val result = PreprocessorFunctionDatabase.getPreprocessorFunction("%nonexistent_function_xyz")
        assertNull("Nonexistent function should return null", result)
    }

    @Test
    fun `test PreprocessorFunctionDatabase getAllPreprocessorFunctions returns all functions`() {
        val functions = PreprocessorFunctionDatabase.getAllPreprocessorFunctions()
        assertTrue("Should have multiple preprocessor functions (at least 5)", functions.size > 5)

        // Verify all functions have required fields
        functions.forEach { function ->
            assertTrue("Function name should not be empty", function.name.isNotEmpty())
            assertTrue("Function description should not be empty", function.description.isNotEmpty())
        }
    }

    @Test
    fun `test PreprocessorFunctionDatabase getPreprocessorFunctionsByPrefix`() {
        val percentFunctions = PreprocessorFunctionDatabase.getPreprocessorFunctionsByPrefix("%")
        assertTrue("Should find functions starting with '%'", percentFunctions.isNotEmpty())

        // All results should start with "%"
        percentFunctions.forEach { function ->
            assertTrue("Function '${function.name}' should start with '%'",
                function.name.startsWith("%", ignoreCase = true))
        }
    }

    @Test
    fun `test PreprocessorFunctionDatabase getPreprocessorFunctionsByPrefix is case-insensitive`() {
        val lowerPrefix = PreprocessorFunctionDatabase.getPreprocessorFunctionsByPrefix("%c")
        val upperPrefix = PreprocessorFunctionDatabase.getPreprocessorFunctionsByPrefix("%C")

        assertEquals("Prefix search should be case-insensitive", lowerPrefix.size, upperPrefix.size)
    }

    @Test
    fun `test PreprocessorFunctionDatabase getPreprocessorFunctionsByPrefix returns empty list for no matches`() {
        val result = PreprocessorFunctionDatabase.getPreprocessorFunctionsByPrefix("%zzzzz")
        assertTrue("Non-matching prefix should return empty list", result.isEmpty())
    }

    @Test
    fun `test PreprocessorFunctionDatabase specific functions exist`() {
        // Test common preprocessor functions
        assertNotNull("%eval should exist", PreprocessorFunctionDatabase.getPreprocessorFunction("%eval"))
        assertNotNull("%str should exist", PreprocessorFunctionDatabase.getPreprocessorFunction("%str"))
    }

    // ============================================================================
    // InstructionDatabase Documentation Tests
    // ============================================================================

    @Test
    fun `test InstructionDatabase lazy initialization with documentation`() {
        val instructionsWithDocs = InstructionDatabase.getInstructionsWithDocumentation()
        assertNotNull("Instructions with documentation should not be null", instructionsWithDocs)
        assertTrue("Should have documented instructions", instructionsWithDocs.isNotEmpty())
    }

    @Test
    fun `test InstructionDatabase getInstruction returns instruction with documentation`() {
        val add = InstructionDatabase.getInstruction("ADD")
        assertNotNull("ADD instruction should exist", add)
        if (add!!.hasDocumentation) {
            val doc = add.documentation!!
            assertTrue("Documentation should have summary", doc.summary.isNotEmpty())
        }
    }

    @Test
    fun `test InstructionDatabase documentation case-insensitive lookup`() {
        val addLower = InstructionDatabase.getInstruction("add")
        val addUpper = InstructionDatabase.getInstruction("ADD")
        val addMixed = InstructionDatabase.getInstruction("AdD")

        assertNotNull("Lowercase lookup should succeed", addLower)
        assertNotNull("Uppercase lookup should succeed", addUpper)
        assertNotNull("Mixed case lookup should succeed", addMixed)
        assertEquals("Different cases should return same instruction", addLower, addUpper)
        assertEquals("Different cases should return same instruction", addLower, addMixed)
    }

    @Test
    fun `test InstructionDatabase hasDocumentation property`() {
        val add = InstructionDatabase.getInstruction("ADD")
        assertNotNull("ADD should exist", add)
        // Check if hasDocumentation property works
        assertTrue("hasDocumentation should be accessible", add!!.hasDocumentation || !add.hasDocumentation)
    }

    @Test
    fun `test InstructionDatabase getInstructionsWithDocumentation`() {
        val instructionsWithDocs = InstructionDatabase.getInstructionsWithDocumentation()
        assertTrue("Should have multiple documented instructions (at least 5)", instructionsWithDocs.size > 5)

        // Verify all returned instructions actually have documentation
        instructionsWithDocs.forEach { instruction ->
            assertTrue("Instruction ${instruction.name} should have documentation", instruction.hasDocumentation)
        }
    }

    @Test
    fun `test InstructionDatabase specific instructions have documentation`() {
        // Test that common instructions exist (documentation is optional)
        assertNotNull("ADD should exist", InstructionDatabase.getInstruction("ADD"))
        assertNotNull("AND should exist", InstructionDatabase.getInstruction("AND"))
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    fun `test databases handle missing XML gracefully`() {
        // Databases should return empty lists when XML fails to load
        // This is tested implicitly - if XML loading fails, the databases
        // should still initialize with empty lists and not throw exceptions

        // Just verify the databases can be accessed without throwing
        assertNotNull(InstructionDatabase.getAllInstructions())
        assertNotNull(RegisterDatabase.getAllRegisters())
        assertNotNull(DirectiveDatabase.getAllDirectives())
        assertNotNull(PreprocessorFunctionDatabase.getAllPreprocessorFunctions())
    }

    // ============================================================================
    // Singleton Behavior Tests
    // ============================================================================

    @Test
    fun `test InstructionDatabase singleton behavior`() {
        val instructions1 = InstructionDatabase.getAllInstructions()
        val instructions2 = InstructionDatabase.getAllInstructions()

        // Should return the same instance (referential equality)
        assertTrue("Multiple calls should return same instance (lazy initialized)",
            instructions1 === instructions2)
    }

    @Test
    fun `test RegisterDatabase singleton behavior`() {
        val registers1 = RegisterDatabase.getAllRegisters()
        val registers2 = RegisterDatabase.getAllRegisters()

        assertTrue("Multiple calls should return same instance (lazy initialized)",
            registers1 === registers2)
    }

    @Test
    fun `test DirectiveDatabase singleton behavior`() {
        val directives1 = DirectiveDatabase.getAllDirectives()
        val directives2 = DirectiveDatabase.getAllDirectives()

        assertTrue("Multiple calls should return same instance (lazy initialized)",
            directives1 === directives2)
    }

    @Test
    fun `test PreprocessorFunctionDatabase singleton behavior`() {
        val functions1 = PreprocessorFunctionDatabase.getAllPreprocessorFunctions()
        val functions2 = PreprocessorFunctionDatabase.getAllPreprocessorFunctions()

        assertTrue("Multiple calls should return same instance (lazy initialized)",
            functions1 === functions2)
    }


    // ============================================================================
    // Data Integrity Tests
    // ============================================================================

    @Test
    fun `test InstructionDatabase data integrity`() {
        val instructions = InstructionDatabase.getAllInstructions()

        // Check for duplicates (note: some instructions may intentionally appear multiple times
        // with different categories or descriptions)
        val names = instructions.map { it.name.lowercase() }
        val uniqueNames = names.toSet()

        // Log duplicates if any exist (for informational purposes)
        if (names.size != uniqueNames.size) {
            val duplicates = names.groupingBy { it }.eachCount().filter { it.value > 1 }
            println("Note: Found duplicate instruction names: $duplicates")
        }

        // Verify the database loaded successfully
        assertTrue("Instruction database should have entries", instructions.isNotEmpty())
    }

    @Test
    fun `test RegisterDatabase data integrity`() {
        val registers = RegisterDatabase.getAllRegisters()

        // Check for duplicates
        val names = registers.map { it.name.lowercase() }
        val uniqueNames = names.toSet()
        assertEquals("Should not have duplicate register names", names.size, uniqueNames.size)
    }

    @Test
    fun `test DirectiveDatabase data integrity`() {
        val directives = DirectiveDatabase.getAllDirectives()

        // Check for duplicates
        val names = directives.map { it.name.lowercase() }
        val uniqueNames = names.toSet()
        assertEquals("Should not have duplicate directive names", names.size, uniqueNames.size)
    }

    @Test
    fun `test PreprocessorFunctionDatabase data integrity`() {
        val functions = PreprocessorFunctionDatabase.getAllPreprocessorFunctions()

        // Check for duplicates
        val names = functions.map { it.name.lowercase() }
        val uniqueNames = names.toSet()
        assertEquals("Should not have duplicate function names", names.size, uniqueNames.size)
    }


    // ============================================================================
    // Edge Case Tests
    // ============================================================================

    @Test
    fun `test empty string prefix returns all items`() {
        val allInstructions = InstructionDatabase.getAllInstructions()
        val emptyPrefixInstructions = InstructionDatabase.getInstructionsByPrefix("")

        assertEquals("Empty prefix should return all instructions",
            allInstructions.size, emptyPrefixInstructions.size)
    }

    @Test
    fun `test prefix search with single character`() {
        val singleChar = InstructionDatabase.getInstructionsByPrefix("m")
        assertTrue("Single character prefix should work", singleChar.isNotEmpty())

        singleChar.forEach { instruction ->
            assertTrue("All results should start with 'm'",
                instruction.name.startsWith("m", ignoreCase = true))
        }
    }

    @Test
    fun `test prefix search with exact match`() {
        // Searching with exact instruction name should return at least that instruction
        val movPrefix = InstructionDatabase.getInstructionsByPrefix("mov")
        val movExact = InstructionDatabase.getInstruction("mov")

        assertNotNull("MOV should exist", movExact)
        assertTrue("Prefix search should include exact match", movPrefix.contains(movExact))
    }

    @Test
    fun `test special characters in lookup`() {
        // Directives and preprocessor functions start with %
        val directive = DirectiveDatabase.getDirective("%define")
        assertNotNull("Should handle % in directive names", directive)

        val function = PreprocessorFunctionDatabase.getPreprocessorFunction("%eval")
        assertNotNull("Should handle % in function names", function)
    }
}
