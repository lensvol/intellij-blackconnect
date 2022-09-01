package me.lensvol.blackconnect

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.intellij.util.xmlb.XmlSerializerUtil
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import org.junit.rules.ExternalResource

/**
 * Provides `fixture` for code insight and editor tests based on a lightweight reusable IDE project.
 */
class IdeaTestRule : ExternalResource() {
    companion object {
        init {
            // Don't show the Dock icon on macOS when running tests
            System.setProperty("apple.awt.UIElement", "true")
        }
    }

    lateinit var fixture: CodeInsightTestFixture

    override fun before() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        // We can customize this fixture further here to fit the needs of our plugin tests
        val fixtureBuilder = factory.createLightFixtureBuilder(null)
        val tempDirFixture = LightTempDirTestFixtureImpl(true)
        fixture = factory.createCodeInsightFixture(fixtureBuilder.fixture, tempDirFixture)
        fixture.testDataPath = FileUtil.toSystemIndependentName(PlatformTestUtil.getCommunityPath())
        fixture.setUp()
    }

    override fun after() {
        fixture.tearDown()
    }
}

/**
 * Runs `blackd` within the project's Poetry environment, waits until it accepts connections before continuing.
 *
 * Since blackd takes a couple of seconds to initialise and it is meant to be stateless, you can use this rule
 * as a `@ClassRule` to save some time.
 *
 * This rule installs `blackd` in the Poetry environment of the project if needed.
 */
class BlackdProcessRule(private val port: Int, private val timeout: Int = 5_000) : ExternalResource() {
    private lateinit var process: Process

    override fun before() {
        installDependencies()
        val commandLine = GeneralCommandLine("poetry", "run", "blackd", "--bind-port", port.toString())
        process = commandLine.createProcess()
        try {
            BlackdClient("localhost", port, useSsl = false).waitUntilConnected(timeout)
        } catch (e: Exception) {
            process.destroyForcibly()
            throw e
        }
    }

    override fun after() {
        process.destroyForcibly()
    }

    companion object {
        private fun installDependencies() {
            val commandLine = GeneralCommandLine("poetry", "install")
            val p = commandLine.createProcess()
            if (p.waitFor() != 0) {
                error("Error running '$commandLine': ${p.inputStream.bufferedReader().readText()}")
            }
        }

        private fun BlackdClient.waitUntilConnected(timeout: Int) {
            val attempts = 10
            val reconnectTimeout = (timeout / attempts).toLong()
            var i = 0
            var result = checkConnection()
            while (result is Failure && i < attempts) {
                Thread.sleep(reconnectTimeout)
                result = checkConnection()
                i++
            }
            if (result is Failure) {
                error("Cannot connect to blackd: ${result.reason}")
            }
        }
    }
}

/**
 * Restores Black project settings on every test.
 */
class BlackSettingsRule(private val ideaTest: IdeaTestRule, private val port: Int) : ExternalResource() {
    private lateinit var oldSettings: BlackConnectProjectSettings

    override fun before() {
        oldSettings = XmlSerializerUtil.createCopy(ideaTest.fixture.blackSettings)
        ideaTest.fixture.blackSettings.port = port
    }

    override fun after() {
        ideaTest.fixture.blackSettings.loadState(oldSettings)
    }

    companion object {
        val CodeInsightTestFixture.blackSettings: BlackConnectProjectSettings
            get() = BlackConnectProjectSettings.getInstance(project)
    }
}
