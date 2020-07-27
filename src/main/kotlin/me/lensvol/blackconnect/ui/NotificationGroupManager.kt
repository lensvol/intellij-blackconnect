package me.lensvol.blackconnect.ui

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup

class NotificationGroupManager {
    companion object {
        const val MAIN_DISPLAY_ID = "BlackConnect"

        fun mainGroup(): NotificationGroup {
            val existingGroup = NotificationGroup.findRegisteredGroup(MAIN_DISPLAY_ID)
            if (existingGroup != null) {
                return existingGroup
            }

            return NotificationGroup(MAIN_DISPLAY_ID, NotificationDisplayType.BALLOON, false)
        }
    }
}
