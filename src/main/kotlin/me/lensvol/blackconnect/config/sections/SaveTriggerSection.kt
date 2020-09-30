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

    override fun loadFrom(configuration: BlackConnectProjectSettings) {
        triggerOnEachSave.isSelected = configuration.triggerOnEachSave
    }

    override fun saveTo(configuration: BlackConnectProjectSettings) {
        configuration.triggerOnEachSave = triggerOnEachSave.isSelected
    }

    override fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        return configuration.triggerOnEachSave != triggerOnEachSave.isSelected
    }
}