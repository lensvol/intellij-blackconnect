package me.lensvol.blackconnect

import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.testFramework.LightVirtualFile
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import org.junit.Test

class FileSupportDetectionTest : BlackConnectTestCase() {
    @Test
    fun test_unknown_file_type_does_not_crash_with_jupyter_support_enabled() {
        val unknownFile = LightVirtualFile("file.unknown", UnknownFileType.INSTANCE, "oops")
        val pluginConfiguration = BlackConnectProjectSettings.getInstance(myFixture.project)
        pluginConfiguration.enableJupyterSupport = true

        CodeReformatter(myFixture.project).isFileSupported(unknownFile)
    }
}