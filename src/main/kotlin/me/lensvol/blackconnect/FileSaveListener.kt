package me.lensvol.blackconnect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings

class FileSaveListener(project: Project) : FileDocumentManagerListener {
    private val currentProject: Project = project

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

            Following line will reformat code only if it belongs to one of the modules
            in project associated with this instance.
             */
            ModuleUtil.findModuleForFile(vFile, currentProject) ?: return

            val reformatter = CodeReformatter(currentProject, configuration)
            if (reformatter.isFileSupported(file)) {
                reformatter.process(vFile.name, document.text, vFile.name.endsWith(".pyi")) { reformatted ->
                    DocumentUtil.updateCodeInDocument(currentProject, document) {
                        document.setText(reformatted)
                    }
                }
            }
        }
    }
}