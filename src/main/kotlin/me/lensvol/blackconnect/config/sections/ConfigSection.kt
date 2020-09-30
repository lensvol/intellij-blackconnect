package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JPanel

abstract class ConfigSection(project: Project) {
    abstract val panel: JPanel

    abstract fun loadFrom(configuration: BlackConnectProjectSettings)

    abstract fun saveTo(configuration: BlackConnectProjectSettings)

    abstract fun isModified(configuration: BlackConnectProjectSettings): Boolean
}