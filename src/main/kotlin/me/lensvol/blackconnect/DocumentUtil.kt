package me.lensvol.blackconnect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable

class DocumentUtil {
    companion object {
        fun updateCodeInDocument(
            project: Project,
            document: Document,
            receiver: () -> Unit
        ) {
            with(ApplicationManager.getApplication()) {
                invokeLater {
                    runWriteAction(
                        Computable {
                            CommandProcessor.getInstance().executeCommand(
                                project,
                                {
                                    receiver()
                                },
                                "Reformat code using blackd",
                                null,
                                UndoConfirmationPolicy.DEFAULT,
                                document
                            )
                        }
                    )
                }
            }
        }
    }
}
