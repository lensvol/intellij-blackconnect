package me.lensvol.blackconnect

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project

class FileSaveListener(project: Project) : FileDocumentManagerListener {
    private val currentProject: Project = project

    override fun beforeDocumentSaving(document: Document) {
        val configuration = BlackConnectSettingsConfiguration.getInstance(currentProject)
        if (!configuration.triggerOnEachSave) {
            return
        }

        val vFile = FileDocumentManager.getInstance().getFile(document)
        vFile?.let {file ->
            val reformatter = BlackdReformatter(currentProject, configuration)
            if (reformatter.isFileSupported(file)) {
                reformatter.process(document)
            }
        }
    }
}