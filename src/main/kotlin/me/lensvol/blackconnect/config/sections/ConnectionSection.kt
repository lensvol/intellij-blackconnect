package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.BlackdClient
import me.lensvol.blackconnect.Constants
import me.lensvol.blackconnect.Failure
import me.lensvol.blackconnect.Success
import me.lensvol.blackconnect.config.DEFAULT_BLACKD_PORT
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.disableContents
import me.lensvol.blackconnect.ui.enableContents
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ConnectionSection(project: Project) : ConfigSection(project) {
    private val localPortModel = SpinnerNumberModel(DEFAULT_BLACKD_PORT, 1, 65535, 1)

    private val hostnameText = JTextField("127.0.0.1")
    private val remotePortSpinner = JSpinner(localPortModel)

    private val checkConnectionButton = JButton("Check connection")

    private val remoteConnectionPanel = createRemoteConnectionPanel()

    override val panel: JPanel

    init {
        remotePortSpinner.editor = JSpinner.NumberEditor(remotePortSpinner, "#")

        this.panel = createPanel()
        installUiListeners()
    }

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = GridBagLayout()
            border = IdeBorderFactory.createTitledBorder("Connection settings")
            alignmentX = Component.LEFT_ALIGNMENT

            val constraints = GridBagConstraints().apply {
                anchor = GridBagConstraints.NORTHWEST
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                gridwidth = GridBagConstraints.REMAINDER
            }

            constraints.gridy = 0
            add(remoteConnectionPanel, constraints)

            constraints.gridy = 1
            add(checkConnectionButton, constraints)
        }
    }

    private fun createRemoteConnectionPanel(): JPanel {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Hostname:", hostnameText)
            .addComponent(Box.createRigidArea(Dimension(6, 0)) as JComponent)
            .addLabeledComponent("Port:", remotePortSpinner)
            .panel
            .apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
            }
    }

    private fun installUiListeners() {
        hostnameText.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                disableButtonIfNeeded(e)
            }

            override fun insertUpdate(e: DocumentEvent?) {
                disableButtonIfNeeded(e)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                disableButtonIfNeeded(e)
            }

            fun disableButtonIfNeeded(e: DocumentEvent?) {
                e?.document?.apply {
                    checkConnectionButton.isEnabled = getText(0, length).isNotEmpty()
                }
            }
        })

        checkConnectionButton.addActionListener {
            val blackdClient = BlackdClient(hostnameText.text, remotePortSpinner.value as Int)

            when (val result = blackdClient.checkConnection()) {
                is Success -> Messages.showInfoMessage(
                    this.panel,
                    "It works!<br><br><b>blackd</b> version: ${result.value}",
                    "Connection Status"
                )
                is Failure -> Messages.showErrorDialog(
                    this.panel,
                    "Cannot connect to <b>blackd</b>:<br><br><b>${result.reason}</b>"
                )
            }
        }

    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        hostnameText.text = projectConfig.hostname
        remotePortSpinner.value = projectConfig.port
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        projectConfig.hostname = hostnameText.text.ifBlank { "localhost" }
        projectConfig.port = remotePortSpinner.value as Int
    }

    override fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return hostnameText.text != projectConfig.hostname ||
            remotePortSpinner.value != projectConfig.port
    }
}
