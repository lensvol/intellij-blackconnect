package me.lensvol.blackconnect.ui

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup

class NotificationGroupManager {
    companion object {
        const val MAIN_DISPLAY_ID = "BlackConnect"
        const val NEWS_DISPLAY_ID = "BlackConnect News"

        fun newsGroup(): NotificationGroup {
            val existingGroup = NotificationGroup.findRegisteredGroup(NEWS_DISPLAY_ID)
            if (existingGroup != null) {
                return existingGroup
            }

            return NotificationGroup(NEWS_DISPLAY_ID, NotificationDisplayType.STICKY_BALLOON, false)
        }

        fun mainGroup(): NotificationGroup {
            val existingGroup = NotificationGroup.findRegisteredGroup(MAIN_DISPLAY_ID)
            if (existingGroup != null) {
                return existingGroup
            }

            return NotificationGroup.balloonGroup(MAIN_DISPLAY_ID)
        }
    }
}