package me.lensvol.blackconnect.config.sections

import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JPanel

interface ConfigSection {
    val panel: JPanel

    fun loadFrom(configuration: BlackConnectProjectSettings)

    fun saveTo(configuration: BlackConnectProjectSettings)

    fun isModified(configuration: BlackConnectProjectSettings): Boolean
}