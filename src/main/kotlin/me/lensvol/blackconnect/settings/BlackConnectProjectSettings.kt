package me.lensvol.blackconnect.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import me.lensvol.blackconnect.Constants

@State(name = "BlackConnectSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class BlackConnectProjectSettings : PersistentStateComponent<BlackConnectProjectSettings> {

    companion object {
        fun getInstance(project: Project): BlackConnectProjectSettings =
            ServiceManager.getService(project, BlackConnectProjectSettings::class.java)
    }

    @Attribute
    var hostname: String = Constants.DEFAULT_HOST_BINDING

    @Attribute
    var port: Int = Constants.DEFAULT_BLACKD_PORT

    @Attribute
    var useSSL: Boolean = Constants.DEFAULT_USE_SSL

    @Attribute
    var lineLength: Int = Constants.DEFAULT_LINE_LENGTH

    @Attribute
    var fastMode: Boolean = false

    @Attribute
    var skipStringNormalization: Boolean = false

    @Attribute
    var skipMagicTrailingComma: Boolean = false

    @Attribute
    var triggerOnEachSave: Boolean = false

    @Attribute
    var targetSpecificVersions: Boolean = false

    @Attribute
    var pythonTargets: String = ""

    @Attribute
    var enableJupyterSupport: Boolean = false

    @Attribute
    var showSyntaxErrorMsgs: Boolean = false

    override fun getState() = this

    override fun loadState(state: BlackConnectProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
