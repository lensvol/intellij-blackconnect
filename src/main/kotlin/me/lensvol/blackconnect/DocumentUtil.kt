package me.lensvol.blackconnect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable

@Service
open class DocumentUtil(project: Project) {
    private val currentProject = project
    private val undoManager = UndoManager.getInstance(project)

    open fun updateCodeInDocument(
        document: Document,
        receiver: () -> Unit
    ) {
        val application = ApplicationManager.getApplication()

        with(application) {
            invokeLater {
                runWriteAction(
                    Computable {
                        CommandProcessor.getInstance().executeCommand(
                            currentProject,
                            {
                                receiver()
                            },
                            "Reformat Code Using Black",
                            null,
                            UndoConfirmationPolicy.DEFAULT,
                            document
                        )
                    }
                )
            }
        }
    }

    open fun isInUndoRedo(): Boolean = undoManager.isUndoOrRedoInProgress
}
