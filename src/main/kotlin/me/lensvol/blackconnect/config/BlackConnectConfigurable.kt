package me.lensvol.blackconnect.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import javax.swing.JComponent

class BlackConnectConfigurable(project: Project) : Configurable {
    private val panel: BlackConnectSettingsPanel by lazy {
        BlackConnectSettingsPanel(project)
    }

    private val configuration: BlackConnectProjectSettings =
        BlackConnectProjectSettings.getInstance(project)

    override fun isModified(): Boolean {
        return panel.isModified(configuration)
    }

    override fun getDisplayName(): String {
        return "BlackConnect"
    }

    override fun apply() {
        panel.apply(configuration)
    }

    override fun reset() {
        panel.load(configuration)
    }

    override fun createComponent(): JComponent? {
        panel.load(configuration)
        return panel
    }
}

