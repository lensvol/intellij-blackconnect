package me.lensvol.blackconnect.ui

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class NotificationManager(project: Project) {
    val MAIN_DISPLAY_ID = "BlackConnect"
    val currentProject = project

    private fun mainGroup(): NotificationGroup {
        val existingGroup = NotificationGroup.findRegisteredGroup(MAIN_DISPLAY_ID)
        if (existingGroup != null) {
            return existingGroup
        }

        return NotificationGroup(MAIN_DISPLAY_ID, NotificationDisplayType.BALLOON, false)
    }

    fun showError(text: String) {
        mainGroup()
            .createNotification(text, NotificationType.ERROR)
            .setTitle("BlackConnect")
            .notify(currentProject)
    }
}
