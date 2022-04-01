package me.lensvol.blackconnect.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

const val DEFAULT_COLUMN_WIDTH = 120

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
            layout = BorderLayout()

            val textPane = JTextArea()
            textPane.text = textToDisplay
            textPane.columns = DEFAULT_COLUMN_WIDTH
            textPane.lineWrap = true
            textPane.isEditable = false

            val scrollPane = JScrollPane(textPane)
            add(scrollPane)
        }
    }
}
