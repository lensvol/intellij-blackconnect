package me.lensvol.blackconnect

import com.intellij.AppTopics
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class PluginStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val applicationInfo = ApplicationInfo.getInstance()
        val configuration = BlackConnectSettingsConfiguration.getInstance(project)

        // FIXME: For some reason, project listeners declared in plugin.xml
        // are not being registered, so we do it manually.
        if (applicationInfo.build.baselineVersion < 201) {
            project.messageBus.connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, FileSaveListener(project))
        }

        val notificationGroup = NotificationGroup("BlackConnect", NotificationDisplayType.STICKY_BALLOON, false)

        if (configuration.showSaveTriggerOptIn) {
            val optInNotification = NewSettingsReminderNotification(
                    notificationGroup,
                    project, configuration
            )
            optInNotification.notify(project)
        }
    }
}

