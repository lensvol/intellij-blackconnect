package me.lensvol.blackconnect

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class BlackConnectConfigurable(project: Project) : Configurable {
    private val panel: BlackConnectSettingsPanel by lazy {
        BlackConnectSettingsPanel()
    }

    private val configuration: BlackConnectSettingsConfiguration = BlackConnectSettingsConfiguration.getInstance(project)

    override fun isModified(): Boolean {
        return true
    }

    override fun getDisplayName(): String {
        return "BlackConnect"
    }

    override fun apply() {
        panel.apply(configuration)
    }

    override fun createComponent(): JComponent? {
        panel.load(configuration)
        return panel
    }

}

