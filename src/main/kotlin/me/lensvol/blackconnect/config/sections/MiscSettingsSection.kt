@file:Suppress("UnstableApiUsage")

package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

internal data class MiscSettingsSectionModel(
    var enableJupyterSupport: Boolean = false,
    var showSyntaxErrorMsgs: Boolean = false
)

class MiscSettingsSection(project: Project) : ConfigSection(project) {
    private val state = MiscSettingsSectionModel()
    override val panel: DialogPanel by lazy {
        panel {
            group("Miscellaneous Settings") {
                row {
                    checkBox("Enable Jupyter Notebook support (whole file only)")
                        .bindSelected(state::enableJupyterSupport)
                }
                row {
                    checkBox("Show notifications about syntax errors")
                        .bindSelected(state::showSyntaxErrorMsgs)
                }
            }
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        state.showSyntaxErrorMsgs = projectConfig.showSyntaxErrorMsgs
        state.enableJupyterSupport = projectConfig.enableJupyterSupport

        this.panel.reset()
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        this.panel.apply()

        projectConfig.showSyntaxErrorMsgs = state.showSyntaxErrorMsgs
        projectConfig.enableJupyterSupport = state.enableJupyterSupport
    }

    override fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return this.panel.isModified()
    }
}
