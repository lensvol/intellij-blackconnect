package me.lensvol.blackconnect

import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.testFramework.PlatformTestUtil
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import org.jetbrains.concurrency.AsyncPromise
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class SmokeTestCase : BlackConnectTestCase() {
    lateinit var mockServer: ClientAndServer

    override fun setupMocks()

    override fun setUp() {
        super.setUp()
        mockServer = ClientAndServer.startClientAndServer(45484)
    }

    override fun tearDown() {
        mockServer.close()
        super.tearDown()
    }

    private fun checkWhenCommandCompletes(commandName: String, codeBlock: () -> Unit, check: () -> Unit) {
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

    @Test
    fun test_whole_file_is_reformatted_via_menu_action() {
        val unformattedFile = myFixture.copyFileToProject("unformatted.py")
        myFixture.openFileInEditor(unformattedFile)
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

        val codeUnderTest = {
            val event = eventForFile(unformattedFile)
            val actionUnderTest = ReformatWholeFileAction()
            actionUnderTest.beforeActionPerformedUpdate(event)
            actionUnderTest.actionPerformed(event)
        }

        checkWhenCommandCompletes("Reformat Code Using Black", codeUnderTest) {
            myFixture.checkResultByFile("reformatted.py")
        }
    }
}
