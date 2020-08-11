package me.lensvol.blackconnect

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import me.lensvol.blackconnect.actions.ReformatWholeFileAction
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

class FileSaveListener(project: Project) : FileDocumentManagerListener {
    private val currentProject: Project = project
    private val codeReformatter = project.service<CodeReformatter>()

    override fun beforeDocumentSaving(document: Document) {
        val configuration = BlackConnectProjectSettings.getInstance(currentProject)
        if (!configuration.triggerOnEachSave) {
            return
        }

        val vFile = FileDocumentManager.getInstance().getFile(document) ?: return
        /*
        When document is going to be saved, it will be passed to each instance
        of the listener and they may each reformat it according to their projects.

        Following line will ensure that we reformat code only if it belongs to
        one of the modules in project associated with this instance.
         */
        ModuleUtil.findModuleForFile(vFile, currentProject) ?: return
        if (codeReformatter.isFileSupported(vFile)) {
            ReformatWholeFileAction.reformatWholeDocument(vFile.name, currentProject, document)
        }
    }
}
