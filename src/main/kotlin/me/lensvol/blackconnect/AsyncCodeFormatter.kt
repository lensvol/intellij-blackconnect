package me.lensvol.blackconnect

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager
import java.util.EnumSet

class AsyncCodeFormatter: AsyncDocumentFormattingService() {
    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return EnumSet.noneOf(FormattingService.Feature::class.java)
    }

    override fun canFormat(source: PsiFile): Boolean {
        val project = source.project
        if (!BlackConnectProjectSettings.getInstance(project).triggerOnReformat) return false
        val file = source.virtualFile ?: return false
        return project.service<CodeReformatter>().isFileSupported(file)
    }

    override fun createFormattingTask(formattingRequest: AsyncFormattingRequest): FormattingTask {
        return object: FormattingTask {
            override fun run() {
                val formatter = formattingRequest.context.project.service<CodeReformatter>()
                val source = formattingRequest.documentText
                val response = formatter.reformatFragment(
                    source,
                    formattingRequest.ioFile?.name?.endsWith("pyi") ?: false
                )
                val blackened = response as? BlackdResponse.Blackened ?: return
                formattingRequest.onTextReady(blackened.sourceCode)
            }

            override fun cancel(): Boolean = false
        }
    }

    override fun getNotificationGroupId(): String = NotificationManager.NOTIFICATION_GROUP_ID

    override fun getName(): String = "BlackConnect"
}