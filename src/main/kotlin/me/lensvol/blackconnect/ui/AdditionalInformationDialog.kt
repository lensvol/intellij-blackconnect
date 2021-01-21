package me.lensvol.blackconnect.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class AdditionalInformationDialog(project: Project?, additionalInformation: String) : DialogWrapper(project) {
    private val textToDisplay = additionalInformation

    init {
        super.init()
        title = "Additional Information"
    }

    override fun createActions(): Array<Action> {
        return arrayOf(object : DialogWrapperExitAction("OK", OK_EXIT_CODE) {})
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            val textPane = JTextArea()
            textPane.text = textToDisplay
            textPane.isEditable = false
            add(textPane)
        }
    }
}
