package me.lensvol.blackconnect

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import me.lensvol.blackconnect.mocks.CodeReformatterMock
import me.lensvol.blackconnect.mocks.NotificationManagerMock
import me.lensvol.blackconnect.ui.NotificationManager

abstract class BlackConnectTestCase : BasePlatformTestCase() {
    lateinit var mockNotificationManager: NotificationManagerMock
    lateinit var mockCodeReformatter: CodeReformatterMock

    override fun setUp() {
        super.setUp()

        setupMocks()
    }

    protected open fun setupMocks() {
        mockNotificationManager =
            NotificationManagerMock(myFixture.project)
        myFixture.project.replaceService(NotificationManager::class.java, mockNotificationManager, testRootDisposable)

        mockCodeReformatter = CodeReformatterMock(myFixture.project)
        myFixture.project.replaceService(CodeReformatter::class.java, mockCodeReformatter, testRootDisposable)
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
}
