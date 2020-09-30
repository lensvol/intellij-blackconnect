package me.lensvol.blackconnect.config

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import me.lensvol.blackconnect.config.sections.ConnectionSection
import me.lensvol.blackconnect.config.sections.FormattingSection
import me.lensvol.blackconnect.config.sections.SaveTriggerSection
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

const val PYPROJECT_TOML: String = "pyproject.toml"
const val DEFAULT_LINE_LENGTH: Int = 88
const val DEFAULT_BLACKD_PORT: Int = 45484

class BlackConnectSettingsPanel(project: Project) : JPanel() {
    private val configSections = listOf(
        SaveTriggerSection(project),
        ConnectionSection(project),
        FormattingSection(project)
    )

    private val jupyterSupportCheckbox = JCheckBox("Enable Jupyter Notebook support (whole file only)")
    private val showSyntaxErrorMsgsCheckbox = JCheckBox("Show notifications about syntax errors")

    init {
        layout = GridBagLayout()
        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)
        val constraints = initBagLayoutConstraints()

        configSections.map {
            add(it.panel, constraints)
        }

        val miscSettingsPanel = createMiscSettingsPanel()
        miscSettingsPanel.alignmentX = Component.LEFT_ALIGNMENT
        add(miscSettingsPanel, constraints)

        addEmptyFiller(this, constraints)
    }

    private fun addEmptyFiller(container: JComponent, constraints: GridBagConstraints) {
        // Add empty filler to push our other panels to the top
        constraints.fill = GridBagConstraints.VERTICAL
        constraints.gridheight = GridBagConstraints.REMAINDER
        constraints.weighty = 2.0
        constraints.gridx = 0
        constraints.anchor = GridBagConstraints.NORTH
        container.add(JPanel(), constraints)
    }

    private fun initBagLayoutConstraints(): GridBagConstraints {
        val constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.NORTHWEST
        constraints.weightx = 1.0
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER
        return constraints
    }

    private fun createMiscSettingsPanel(): JPanel {
        return JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Miscellaneous settings")

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(jupyterSupportCheckbox)
                    .addComponent(showSyntaxErrorMsgsCheckbox)
                    .panel,
                BorderLayout.NORTH
            )
        }
    }

    fun apply(configuration: BlackConnectProjectSettings) {
        configSections.map {
            it.saveTo(configuration)
        }


        configuration.enableJupyterSupport = jupyterSupportCheckbox.isSelected
        configuration.showSyntaxErrorMsgs = showSyntaxErrorMsgsCheckbox.isSelected
    }

    fun load(configuration: BlackConnectProjectSettings) {
        configSections.map {
            it.loadFrom(configuration)
        }

        jupyterSupportCheckbox.isSelected = configuration.enableJupyterSupport
        showSyntaxErrorMsgsCheckbox.isSelected = configuration.showSyntaxErrorMsgs

    }

    fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        val anyChangesInSections = configSections.fold(
            false,
            { changed, section -> changed || section.isModified(configuration) }
        )
        return anyChangesInSections ||
            jupyterSupportCheckbox.isSelected != configuration.enableJupyterSupport ||
            showSyntaxErrorMsgsCheckbox.isSelected != configuration.showSyntaxErrorMsgs
    }
}
