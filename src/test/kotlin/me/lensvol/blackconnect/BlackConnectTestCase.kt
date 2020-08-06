package me.lensvol.blackconnect

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
import org.jetbrains.concurrency.AsyncPromise
import org.mockserver.integration.ClientAndServer

abstract class BlackConnectTestCase : BasePlatformTestCase() {
    lateinit var mockServer: ClientAndServer
    override fun setUp() {
        super.setUp()
        mockServer = ClientAndServer.startClientAndServer(45484)
    }

    override fun tearDown() {
        mockServer.close()
        super.tearDown()
    }

    protected fun eventForFile(file: VirtualFile): TestActionEvent {
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

    protected fun checkWhenCommandCompletes(commandName: String, codeBlock: () -> Unit, check: () -> Unit) {
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

    protected fun checkNotificationIsShown(expectedContent: String, codeBlock: () -> Unit) {
        val completionPromise = AsyncPromise<Boolean>()

        val busConnection = myFixture.project.messageBus.connect()
        busConnection.subscribe(
            Notifications.TOPIC,
            object : Notifications {
                override fun notify(notification: Notification) {
                    if (notification.hasContent()) {
                        TestCase.assertEquals(expectedContent, notification.content)
                        busConnection.disconnect()
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
