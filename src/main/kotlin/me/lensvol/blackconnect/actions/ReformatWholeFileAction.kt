package me.lensvol.blackconnect.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackdResponse
import me.lensvol.blackconnect.CodeReformatter
import me.lensvol.blackconnect.DocumentUtil
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationGroupManager

class ReformatWholeFileAction : AnAction(), DumbAware {
    companion object {
        private val logger = Logger.getInstance(ReformatWholeFileAction::class.java.name)

        private fun showError(currentProject: Project, text: String) {
            NotificationGroupManager.mainGroup()
                .createNotification(text, NotificationType.ERROR)
                .setTitle("BlackConnect")
                .notify(currentProject)
        }

        fun reformatWholeDocument(
            fileName: String,
            project: Project,
            document: Document
        ) {
            val configuration = BlackConnectProjectSettings.getInstance(project)
            val codeReformatter = CodeReformatter(project, configuration)
            codeReformatter.process(
                fileName,
                document.text,
                fileName.endsWith(".pyi")
            ) { response ->
                when (response) {
                    is BlackdResponse.Blackened -> {
                        DocumentUtil.updateCodeInDocument(project, document) {
                            logger.debug("Code is going to be updated in $document")
                            document.setText(response.sourceCode)
                        }
                    }
                    BlackdResponse.NoChangesMade -> {
                    }
                    is BlackdResponse.SyntaxError -> {
                        if (configuration.showSyntaxErrorMsgs) {
                            showError(project, "Source code contained syntax errors.")
                        }
                    }
                    is BlackdResponse.InternalError -> showError(project, "Internal error, please see blackd output.")
                    is BlackdResponse.UnknownStatus -> showError(
                        project,
                        "Something unexpected happened:\n${response.responseText}"
                    )
                }
            }
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return

        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(editor.document)
        val fileName = vFile?.name ?: "unknown"
        reformatWholeDocument(fileName, project, editor.document)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = false

        val project: Project = event.project ?: return
        val vFile: VirtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val configuration = BlackConnectProjectSettings.getInstance(project)

        event.presentation.isEnabled = CodeReformatter(
            project,
            configuration
        ).isFileSupported(vFile)
    }
}