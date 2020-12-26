package me.lensvol.blackconnect.mocks

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import me.lensvol.blackconnect.DocumentUtil

class DocumentUtilMock(project: Project) : DocumentUtil(project) {
    override fun updateCodeInDocument(document: Document, receiver: () -> Unit) {
        val application = ApplicationManager.getApplication()

        with(application) {
            invokeAndWait {
                runWriteAction(
                    Computable {
                        receiver()
                    }
                )
            }
        }

    }
}

