package me.lensvol.blackconnect.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JComponent

class BlackConnectConfigurable(project: Project) : Configurable {
    private val panel: BlackConnectSettingsPanel by lazy {
        BlackConnectSettingsPanel(project)
    }

    private val projectConfig: BlackConnectProjectSettings = BlackConnectProjectSettings.getInstance(project)

    private val globalConfig: BlackConnectGlobalSettings = BlackConnectGlobalSettings.getInstance()

    override fun isModified(): Boolean {
        return panel.isModified(globalConfig, projectConfig)
    }

    override fun getDisplayName(): String {
        return "BlackConnect"
    }

    override fun apply() {
        panel.apply(globalConfig, projectConfig)
    }

    override fun reset() {
        panel.load(globalConfig, projectConfig)
    }

    override fun createComponent(): JComponent? {
        panel.load(globalConfig, projectConfig)
        return panel
    }
}
