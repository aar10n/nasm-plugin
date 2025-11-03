package dev.agb.nasmplugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for NASM project settings functionality.
 * Tests settings persistence, modification tracking, and command-line macros.
 */
class NasmSettingsTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        // Clear settings before each test
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.clear()
        settings.commandLineMacros = ""
        settings.notifyChanged()
    }

    fun testSettingsServiceIsAvailable() {
        val settings = NasmProjectSettings.getInstance(project)
        assertNotNull("Settings service should be available", settings)
    }

    fun testDefaultIncludePathsAreEmpty() {
        val settings = NasmProjectSettings.getInstance(project)
        assertTrue("Include paths should be empty by default", settings.includePaths.isEmpty())
    }

    fun testAddIncludePath() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("include")

        assertEquals("Should have 1 include path", 1, settings.includePaths.size)
        assertEquals("Should contain 'include'", "include", settings.includePaths[0])
    }

    fun testAddMultipleIncludePaths() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("include")
        settings.includePaths.add("lib/headers")
        settings.includePaths.add("/usr/local/include/nasm")

        assertEquals("Should have 3 include paths", 3, settings.includePaths.size)
        assertTrue("Should contain 'include'", settings.includePaths.contains("include"))
        assertTrue("Should contain 'lib/headers'", settings.includePaths.contains("lib/headers"))
        assertTrue("Should contain '/usr/local/include/nasm'",
            settings.includePaths.contains("/usr/local/include/nasm"))
    }

    fun testRemoveIncludePath() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("include")
        settings.includePaths.add("lib")

        settings.includePaths.remove("include")

        assertEquals("Should have 1 include path", 1, settings.includePaths.size)
        assertEquals("Should only contain 'lib'", "lib", settings.includePaths[0])
    }

    fun testClearIncludePaths() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("include")
        settings.includePaths.add("lib")

        settings.includePaths.clear()

        assertTrue("Include paths should be empty", settings.includePaths.isEmpty())
    }

    fun testDefaultCommandLineMacrosAreEmpty() {
        val settings = NasmProjectSettings.getInstance(project)
        assertEquals("Command-line macros should be empty by default", "", settings.commandLineMacros)
    }

    fun testSetCommandLineMacrosSingleMacro() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG"

        assertEquals("Should have DEBUG macro", "DEBUG", settings.commandLineMacros)
    }

    fun testSetCommandLineMacrosWithValue() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "VERSION=2"

        assertEquals("Should have VERSION=2 macro", "VERSION=2", settings.commandLineMacros)
    }

    fun testSetCommandLineMacrosMultiple() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG,VERSION=2,OS=LINUX"

        assertEquals("Should have all macros", "DEBUG,VERSION=2,OS=LINUX", settings.commandLineMacros)
    }

    fun testModificationCounterIncrementsOnMacroChange() {
        val settings = NasmProjectSettings.getInstance(project)
        val initialCount = settings.modificationCount

        settings.commandLineMacros = "DEBUG"

        val newCount = settings.modificationCount
        assertTrue("Modification count should increment",
            newCount > initialCount)
    }

    fun testModificationCounterIncrementsOnNotifyChanged() {
        val settings = NasmProjectSettings.getInstance(project)
        val initialCount = settings.modificationCount

        settings.includePaths.add("include")
        settings.notifyChanged()

        val newCount = settings.modificationCount
        assertTrue("Modification count should increment after notifyChanged",
            newCount > initialCount)
    }

    fun testMultipleModificationsIncrementCounter() {
        val settings = NasmProjectSettings.getInstance(project)
        val initialCount = settings.modificationCount

        settings.commandLineMacros = "DEBUG"
        val count1 = settings.modificationCount

        settings.commandLineMacros = "VERSION=2"
        val count2 = settings.modificationCount

        assertTrue("First modification should increment", count1 > initialCount)
        assertTrue("Second modification should increment", count2 > count1)
    }

    fun testSettingsStatePersistence() {
        val settings = NasmProjectSettings.getInstance(project)

        // Configure settings
        settings.includePaths.add("include")
        settings.includePaths.add("lib")
        settings.commandLineMacros = "DEBUG,VERSION=2"

        // Get state
        val state = settings.state
        assertNotNull("State should not be null", state)
        assertSame("State should be the settings object itself", settings, state)

        // Verify state contains the data
        assertEquals("State should have include paths", 2, state?.includePaths?.size)
        assertEquals("State should have macros", "DEBUG,VERSION=2", state?.commandLineMacros)
    }

    fun testSettingsLoadState() {
        val settings = NasmProjectSettings.getInstance(project)

        // Create a state object
        val newState = NasmProjectSettings()
        newState.includePaths.add("custom/include")
        newState.commandLineMacros = "CUSTOM_MACRO"

        // Load the state
        settings.loadState(newState)

        // Verify settings were loaded
        assertEquals("Should have 1 include path", 1, settings.includePaths.size)
        assertEquals("Should have custom/include", "custom/include", settings.includePaths[0])
        assertEquals("Should have CUSTOM_MACRO", "CUSTOM_MACRO", settings.commandLineMacros)
    }

    fun testLoadStateIncrementsModificationCounter() {
        val settings = NasmProjectSettings.getInstance(project)
        val initialCount = settings.modificationCount

        val newState = NasmProjectSettings()
        newState.includePaths.add("test")

        settings.loadState(newState)

        assertTrue("Load state should increment modification count",
            settings.modificationCount > initialCount)
    }

    fun testIncludePathsAreMutable() {
        val settings = NasmProjectSettings.getInstance(project)

        // Add paths
        settings.includePaths.add("path1")
        settings.includePaths.add("path2")

        // Modify in place
        settings.includePaths[0] = "modified_path"

        assertEquals("Should have modified path", "modified_path", settings.includePaths[0])
        assertEquals("Second path should be unchanged", "path2", settings.includePaths[1])
    }

    fun testSettingsAreProjectScoped() {
        val settings1 = NasmProjectSettings.getInstance(project)
        val settings2 = NasmProjectSettings.getInstance(project)

        assertSame("Should return same instance for same project", settings1, settings2)
    }

    fun testEmptyCommandLineMacrosString() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = ""

        assertEquals("Empty string should be allowed", "", settings.commandLineMacros)
    }

    fun testWhitespaceInCommandLineMacros() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.commandLineMacros = "DEBUG, VERSION=2, OS=LINUX"

        assertEquals("Whitespace should be preserved",
            "DEBUG, VERSION=2, OS=LINUX", settings.commandLineMacros)
    }

    fun testSpecialCharactersInIncludePaths() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("path with spaces")
        settings.includePaths.add("path-with-dashes")
        settings.includePaths.add("path_with_underscores")
        settings.includePaths.add("path.with.dots")

        assertEquals("Should have 4 paths", 4, settings.includePaths.size)
        assertTrue("Should contain path with spaces",
            settings.includePaths.contains("path with spaces"))
        assertTrue("Should contain path with dashes",
            settings.includePaths.contains("path-with-dashes"))
        assertTrue("Should contain path with underscores",
            settings.includePaths.contains("path_with_underscores"))
        assertTrue("Should contain path with dots",
            settings.includePaths.contains("path.with.dots"))
    }

    fun testComplexMacroDefinitions() {
        val settings = NasmProjectSettings.getInstance(project)

        // Test various macro formats
        settings.commandLineMacros = "SIMPLE,WITH_VALUE=123,WITH_STRING=\"hello\",EXPR=2+2"

        assertEquals("Should store complex macro string",
            "SIMPLE,WITH_VALUE=123,WITH_STRING=\"hello\",EXPR=2+2",
            settings.commandLineMacros)
    }

    fun testIncludePathOrderIsPreserved() {
        val settings = NasmProjectSettings.getInstance(project)
        settings.includePaths.add("first")
        settings.includePaths.add("second")
        settings.includePaths.add("third")

        assertEquals("First path should be 'first'", "first", settings.includePaths[0])
        assertEquals("Second path should be 'second'", "second", settings.includePaths[1])
        assertEquals("Third path should be 'third'", "third", settings.includePaths[2])
    }

    fun testNotifyChangedCanBeCalledMultipleTimes() {
        val settings = NasmProjectSettings.getInstance(project)
        val initialCount = settings.modificationCount

        settings.notifyChanged()
        val count1 = settings.modificationCount

        settings.notifyChanged()
        val count2 = settings.modificationCount

        settings.notifyChanged()
        val count3 = settings.modificationCount

        assertTrue("Each notifyChanged should increment", count1 > initialCount)
        assertTrue("Each notifyChanged should increment", count2 > count1)
        assertTrue("Each notifyChanged should increment", count3 > count2)
    }
}
