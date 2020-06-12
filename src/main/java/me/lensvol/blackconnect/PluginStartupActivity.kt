package me.lensvol.blackconnect

import com.intellij.AppTopics
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.ui.NewSettingsReminderNotification

class PluginStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val applicationInfo = ApplicationInfo.getInstance()

        // FIXME: For some reason, project listeners declared in plugin.xml
        // are not being registered, so we do it manually.
        if (applicationInfo.build.baselineVersion < 201) {
            project.messageBus.connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, FileSaveListener(project))
        }

        val notificationGroup = NotificationGroup("BlackConnect", NotificationDisplayType.STICKY_BALLOON, false)
        val appLevelConfiguration = BlackConnectGlobalSettings.getInstance()
        if (appLevelConfiguration.showSaveTriggerOptIn) {
            val optInNotification = NewSettingsReminderNotification(
                notificationGroup,
                project, appLevelConfiguration
            )
            optInNotification.notify(project)
        }
    }
}

