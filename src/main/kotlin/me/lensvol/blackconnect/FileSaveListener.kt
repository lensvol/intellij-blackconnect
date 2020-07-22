package me.lensvol.blackconnect

import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationGroupManager

class FileSaveListener(project: Project) : FileDocumentManagerListener {
    private val currentProject: Project = project

    private fun showError(currentProject: Project, text: String) {
        NotificationGroupManager.mainGroup()
            .createNotification(text, NotificationType.ERROR)
            .setTitle("BlackConnect")
            .notify(currentProject)
    }

    override fun beforeDocumentSaving(document: Document) {
        val configuration = BlackConnectProjectSettings.getInstance(currentProject)
        if (!configuration.triggerOnEachSave) {
            return
        }

        val vFile = FileDocumentManager.getInstance().getFile(document)
        vFile?.let { file ->
            /*
            When document is going to be saved, it will be passed to each instance
            of the listener and they may each reformat it according to their projects.

            Following line will ensure that we reformat code only if it belongs to
            one of the modules in project associated with this instance.
             */
            ModuleUtil.findModuleForFile(vFile, currentProject) ?: return
            ReformatWholeFileAction.reformatWholeDocument(file.name, currentProject, document)
        }
    }
}