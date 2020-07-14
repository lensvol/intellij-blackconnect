package me.lensvol.blackconnect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable

class DocumentUtil {
    companion object {
        private val logger = Logger.getInstance(DocumentUtil::class.java.name)

        fun updateCodeInDocument(
            project: Project,
            document: Document,
            sourceCode: String
        ) {
            with(ApplicationManager.getApplication()) {
                invokeLater {
                    runWriteAction(Computable {
                        CommandProcessor.getInstance().executeCommand(
                            project,
                            {
                                logger.debug("Code is going to be updated in $document")
                                document.setText(sourceCode)
                            },
                            "Reformat code using blackd",
                            null,
                            UndoConfirmationPolicy.DEFAULT,
                            document
                        )
                    })
                }
            }
        }
    }
}