package me.lensvol.blackconnect

import com.intellij.ide.IdeEventQueue
import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.locks.ReentrantLock

class WholeFileReformatTestCase : BasePlatformTestCase() {
    lateinit var mockServer: ClientAndServer

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

    override fun setUp() {
        super.setUp()
        mockServer = ClientAndServer.startClientAndServer(45484)
    }

    override fun tearDown() {
        mockServer.close()
        super.tearDown()
    }

    private fun eventForFile(file: VirtualFile): TestActionEvent {
        return TestActionEvent(
            DataContext { dataId ->
                when (dataId) {
                    PlatformDataKeys.VIRTUAL_FILE.name -> file
                    PlatformDataKeys.PROJECT.name -> myFixture.project
                    else -> null
                }
            }
        )
    }

    override fun getTestDataPath(): String {
        return System.getProperty("user.dir") + "/src/test/testData"
    }

    private fun checkWhenCommandCompletes(commandName: String, codeBlock: () -> Unit, check: () -> Unit) {
        val lock = ReentrantLock()
        lock.lock()

        val completionPromise = AsyncPromise<Boolean>()

        myFixture.project.messageBus.connect().subscribe(
            CommandListener.TOPIC,
            object : CommandListener {
                override fun commandFinished(event: CommandEvent) {
                    if (event.commandName == commandName) {
                        check()
                        completionPromise.setResult(true)
                    }
                }
            }
        )

        codeBlock()

        PlatformTestUtil.waitForPromise(completionPromise, 3 * 60 * 1000)
    }

    private fun checkNotificationIsShown(expectedContent: String, codeBlock: () -> Unit) {
        val completionPromise = AsyncPromise<Boolean>()

        myFixture.project.messageBus.connect().subscribe(
            Notifications.TOPIC,
            object : Notifications {
                override fun notify(notification: Notification) {
                    if (notification.hasContent()) {
                        TestCase.assertEquals(expectedContent, notification.content)
                        completionPromise.setResult(true)
                    }
                }
            }
        )

        codeBlock()

        PlatformTestUtil.waitForPromise(completionPromise, 3 * 60 * 1000)
        assertTrue("Expected notification was not shown.", completionPromise.isSucceeded)
    }
}
