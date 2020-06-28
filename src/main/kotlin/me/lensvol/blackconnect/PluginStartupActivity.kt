package me.lensvol.blackconnect

import com.intellij.AppTopics
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.ui.NewSettingsReminderNotification
import me.lensvol.blackconnect.ui.NotificationGroupManager
import java.util.Properties

class PluginStartupActivity : StartupActivity, DumbAware {
    private val logger = Logger.getInstance(PluginStartupActivity::class.java.name)

    override fun runActivity(project: Project) {
        val applicationInfo = ApplicationInfo.getInstance()

        val properties = Properties()
        val versionResource = PluginStartupActivity::class.java.getResourceAsStream("/version.properties")
        if (versionResource != null) {
            properties.load(versionResource)
            versionResource.close()
            logger.info("BlackConnect plugin (version: " + properties["version"] + ") is ready to start.")
        } else {
            logger.info("Unknown version of BlackConnect plugin is ready to start.")
        }

        // FIXME: For some reason, project listeners declared in plugin.xml
        // are not being registered, so we do it manually.
        if (applicationInfo.build.baselineVersion < 201) {
            logger.debug("IDE version is too old, we manually subscribe to FILE_DOCUMENT_SYNC events.")
            project.messageBus.connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, FileSaveListener(project))
        }

        val appLevelConfiguration = BlackConnectGlobalSettings.getInstance()
        if (appLevelConfiguration.showSaveTriggerOptIn) {
            logger.debug("Showing opt-in balloon.")
            val optInNotification = NewSettingsReminderNotification(
                NotificationGroupManager.newsGroup(),
                project, appLevelConfiguration
            )
            optInNotification.notify(project)
        }
    }
}

