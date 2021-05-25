package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JPanel

class MiscSettingsSection(project: Project) : ConfigSection(project) {
    private val jupyterSupportCheckbox = JCheckBox("Enable Jupyter Notebook support (whole file only)")
    private val showSyntaxErrorMsgsCheckbox = JCheckBox("Show notifications about syntax errors")

    override val panel: JPanel = createPanel()

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Miscellaneous Settings")

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(jupyterSupportCheckbox)
                    .addComponent(showSyntaxErrorMsgsCheckbox)
                    .panel,
                BorderLayout.NORTH
            )
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        jupyterSupportCheckbox.isSelected = projectConfig.enableJupyterSupport
        showSyntaxErrorMsgsCheckbox.isSelected = projectConfig.showSyntaxErrorMsgs
    }
        
    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        projectConfig.enableJupyterSupport = jupyterSupportCheckbox.isSelected
        projectConfig.showSyntaxErrorMsgs = showSyntaxErrorMsgsCheckbox.isSelected
    }

    override fun isModified(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings): Boolean {
        return jupyterSupportCheckbox.isSelected != projectConfig.enableJupyterSupport ||
            showSyntaxErrorMsgsCheckbox.isSelected != projectConfig.showSyntaxErrorMsgs
    }
}
