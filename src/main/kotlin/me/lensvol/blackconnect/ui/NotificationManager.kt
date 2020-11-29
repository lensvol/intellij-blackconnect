package me.lensvol.blackconnect.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

const val MAIN_DISPLAY_ID = "BlackConnect"

@Service
open class NotificationManager(project: Project) {
    private val currentProject = project
    private val shownErrorNotifications = HashSet<Notification>()

    private fun mainGroup(): NotificationGroup {
        val existingGroup = NotificationGroup.findRegisteredGroup(MAIN_DISPLAY_ID)
        if (existingGroup != null) {
            return existingGroup
        }

        return NotificationGroup(MAIN_DISPLAY_ID, NotificationDisplayType.BALLOON, false)
    }

    open fun showError(text: String) {
        val notification = mainGroup().createNotification(text, NotificationType.ERROR)
        shownErrorNotifications.add(notification)

        notification
            .setTitle("BlackConnect")
            .whenExpired { shownErrorNotifications.remove(notification) }
            .notify(currentProject)
    }

    open fun expireAllErrors() {
        shownErrorNotifications.map { it.expire() }
        shownErrorNotifications.clear()
    }
}
