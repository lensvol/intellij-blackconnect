package me.lensvol.blackconnect.config.sections

import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JPanel

interface ConfigSection {
    val panel: JPanel

    fun loadFrom(projectConfig: BlackConnectProjectSettings)

    fun saveTo(projectConfig: BlackConnectProjectSettings)

    fun isModified(projectConfig: BlackConnectProjectSettings): Boolean
}