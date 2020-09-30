package me.lensvol.blackconnect.config.sections

import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JPanel

class SaveTriggerSection : ConfigSection {
    private val triggerOnEachSave = JCheckBox("Trigger when saving changed files")

    override val panel: JPanel by lazy {
        JPanel().apply {
            layout = BorderLayout()
            alignmentX = Component.LEFT_ALIGNMENT

            add(triggerOnEachSave)
        }
    }

    override fun loadFrom(projectConfig: BlackConnectProjectSettings) {
        triggerOnEachSave.isSelected = projectConfig.triggerOnEachSave
    }

    override fun saveTo(projectConfig: BlackConnectProjectSettings) {
        projectConfig.triggerOnEachSave = triggerOnEachSave.isSelected
    }

    override fun isModified(projectConfig: BlackConnectProjectSettings): Boolean {
        return projectConfig.triggerOnEachSave != triggerOnEachSave.isSelected
    }
}