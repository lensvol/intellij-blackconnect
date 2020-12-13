package me.lensvol.blackconnect.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

const val MAIN_DISPLAY_ID = "BlackConnect"

@Service
open class NotificationManager(project: Project) {
    private val currentProject = project
    private val shownErrorNotifications = ConcurrentHashMap<Notification, String>()

    private fun mainGroup(): NotificationGroup {
        val existingGroup = NotificationGroup.findRegisteredGroup(MAIN_DISPLAY_ID)
        if (existingGroup != null) {
            return existingGroup
        }

        return NotificationGroup(MAIN_DISPLAY_ID, NotificationDisplayType.BALLOON, false)
    }

    open fun showError(text: String) {
        for ((shown_notification, error_text) in shownErrorNotifications.iterator()) {
            if (error_text == text) {
                shown_notification.expire()
                shownErrorNotifications.remove(shown_notification)
            }
        }

        val newNotification = mainGroup().createNotification(text, NotificationType.ERROR)
        shownErrorNotifications[newNotification] = text

        newNotification
            .setTitle("BlackConnect")
            .whenExpired { shownErrorNotifications.remove(newNotification) }
            .notify(currentProject)
    }

    open fun expireAllErrors() {
        shownErrorNotifications.keys.map { it.expire() }
        shownErrorNotifications.clear()
    }
}
