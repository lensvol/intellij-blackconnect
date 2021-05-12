package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
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

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        triggerOnEachSave.isSelected = projectConfig.triggerOnEachSave
        triggerOnReformat.isSelected = projectConfig.triggerOnReformat
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        projectConfig.triggerOnEachSave = triggerOnEachSave.isSelected
        projectConfig.triggerOnReformat = triggerOnReformat.isSelected
    }

    override fun isModified(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings): Boolean {
        return projectConfig.triggerOnEachSave != triggerOnEachSave.isSelected ||
            projectConfig.triggerOnReformat != triggerOnReformat.isSelected
    }
}
