package me.lensvol.blackconnect.config

import com.intellij.openapi.project.Project
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.UIUtil
import me.lensvol.blackconnect.config.sections.ConnectionSection
import me.lensvol.blackconnect.config.sections.FormattingSection
import me.lensvol.blackconnect.config.sections.LocalDaemonSection
import me.lensvol.blackconnect.config.sections.MiscSettingsSection
import me.lensvol.blackconnect.config.sections.SaveTriggerSection
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

const val PYPROJECT_TOML: String = "pyproject.toml"
const val DEFAULT_LINE_LENGTH: Int = 88
const val DEFAULT_BLACKD_PORT: Int = 45484
const val DEFAULT_BLACKD_HOST: String = "localhost"

class BlackConnectSettingsPanel(project: Project) : JPanel() {
    private val configSections = listOf(
        SaveTriggerSection(project),
        ConnectionSection(project),
        LocalDaemonSection(project),
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

    fun apply(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        // We are doing this in two passes to prevent partial save of the incorrect configuration
        configSections.map {
            it.validate()
        }
        configSections.map {
            it.saveTo(globalConfig, projectConfig)
        }
    }

    fun load(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        configSections.map {
            it.loadFrom(globalConfig, projectConfig)
        }
    }

    fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return configSections.fold(
            false,
            { changed, section -> changed || section.isModified(globalConfig, projectConfig) }
        )
    }
}
