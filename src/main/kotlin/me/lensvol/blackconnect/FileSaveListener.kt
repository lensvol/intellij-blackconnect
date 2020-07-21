package me.lensvol.blackconnect

import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
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

            Following line will reformat code only if it belongs to one of the modules
            in project associated with this instance.
             */
            ModuleUtil.findModuleForFile(vFile, currentProject) ?: return

            val reformatter = CodeReformatter(currentProject, configuration)
            if (!reformatter.isFileSupported(file)) {
                return
            }
            reformatter.process(vFile.name, document.text, vFile.name.endsWith(".pyi")) { response ->
                when (response) {
                    is BlackdResponse.Blackened -> {
                        DocumentUtil.updateCodeInDocument(currentProject, document) {
                            document.setText(response.sourceCode)
                        }
                    }
                    BlackdResponse.NoChangesMade -> {
                    }
                    is BlackdResponse.SyntaxError -> {
                        if (configuration.showSyntaxErrorMsgs) {
                            showError(currentProject, "Source code contained syntax errors.")
                        }
                    }
                    is BlackdResponse.InternalError -> showError(
                        currentProject,
                        "Internal error, please see blackd output."
                    )
                    is BlackdResponse.UnknownStatus -> showError(
                        currentProject,
                        "Something unexpected happened:\n${response.responseText}"
                    )
                }
            }
        }
    }
}