package me.lensvol.blackconnect.mocks

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import me.lensvol.blackconnect.DocumentUtil

class DocumentUtilMock(project: Project) : DocumentUtil(project) {
    private var imitateUndoRedo: Boolean = false

    init {
        resetUndoRedo()
    }

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

    override fun isInUndoRedo(): Boolean {
        return imitateUndoRedo
    }

    private fun resetUndoRedo() {
        imitateUndoRedo = false
    }

    fun enterUndo() {
        imitateUndoRedo = true
    }
}
