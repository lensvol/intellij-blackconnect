package me.lensvol.blackconnect

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import me.lensvol.blackconnect.mocks.CodeReformatterMock
import me.lensvol.blackconnect.mocks.DocumentUtilMock
import me.lensvol.blackconnect.mocks.NotificationManagerMock
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager

abstract class BlackConnectTestCase : BasePlatformTestCase() {
    lateinit var mockNotificationManager: NotificationManagerMock
    lateinit var mockCodeReformatter: CodeReformatterMock
    lateinit var mockDocumentUtil: DocumentUtil
    lateinit var pluginConfiguration: BlackConnectProjectSettings

    override fun setUp() {
        super.setUp()

        pluginConfiguration = BlackConnectProjectSettings.getInstance(myFixture.project)

        setupMocks()
    }

    protected open fun setupMocks() {
        mockNotificationManager =
            NotificationManagerMock(myFixture.project)
        myFixture.project.replaceService(NotificationManager::class.java, mockNotificationManager, testRootDisposable)

        mockCodeReformatter = CodeReformatterMock(myFixture.project)
        myFixture.project.replaceService(CodeReformatter::class.java, mockCodeReformatter, testRootDisposable)

        mockDocumentUtil = DocumentUtilMock(myFixture.project)
        myFixture.project.replaceService(DocumentUtil::class.java, mockDocumentUtil, testRootDisposable)
    }

    protected fun eventForFile(file: VirtualFile): TestActionEvent {
        return TestActionEvent(
            DataContext { dataId ->
                when (dataId) {
                    PlatformDataKeys.VIRTUAL_FILE.name -> file
                    PlatformDataKeys.PROJECT.name -> myFixture.project
                    else -> null
                }
            }
        )
    }

    override fun getTestDataPath(): String {
        return System.getProperty("user.dir") + "/src/test/testData"
    }

    fun setupBlackdResponse(response: BlackdResponse) {
        mockCodeReformatter.setResponse(response)
    }

    fun openFileInEditor(filePath: String): VirtualFile {
        val file = myFixture.copyFileToProject(filePath)
        myFixture.openFileInEditor(file)
        return file
    }
}
