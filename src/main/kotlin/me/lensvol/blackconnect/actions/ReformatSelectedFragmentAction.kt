package me.lensvol.blackconnect.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackdResponse
import me.lensvol.blackconnect.CodeReformatter
import me.lensvol.blackconnect.DocumentUtil
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationGroupManager

class ReformatSelectedFragmentAction : AnAction(), DumbAware {
    private val logger = Logger.getInstance(ReformatSelectedFragmentAction::class.java.name)

    private fun showError(currentProject: Project, text: String) {
        NotificationGroupManager.mainGroup()
            .createNotification(text, NotificationType.ERROR)
            .setTitle("BlackConnect")
            .notify(currentProject)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return
        val configuration = BlackConnectProjectSettings.getInstance(project)
        val hintManager = HintManager.getInstance()
        val codeReformatter = CodeReformatter(project, configuration)

        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(editor.document)
        val fileName = vFile?.name ?: "unknown"

        val primaryCaret = editor.caretModel.primaryCaret
        val selectionStart = primaryCaret.selectionStart
        val selectionEnd = if (primaryCaret.selectionEndPosition.column == 0) {
            primaryCaret.selectionEnd - 1
        } else {
            primaryCaret.selectionEnd
        }

        logger.debug("Reformatting fragment $selectionStart-$selectionStart of '$fileName'")

        val fragment = editor.document.getText(TextRange.create(selectionStart, selectionEnd))
        codeReformatter.processFragment(fileName, fragment, fileName.endsWith(".pyi")) { response ->
            when (response) {
                is BlackdResponse.Blackened -> {
                    DocumentUtil.updateCodeInDocument(project, editor.document) {
                        editor.document.replaceString(selectionStart, selectionEnd, response.sourceCode)
                    }
                }
                BlackdResponse.NoChangesMade -> {
                    ApplicationManager.getApplication().invokeLater {
                        hintManager.showInformationHint(editor, "No changes were needed.")
                    }
                }
                is BlackdResponse.SyntaxError -> {
                    ApplicationManager.getApplication().invokeLater {
                        hintManager.showErrorHint(editor, "Fragment has invalid syntax.")
                    }
                }
                is BlackdResponse.InternalError -> showError(project, "Internal error, please see blackd output.")
                is BlackdResponse.UnknownStatus -> showError(project, "Something unexpected happened:\n${response.responseText}")
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = false

        val project: Project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return

        event.presentation.isEnabled = editor.selectionModel.hasSelection()
    }
}