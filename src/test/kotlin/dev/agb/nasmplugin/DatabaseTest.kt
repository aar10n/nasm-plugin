package dev.agb.nasmplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.agb.nasmplugin.database.InstructionDatabase

/**
 * Test for the instruction database loader.
 * This test verifies that the database loads correctly and contains expected data.
 */
class DatabaseTest : BasePlatformTestCase() {

    fun testDatabaseLoads() {
        val instructions = InstructionDatabase.getAllInstructions()
        assertTrue("Database should contain instructions", instructions.isNotEmpty())
        println("Loaded ${instructions.size} instructions from database")
    }

    fun testInstructionWithVariants() {
        val mov = InstructionDatabase.getInstruction("mov")
        assertNotNull("MOV instruction should exist", mov)
        assertTrue("MOV should have variants", mov!!.variants.isNotEmpty())
        println("MOV has ${mov.variants.size} variants")
    }

    fun testInstructionWithDocumentation() {
        val add = InstructionDatabase.getInstruction("add")
        assertNotNull("ADD instruction should exist", add)
        assertTrue("ADD should have documentation", add!!.hasDocumentation)
        assertNotNull("ADD documentation should not be null", add.documentation)
        println("ADD documentation:")
        println("  Summary: ${add.documentation!!.summary}")
        println("  Operation: ${add.documentation.operation}")
        println("  Flags: ${add.documentation.flagsAffected}")
    }

    fun testInstructionsByPrefix() {
        val cmovInstructions = InstructionDatabase.getInstructionsByPrefix("cmov")
        assertTrue("Should find CMOV instructions", cmovInstructions.isNotEmpty())
        println("Found ${cmovInstructions.size} instructions starting with 'cmov'")
    }

    fun testInstructionsWithDocs() {
        val withDocs = InstructionDatabase.getInstructionsWithDocumentation()
        assertTrue("Should have instructions with documentation", withDocs.isNotEmpty())
        println("${withDocs.size} instructions have detailed documentation")
    }

    fun testSpecificInstructions() {
        // Test a few specific instructions to verify correct loading
        val testCases = listOf("mov", "add", "sub", "jmp", "call", "ret", "push", "pop")

        for (name in testCases) {
            val inst = InstructionDatabase.getInstruction(name)
            assertNotNull("$name should exist", inst)
            println("$name: ${inst!!.description} (${inst.variants.size} variants, has docs: ${inst.hasDocumentation})")
        }
    }
}
