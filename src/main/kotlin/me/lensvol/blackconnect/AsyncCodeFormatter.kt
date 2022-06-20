package me.lensvol.blackconnect

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager
import java.util.EnumSet

class AsyncCodeFormatter: AsyncDocumentFormattingService() {
    override fun getFeatures(): MutableSet<FormattingService.Feature> {
        return EnumSet.of(FormattingService.Feature.FORMAT_FRAGMENTS)
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
                var source = formattingRequest.documentText
                val ranges = formattingRequest.formattingRanges.toMutableList()
                ranges.sortByDescending { range -> range.endOffset }
                ranges.map { range ->
                    val sliceRange = IntRange(range.startOffset, range.endOffset)
                    val response = formatter.reformatFragment(
                        source.slice(sliceRange),
                        formattingRequest.ioFile?.name?.endsWith("pyi") ?: false
                    )
                    val blackened = response as? BlackdResponse.Blackened ?: return
                    source = source.replaceRange(sliceRange, blackened.sourceCode)
                }
                formattingRequest.onTextReady(source)
            }

            override fun cancel(): Boolean = false

            override fun isRunUnderProgress(): Boolean = false
        }
    }

    override fun getNotificationGroupId(): String = NotificationManager.NOTIFICATION_GROUP_ID

    override fun getName(): String = "BlackConnect"
}