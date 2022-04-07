package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JPanel

@Suppress("UNUSED_PARAMETER")
abstract class ConfigSection(project: Project) {
    abstract val panel: JPanel

    abstract fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings)

    abstract fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings)

    abstract fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean

    open fun validate() {}
}
