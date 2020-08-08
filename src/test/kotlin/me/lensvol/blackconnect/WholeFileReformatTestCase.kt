package me.lensvol.blackconnect

import com.intellij.testFramework.TestActionEvent
import junit.framework.TestCase
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.junit.Test

class WholeFileReformatTestCase : BlackConnectTestCase() {
    @Test
    fun test_menu_item_is_not_active_for_non_python() {
        val unformattedFile = myFixture.copyFileToProject("not_python.txt")
        myFixture.openFileInEditor(unformattedFile)

        val event = eventForFile(unformattedFile)
        ReformatWholeFileAction().update(event)

        TestCase.assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun test_menu_item_is_not_active_for_python() {
        val unformattedFile = myFixture.copyFileToProject("unformatted.py")
        myFixture.openFileInEditor(unformattedFile)

        val event = eventForFile(unformattedFile)
        ReformatWholeFileAction().update(event)

        TestCase.assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun test_error_message_is_displayed_on_syntax_error() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        myFixture.openFileInEditor(unformattedFile)
        setupBlackdResponse(BlackdResponse.SyntaxError("Error"))

        val event = eventForFile(unformattedFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Source code contained syntax errors.")
    }

    @Test
    fun test_error_message_is_displayed_on_blackd_internal_error() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        myFixture.openFileInEditor(unformattedFile)
        setupBlackdResponse(BlackdResponse.InternalError("SNAFU"))

        val event = eventForFile(unformattedFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Internal error, please see blackd output.")
    }

    @Test
    fun test_error_message_is_displayed_on_unknown_status_from_blackd() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        myFixture.openFileInEditor(unformattedFile)
        setupBlackdResponse(BlackdResponse.UnknownStatus(417, "I'm A Teapot!"))

        val event = eventForFile(unformattedFile)
        runActionForEvent(event)

        mockNotificationManager.assertNotificationShown("Something unexpected happened:\nI'm A Teapot!")
    }

    private fun runActionForEvent(event: TestActionEvent) {
        val actionUnderTest = ReformatWholeFileAction()
        actionUnderTest.beforeActionPerformedUpdate(event)
        actionUnderTest.actionPerformed(event)
    }

    private fun setupBlackdResponse(response: BlackdResponse) {
        mockCodeReformatter.setResponse(response)
    }
}
