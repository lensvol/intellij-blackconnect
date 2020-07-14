package me.lensvol.blackconnect.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.CodeReformatter
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

class FormatUsingBlackdAction : AnAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.let { project ->
            FileEditorManagerEx.getInstance(project).selectedTextEditor?.let { editor ->
                val configuration = BlackConnectProjectSettings.getInstance(project)
                CodeReformatter(project, configuration).process(editor.document)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.project
        event.presentation.isEnabled = false

        if (project == null)
            return

        val configuration = BlackConnectProjectSettings.getInstance(project)
        val vFile: VirtualFile? = event.getData(PlatformDataKeys.VIRTUAL_FILE)
        vFile?.let {
            event.presentation.isEnabled = CodeReformatter(
                project,
                configuration
            ).isFileSupported(vFile)
        }
    }
}