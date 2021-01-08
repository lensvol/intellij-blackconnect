package me.lensvol.blackconnect

import com.intellij.AppTopics
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import me.lensvol.blackconnect.config.BlackConnectConfigurable
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager
import java.util.Properties

class PluginStartupActivity : StartupActivity, DumbAware {
    private val logger = Logger.getInstance(PluginStartupActivity::class.java.name)

    override fun runActivity(project: Project) {
        val globalSettings = BlackConnectGlobalSettings.getInstance()
        val projectSettings = BlackConnectProjectSettings.getInstance(project)
        val applicationInfo = ApplicationInfo.getInstance()
        val notificationManager = project.service<NotificationManager>()

        val properties = Properties()
        val versionResource = PluginStartupActivity::class.java.getResourceAsStream("/version.properties")
        if (versionResource != null) {
            properties.load(versionResource)
            versionResource.close()
            logger.info("BlackConnect plugin (version: " + properties["version"] + ") is ready to start.")
        } else {
            logger.info("Unknown version of BlackConnect plugin is ready to start.")
        }

        val blackdExecutor = service<BlackdExecutor>()
        if (globalSettings.spawnBlackdOnStartup) {
            val server = blackdExecutor.serverFor(
                globalSettings.blackdBinaryPath,
                globalSettings.bindOnHostname,
                globalSettings.bindOnPort
            )
            when (val result = server.startDaemon()) {
                is ExecutionResult.Started -> {
                    notificationManager.showInfo("Successfully started <b>blackd</b> process (PID: ${result.pid})")
                }
                is ExecutionResult.AlreadyStarted -> {
                    notificationManager.showInfo("Already started <b>blackd</b> process (PID: ${result.pid})")
                }
                is ExecutionResult.Failed -> {
                    notificationManager.showError("Failed to start <b>blackd</b>:<br>${result.reason}")
                }
            }
        }

        // If uncommented, it will show settings for the plugin immediately upon startup.
        invokeLater {
            ShowSettingsUtil.getInstance().editConfigurable(project, BlackConnectConfigurable(project))
        }
    }
}
