package me.lensvol.blackconnect.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import javax.swing.table.AbstractTableModel

data class BlackdExecutableVariant(val source: String, val path: String)

class ExecutableVariantsDialog(
    val project: Project?,
    val variants: List<BlackdExecutableVariant>,
    val resultHandler: (BlackdExecutableVariant) -> Unit
) :
    DialogWrapper(project) {
    private val columnNames = arrayOf("Source", "Path")
    private val tableModel = object : AbstractTableModel() {
        override fun getRowCount(): Int = variants.size

        override fun getColumnCount(): Int = columnNames.size

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val row = variants[rowIndex]
            return if (columnIndex == 0) {
                row.source
            } else {
                row.path
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

        override fun getColumnName(column: Int): String = columnNames[column]
    }
    val table = JBTable(tableModel).apply {
        setSelectionMode(SINGLE_SELECTION)
    }

    init {
        super.init()
        title = "Choose Instance"
    }

    override fun createActions(): Array<Action> {
        return arrayOf(object : DialogWrapperExitAction("OK", OK_EXIT_CODE) {
            override fun doAction(e: ActionEvent?) {
                resultHandler(variants[table.selectedRow])
                super.doAction(e)
            }
        })
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            layout = BorderLayout()
            val scrollPane = JScrollPane(table)
            add(scrollPane)
        }
    }
}
