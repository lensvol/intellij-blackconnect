package me.lensvol.blackconnect.config

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.UIUtil
import me.lensvol.blackconnect.config.sections.ConnectionSection
import me.lensvol.blackconnect.config.sections.FormattingSection
import me.lensvol.blackconnect.config.sections.MiscSettingsSection
import me.lensvol.blackconnect.config.sections.SaveTriggerSection
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

const val PYPROJECT_TOML: String = "pyproject.toml"
const val DEFAULT_LINE_LENGTH: Int = 88
const val DEFAULT_BLACKD_PORT: Int = 45484

class BlackConnectSettingsPanel(project: Project) : JPanel() {
    private val configSections = listOf(
        SaveTriggerSection(project),
        ConnectionSection(project),
        FormattingSection(project),
        MiscSettingsSection(project)
    )

    init {
        layout = GridBagLayout()
        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)
        val constraints = initBagLayoutConstraints()

        configSections.map {
            add(it.panel, constraints)
        }

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

    fun apply(configuration: BlackConnectProjectSettings) {
        configSections.map {
            it.saveTo(configuration)
        }
    }

    fun load(configuration: BlackConnectProjectSettings) {
        configSections.map {
            it.loadFrom(configuration)
        }
    }

    fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        return configSections.fold(
            false,
            { changed, section -> changed || section.isModified(configuration) }
        )
    }
}
