package me.lensvol.blackconnect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import me.lensvol.blackconnect.config.BlackConnectConfigurable
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.ui.NotificationManager
import java.util.Properties
import kotlin.concurrent.thread

class PluginStartupActivity : StartupActivity, DumbAware {
    private val logger = Logger.getInstance(PluginStartupActivity::class.java.name)

    override fun runActivity(project: Project) {
        val globalSettings = BlackConnectGlobalSettings.getInstance()
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

        if (ApplicationManager.getApplication().isUnitTestMode) return

        //
        if (globalSettings.spawnBlackdOnStartup && globalSettings.blackdBinaryPath.isNotEmpty()) {
            thread {
                val blackdExecutor = service<BlackdExecutor>()
                val result = blackdExecutor.startDaemon(
                    globalSettings.blackdBinaryPath,
                    globalSettings.bindOnHostname,
                    globalSettings.bindOnPort
                )
                invokeLater {
                    when (result) {
                        is ExecutionResult.Started -> {}
                        is ExecutionResult.AlreadyStarted -> {
                            notificationManager.showInfo("Already started <b>blackd</b> process (PID: ${result.pid})")
                        }
                        is ExecutionResult.Failed -> {
                            notificationManager.showError("Failed to start <b>blackd</b> on " +
                                "${globalSettings.bindOnHostname}:${globalSettings.bindOnPort}",
                                additionalInfo = result.reason,
                                viewPromptText = "View error"
                            )
                        }
                    }
                }
            }
        }

        // If uncommented, it will show settings for the plugin immediately upon startup.
        // if (!ApplicationManager.getApplication().isUnitTestMode) {
        //     invokeLater {
        //         ShowSettingsUtil.getInstance().editConfigurable(project, BlackConnectConfigurable(project))
        //     }
        // }
    }
}
