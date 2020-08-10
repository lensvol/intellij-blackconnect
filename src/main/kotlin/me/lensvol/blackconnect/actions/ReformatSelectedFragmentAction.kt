package me.lensvol.blackconnect.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
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
import me.lensvol.blackconnect.ui.NotificationManager

class ReformatSelectedFragmentAction : AnAction(), DumbAware {
    private val logger = Logger.getInstance(ReformatSelectedFragmentAction::class.java.name)

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return

        val hintManager = HintManager.getInstance()
        val notificationService = project.service<NotificationManager>()
        val codeReformatter = project.service<CodeReformatter>()
        val documentUtil = project.service<DocumentUtil>()

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
                    documentUtil.updateCodeInDocument(editor.document) {
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
                is BlackdResponse.InternalError -> {
                    // In some cases trying to reformat syntactically fragment will give out error code 500 instead of
                    // 400. This is due to how Python tokenizer works. The only consistent case that I was able to find
                    // was with fragments like `   if (\n', which trigger error mentioned in the following condition.
                    // This heuristic should make it a little bit easier to see that problem is not with blackd per se.
                    if (response.reason.contains("EOF in multi-line statement")) {
                        ApplicationManager.getApplication().invokeLater {
                            hintManager.showErrorHint(editor, "Fragment has invalid syntax.")
                        }
                    } else {
                        notificationService.showError("Internal error, please see blackd output.")
                    }
                }
                is BlackdResponse.UnknownStatus -> notificationService.showError(
                    "Something unexpected happened:\n${response.responseText}"
                )
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
