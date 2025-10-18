package dev.agb.nasmplugin.references

import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.util.PsiTreeUtil
import dev.agb.nasmplugin.psi.NasmExternDir
import dev.agb.nasmplugin.psi.NasmSymbolDecl

/**
 * Tests that find usages works correctly for extern declarations.
 * This test debugs the issue where "je sigreturn" is not found when doing
 * find usages on "extern sigreturn".
 */
class ExternFindUsagesTest : BasePlatformTestCase() {

    fun testExternFindUsagesInSameFile() {
        // Create a test file that matches your real scenario more closely
        // with both extern and usage in same file
        val file = myFixture.configureByText("syscall.asm", """
            extern handle_syscall
            extern signal_dispatch
            extern sigreturn

            global syscall_handler
            syscall_handler:
                swapgs

                ; handle sigreturn as a special case
                cmp rax, NR_rt_sigreturn
                je sigreturn

                cli
        """.trimIndent())

        // Find the extern declaration for "sigreturn"
        val externDirs = PsiTreeUtil.findChildrenOfType(file, NasmExternDir::class.java)
        println("Found ${externDirs.size} extern directives")

        val allSymbolDecls = externDirs.flatMap { externDir ->
            PsiTreeUtil.findChildrenOfType(externDir, NasmSymbolDecl::class.java)
        }
        println("Found ${allSymbolDecls.size} total symbol declarations in externs")

        val externDecl = allSymbolDecls.firstOrNull { it.name == "sigreturn" }
        assertNotNull("Should find extern symbol declaration for sigreturn", externDecl)
        println("Extern declaration: ${externDecl!!.text} at offset ${externDecl.textOffset}")
        println("Extern declaration name: ${externDecl.name}")
        println("Extern declaration class: ${externDecl.javaClass.name}")

        // Check what the extern declaration resolves to
        val externRef = externDecl.reference
        println("Extern has reference: ${externRef != null}")
        if (externRef != null) {
            val externTarget = externRef.resolve()
            println("Extern resolves to: ${externTarget?.text} (${externTarget?.javaClass?.simpleName})")
        }

        // Now search for all references to this extern declaration
        println("\n=== Searching for usages of extern declaration ===")
        val references = ReferencesSearch.search(externDecl).findAll()
        println("Found ${references.size} references to extern declaration")

        references.forEachIndexed { index, ref ->
            println("Reference $index:")
            println("  Element: ${ref.element.text}")
            println("  Element class: ${ref.element.javaClass.simpleName}")
            println("  Offset: ${ref.element.textOffset}")
            println("  Resolves to: ${ref.resolve()?.text}")
            println("  Resolves to same extern? ${ref.resolve() == externDecl}")
        }

        // Also manually find all symbol references in the file and check what they resolve to
        println("\n=== Checking all identifiers in file ===")
        val allText = file.text
        var searchIndex = 0
        while (true) {
            val index = allText.indexOf("sigreturn", searchIndex)
            if (index == -1) break

            println("Found 'sigreturn' at offset $index")
            val element = file.findElementAt(index)
            println("  Element: ${element?.text} (${element?.javaClass?.simpleName})")
            println("  Parent: ${element?.parent?.text} (${element?.parent?.javaClass?.simpleName})")

            // Try to get reference from element or parent
            val ref = element?.reference ?: element?.parent?.reference
            if (ref != null) {
                println("  Has reference: true")
                val target = ref.resolve()
                println("  Resolves to: ${target?.text} (${target?.javaClass?.simpleName})")
                println("  Resolves to extern decl? ${target == externDecl}")
            } else {
                println("  Has reference: false")
            }

            searchIndex = index + 1
        }

        // The test assertion - we expect at least 1 usage (the "je sigreturn" line)
        assertTrue("Should find at least 1 reference to extern declaration", references.size >= 1)
    }

