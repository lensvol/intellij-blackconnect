package me.lensvol.blackconnect.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.CodeReformatter
import me.lensvol.blackconnect.DocumentUtil
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

class FormatUsingBlackdAction : AnAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return
        val configuration = BlackConnectProjectSettings.getInstance(project)

        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(editor.document)
        val fileName = vFile?.name ?: "unknown"

        CodeReformatter(project, configuration).process(
            fileName,
            editor.document.text,
            fileName.endsWith(".pyi")
        ) { reformatted -> DocumentUtil.updateCodeInDocument(project, editor.document, reformatted) }
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