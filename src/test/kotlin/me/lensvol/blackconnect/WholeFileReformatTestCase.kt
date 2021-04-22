package me.lensvol.blackconnect

import com.intellij.testFramework.TestActionEvent
import junit.framework.TestCase
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.junit.Test

class WholeFileReformatTestCase : BlackConnectTestCase() {
    @Test
    fun test_whole_file_is_reformatted_if_needed() {
        setupBlackdResponse(BlackdResponse.Blackened("print(\"123\")"))
        val unformattedFile = openFileInEditor("unformatted.py")

        val event = eventForFile(unformattedFile)
        runActionForEvent(event)

        myFixture.checkResultByFile("reformatted.py")
    }

    @Test
    fun test_menu_item_is_not_active_for_non_python() {
        val nonPythonFile = openFileInEditor("not_python.txt")

        val event = eventForFile(nonPythonFile)
        ReformatWholeFileAction().update(event)

        TestCase.assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun test_menu_item_is_not_active_for_python() {
        val unformattedFile = openFileInEditor("unformatted.py")

        val event = eventForFile(unformattedFile)
        ReformatWholeFileAction().update(event)

        TestCase.assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun test_error_message_is_displayed_on_syntax_error_if_option_is_set() {
        setupBlackdResponse(BlackdResponse.SyntaxError("Error"))
        val brokenSyntaxFile = openFileInEditor("broken.py")
        pluginConfiguration.showSyntaxErrorMsgs = true

        val event = eventForFile(brokenSyntaxFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Source code contained syntax errors.")
    }

    @Test
    fun test_error_message_is_not_displayed_on_syntax_error_if_option_is_not_set() {
        setupBlackdResponse(BlackdResponse.SyntaxError("Error"))
        val brokenSyntaxFile = openFileInEditor("broken.py")

        pluginConfiguration.showSyntaxErrorMsgs = false
        val event = eventForFile(brokenSyntaxFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationNotShown("Source code contained syntax errors.")
    }

    @Test
    fun test_error_message_is_displayed_on_blackd_internal_error() {
        setupBlackdResponse(BlackdResponse.InternalError("SNAFU"))
        val brokenSyntaxFile = openFileInEditor("broken.py")

        val event = eventForFile(brokenSyntaxFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Internal server error, please see blackd output.")
    }

    @Test
    fun test_error_message_is_displayed_on_unknown_status_from_blackd() {
        setupBlackdResponse(BlackdResponse.UnknownStatus(417, "I'm A Teapot!"))
        val unformattedFile = openFileInEditor("unformatted.py")

        val event = eventForFile(unformattedFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Something unexpected happened:<br>I'm A Teapot!")
    }

    private fun runActionForEvent(event: TestActionEvent) {
        val actionUnderTest = ReformatWholeFileAction()
        actionUnderTest.beforeActionPerformedUpdate(event)
        actionUnderTest.actionPerformed(event)
    }
}
