package me.lensvol.blackconnect.mocks

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import junit.framework.TestCase
import me.lensvol.blackconnect.ui.NotificationManager

@Service
class NotificationManagerMock(project: Project) : NotificationManager(project) {
    private val shownNotifications = mutableListOf<String>()

    override fun showError(text: String) {
        shownNotifications.add(text)
    }

    fun assertNotificationShown(expectedText: String) {
        TestCase.assertTrue(shownNotifications.contains(expectedText))
    }

    fun assertNotificationNotShown(expectedText: String) {
        TestCase.assertFalse(shownNotifications.contains(expectedText))
    }
}
