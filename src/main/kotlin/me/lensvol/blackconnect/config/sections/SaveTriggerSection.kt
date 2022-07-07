package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

internal data class SaveTriggerSectionModel(
    var triggerOnEachSave: Boolean = false,
    var triggerOnReformat: Boolean = false
)

@Suppress("UnstableApiUsage")
class SaveTriggerSection(project: Project) : ConfigSection(project) {
    private val state = SaveTriggerSectionModel()

    override val panel: DialogPanel by lazy {
        panel {
            group("Trigger Settings") {
                row {
                    checkBox("Trigger when saving changed files").bindSelected(state::triggerOnEachSave)
                }
                row {
                    checkBox("Trigger on code reformat").bindSelected(state::triggerOnReformat)
                }
            }
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        state.triggerOnEachSave = projectConfig.triggerOnEachSave
        state.triggerOnReformat = projectConfig.triggerOnReformat
        this.panel.reset()
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        this.panel.apply()
        projectConfig.triggerOnEachSave = state.triggerOnEachSave
        projectConfig.triggerOnReformat = state.triggerOnReformat
    }

    override fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return this.panel.isModified()
    }
}
