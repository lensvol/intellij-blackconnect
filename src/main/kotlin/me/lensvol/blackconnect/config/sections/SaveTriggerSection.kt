package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JPanel

class SaveTriggerSection(project: Project) : ConfigSection(project) {
    private val triggerOnEachSave = JCheckBox("Trigger when saving changed files")
    private val triggerOnReformat = JCheckBox("Trigger on code reformat")

    override val panel: JPanel by lazy {
        JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Trigger Settings")
            val formPanel = FormBuilder.createFormBuilder()
                .addComponent(triggerOnEachSave)
                .addComponent(triggerOnReformat)
                .panel
            add(formPanel)
        }
    }

    override fun loadFrom(configuration: BlackConnectProjectSettings) {
        triggerOnEachSave.isSelected = configuration.triggerOnEachSave
        triggerOnReformat.isSelected = configuration.triggerOnReformat
    }

    override fun saveTo(configuration: BlackConnectProjectSettings) {
        configuration.triggerOnEachSave = triggerOnEachSave.isSelected
        configuration.triggerOnReformat = triggerOnReformat.isSelected
    }

    override fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        return configuration.triggerOnEachSave != triggerOnEachSave.isSelected
            || configuration.triggerOnReformat != triggerOnReformat.isSelected
    }
}
