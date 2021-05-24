package me.lensvol.blackconnect.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute

@State(name = "BlackConnectGlobalSettings", storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)])
class BlackConnectGlobalSettings : PersistentStateComponent<BlackConnectGlobalSettings> {

    companion object {
        fun getInstance() = service<BlackConnectGlobalSettings>()
    }

    @Attribute
    var spawnBlackdOnStartup: Boolean = true

    @Attribute
    var showSaveTriggerOptIn: Boolean = true

    @Attribute
    var bindOnHostname: String = "localhost"

    @Attribute
    var bindOnPort: Int = 45484

    @Attribute
    var blackdBinaryPath: String = ""

    override fun getState() = this

    override fun loadState(state: BlackConnectGlobalSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
