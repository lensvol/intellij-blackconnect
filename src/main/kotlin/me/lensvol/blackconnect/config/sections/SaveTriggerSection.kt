package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JPanel

class SaveTriggerSection(project: Project) : ConfigSection(project) {
    private val triggerOnEachSave = JCheckBox("Trigger when saving changed files")

    override val panel: JPanel by lazy {
        JPanel().apply {
            layout = BorderLayout()
            alignmentX = Component.LEFT_ALIGNMENT

            add(triggerOnEachSave)
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        triggerOnEachSave.isSelected = projectConfig.triggerOnEachSave
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        projectConfig.triggerOnEachSave = triggerOnEachSave.isSelected
    }

    override fun isModified(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings): Boolean {
        return projectConfig.triggerOnEachSave != triggerOnEachSave.isSelected
    }
}