    fun testExternWithGlobalLabelInAnotherFile() {
        // This tests the scenario where there's a global label in another file
        // The extern should still be the target for references in the current file

        // Create first file with global label definition
        myFixture.configureByText("sigtramp.asm", """
            global sigreturn
            sigreturn:
                ; implementation
                ret
        """.trimIndent())

        // Create second file with extern and usage
        val syscallFile = myFixture.configureByText("syscall.asm", """
            extern sigreturn

            global syscall_handler
            syscall_handler:
                je sigreturn
                call sigreturn
        """.trimIndent())

        // Find the extern declaration in syscall.asm
        val externDirs = PsiTreeUtil.findChildrenOfType(syscallFile, NasmExternDir::class.java)
        val externDecl = PsiTreeUtil.findChildrenOfType(externDirs.first(), NasmSymbolDecl::class.java).first()

        println("\n=== Cross-file test ===")
        println("Extern declaration: ${externDecl.text} in ${externDecl.containingFile.name}")

        // Check what the "je sigreturn" reference resolves to
        val allText = syscallFile.text
        val jeIndex = allText.indexOf("je sigreturn")
        val sigreIndex = allText.indexOf("sigreturn", jeIndex)
        val element = syscallFile.findElementAt(sigreIndex)
        val ref = element?.parent?.reference

        println("je sigreturn element: ${element?.parent?.text} (${element?.parent?.javaClass?.simpleName})")
        if (ref != null) {
            val target = ref.resolve()
            println("Resolves to: ${target?.text} in ${target?.containingFile?.name} (${target?.javaClass?.simpleName})")
            println("Resolves to extern decl? ${target == externDecl}")
        }

        // Search for usages of extern decl - this should now include je and call!
        val references = ReferencesSearch.search(externDecl).findAll()
        println("Found ${references.size} references to extern declaration")
        references.forEach { r ->
            println("  - ${r.element.text} at offset ${r.element.textOffset} in ${r.element.containingFile.name}")
        }

        // The je sigreturn should resolve to the extern decl (not the global label)
        assertEquals("je sigreturn should resolve to extern decl", externDecl, ref?.resolve())

        // Should find 2 references: je sigreturn and call sigreturn
        assertEquals("Should find 2 references (je and call)", 2, references.size)
    }

    fun testFindUsagesActionOnExtern() {
        // This test simulates the actual "Find Usages" UI action
        // rather than programmatically calling ReferencesSearch.search()

        // Create first file with global label definition
        myFixture.configureByText("sigtramp.asm", """
            global my_function
            my_function:
                ret
        """.trimIndent())

        // Create second file with extern and usages
        myFixture.configureByText("test.asm", """
            extern my_function

            start:
                call my_function
                je my_function
        """.trimIndent())

        // Position the caret on "my_function" in the extern declaration (line 0, after "extern ")
        val externOffset = myFixture.file.text.indexOf("my_function")
        myFixture.editor.caretModel.moveToOffset(externOffset + 5) // middle of "my_function"

        // Use the fixture's find usages helper
        val usages = myFixture.testFindUsages("test.asm")

        println("\n=== Find Usages Action Test ===")
        println("Found ${usages.size} usages via UI action")

        val usagesByFile = usages.groupBy { it.element?.containingFile?.name }
        usagesByFile.forEach { (file, fileUsages) ->
            println("In $file:")
            fileUsages.forEach { usage ->
                val element = usage.element
                val offset = element?.textOffset ?: -1
                val text = element?.text ?: "null"
                val lineContext = element?.context?.text?.take(50) ?: "no context"
                println("  - '$text' at offset $offset, context: $lineContext")
            }
        }

        // Check that we found usages in test.asm (the file with the extern)
        val testAsmUsages = usages.filter { it.element?.containingFile?.name == "test.asm" }
        println("\nUsages in test.asm: ${testAsmUsages.size}")

        // We should find: extern declaration, call my_function, je my_function = 3 total
        // Or at minimum: call and je = 2 in test.asm
        assertTrue(
            "Should find at least call and je in test.asm (found ${testAsmUsages.size})",
            testAsmUsages.size >= 2
        )

        // Check that we actually found the call and je, not just the extern twice
        val callOffset = myFixture.file.text.indexOf("call my_function") + 5  // offset of "my_function" in call
        val jeOffset = myFixture.file.text.indexOf("je my_function") + 3      // offset of "my_function" in je

        val hasCall = testAsmUsages.any {
            val offset = it.element?.textOffset ?: -1
            offset >= callOffset && offset < callOffset + 15
        }
        val hasJe = testAsmUsages.any {
            val offset = it.element?.textOffset ?: -1
            offset >= jeOffset && offset < jeOffset + 15
        }

        println("Found call usage: $hasCall")
        println("Found je usage: $hasJe")

        assertTrue("Should find 'call my_function' usage", hasCall)
        assertTrue("Should find 'je my_function' usage", hasJe)
    }
}
