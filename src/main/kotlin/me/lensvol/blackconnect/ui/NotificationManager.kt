package me.lensvol.blackconnect.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationFullContent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

const val MAIN_DISPLAY_ID = "BlackConnect"

class FullSizeErrorNotification(text: String) : NotificationFullContent,
    Notification(MAIN_DISPLAY_ID, "BlackConnect", text, NotificationType.ERROR)

@Service
open class NotificationManager(project: Project) {
    private val currentProject = project
    private val shownErrorNotifications = ConcurrentHashMap<Notification, String>()

    open fun showInfo(text: String) {
        Notification(MAIN_DISPLAY_ID, null, NotificationType.INFORMATION)
            .setTitle("BlackConnect")
            .setContent(text)
            .notify(currentProject)
    }

    open fun showError(text: String, additionalInfo: String? = null, viewPromptText: String = "View") {
        for ((shown_notification, error_text) in shownErrorNotifications.iterator()) {
            if (error_text == text) {
                shown_notification.expire()
                shownErrorNotifications.remove(shown_notification)
            }
        }

        val newNotification = FullSizeErrorNotification(text)
        shownErrorNotifications[newNotification] = text

        newNotification
            .setTitle("BlackConnect")
            .setImportant(true)
            .whenExpired { shownErrorNotifications.remove(newNotification) }

        additionalInfo?.let {
            newNotification.addAction(
                NotificationAction.createSimple(viewPromptText) {
                    AdditionalInformationDialog(currentProject, additionalInfo).show()
                }
            )
        }

        newNotification.notify(currentProject)
    }

    open fun expireAllErrors() {
        shownErrorNotifications.keys.map { it.expire() }
        shownErrorNotifications.clear()
    }
}
