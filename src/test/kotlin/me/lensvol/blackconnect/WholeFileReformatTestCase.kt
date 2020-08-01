package me.lensvol.blackconnect

import junit.framework.TestCase
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.junit.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class WholeFileReformatTestCase : BlackConnectTestCase() {
    @Test
    fun test_whole_file_is_reformatted_via_menu_action() {
        mockServer.apply {
            this.`when`(
                request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                response()
                    .withStatusCode(200)
                    .withBody("print(\"123\")")
            )
        }

        val unformattedFile = myFixture.copyFileToProject("unformatted.py")
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
        myFixture.openFileInEditor(unformattedFile)
        val event = eventForFile(unformattedFile)

        ReformatWholeFileAction().update(event)

        TestCase.assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun test_error_message_is_displayed_on_syntax_error() {
        val unformattedFile = myFixture.copyFileToProject("broken.py")
        val event = eventForFile(unformattedFile)

        myFixture.openFileInEditor(unformattedFile)
        mockServer.apply {
            this.`when`(
                request()
                    .withMethod("POST")
                    .withPath("/")
            ).respond(
                response()
                    .withStatusCode(400)
                    .withBody("Syntax error")
            )
        }

        checkNotificationIsShown("Source code contained syntax errors.") {
            val actionUnderTest = ReformatWholeFileAction()
            actionUnderTest.beforeActionPerformedUpdate(event)
            actionUnderTest.actionPerformed(event)
        }
    }
}
