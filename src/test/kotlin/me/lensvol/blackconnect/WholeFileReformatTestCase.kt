package me.lensvol.blackconnect

import com.intellij.testFramework.TestActionEvent
import junit.framework.TestCase
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.junit.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class WholeFileReformatTestCase : BlackConnectTestCase() {
    @Test
    fun test_whole_file_is_reformatted_via_menu_action() {
        val unformattedFile = myFixture.copyFileToProject("unformatted.py")
        setupBlackdResponse(200, "print(\"123\")")
        myFixture.openFileInEditor(unformattedFile)

        val codeUnderTest = {
            val event = eventForFile(unformattedFile)

            val actionUnderTest = ReformatWholeFileAction()
            actionUnderTest.beforeActionPerformedUpdate(event)
            actionUnderTest.actionPerformed(event)
        }

        checkWhenCommandCompletes("Reformat code using blackd", codeUnderTest) {
            myFixture.checkResultByFile("reformatted.py")
        }
    }

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
        val event = eventForFile(unformattedFile)
        myFixture.openFileInEditor(unformattedFile)

        ReformatWholeFileAction().update(event)

        TestCase.assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun test_error_message_is_displayed_on_syntax_error() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        val event = eventForFile(unformattedFile)
        myFixture.openFileInEditor(unformattedFile)
        setupBlackdResponse(400, "Error")

        checkNotificationIsShown("Source code contained syntax errors.", runActionForEvent(event))
    }

    @Test
    fun test_error_message_is_displayed_on_blackd_internal_error() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        val event = eventForFile(unformattedFile)
        setupBlackdResponse(500, "SNAFU")
        myFixture.openFileInEditor(unformattedFile)

        checkNotificationIsShown("Internal error, please see blackd output.", runActionForEvent(event))
    }

    @Test
    fun test_error_message_is_displayed_on_unknown_status_from_blackd() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        val event = eventForFile(unformattedFile)
        setupBlackdResponse(417, "I'm A Teapot!")
        myFixture.openFileInEditor(unformattedFile)

        val expectedErrorMessage = "Failed to connect to <b>blackd</b>:" +
            "<br>Server returned HTTP response code: 417 for URL: " +
            "http://localhost:${mockServer.port}"

        checkNotificationIsShown(expectedErrorMessage, runActionForEvent(event))
    }

    private fun runActionForEvent(event: TestActionEvent): () -> Unit {
        return {
            val actionUnderTest = ReformatWholeFileAction()
            actionUnderTest.beforeActionPerformedUpdate(event)
            actionUnderTest.actionPerformed(event)
        }
    }

    private fun setupBlackdResponse(statusCode: Int, body: String) {
        mockServer.apply {
            this.`when`(
                request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                response()
                    .withStatusCode(statusCode)
                    .withBody(body)
            )
        }
    }
}
