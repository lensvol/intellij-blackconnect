package me.lensvol.blackconnect

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute

@State(name = "BlackConnectSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class BlackConnectSettingsConfiguration : PersistentStateComponent<BlackConnectSettingsConfiguration> {

    companion object {
        fun getInstance(project: Project): BlackConnectSettingsConfiguration =
                ServiceManager.getService(project, BlackConnectSettingsConfiguration::class.java)
    }

    @Attribute
    var hostname: String = "localhost"

    @Attribute
    var port: Int = 45484

    @Attribute
    var lineLength: Int = 80

    override fun getState() = this

    override fun loadState(state: BlackConnectSettingsConfiguration) {
        XmlSerializerUtil.copyBean(state, this)
    }
}