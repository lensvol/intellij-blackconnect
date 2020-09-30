package me.lensvol.blackconnect.config.sections

import com.intellij.openapi.ui.Messages
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.BlackdClient
import me.lensvol.blackconnect.Failure
import me.lensvol.blackconnect.Success
import me.lensvol.blackconnect.config.DEFAULT_BLACKD_PORT
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ConnectionSettingsSection : ConfigSection {
    private val hostnameText = JTextField("127.0.0.1")
    private val portSpinnerModel = SpinnerNumberModel(DEFAULT_BLACKD_PORT, 1, 65535, 1)
    private val portSpinner = JSpinner(portSpinnerModel)
    private val checkConnectionButton = JButton("Check connection")

    override val panel: JPanel

    init {
        portSpinner.editor = JSpinner.NumberEditor(portSpinner, "#")

        this.panel = createPanel()
        installUiListeners()
    }

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Connection settings")
            alignmentX = Component.LEFT_ALIGNMENT

            val panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Hostname:", hostnameText)
                .addComponent(Box.createRigidArea(Dimension(6, 0)) as JComponent)
                .addLabeledComponent("Port:", portSpinner)
                .panel

            panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

            add(checkConnectionButton, BorderLayout.SOUTH)
            add(
                panel,
                BorderLayout.NORTH
            )
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
            val blackdClient = BlackdClient(hostnameText.text, portSpinner.value as Int)

            when (val result = blackdClient.checkConnection()) {
                is Success -> Messages.showInfoMessage(
                    this.panel,
                    "It works!<br><br><b>blackd</b> version: ${result.value}",
                    "Connection status"
                )
                is Failure -> Messages.showErrorDialog(
                    this.panel,
                    "Cannot connect to <b>blackd</b>:<br><br><b>${result.reason}</b>"
                )
            }
        }
    }

    override fun loadFrom(configuration: BlackConnectProjectSettings) {
        hostnameText.text = configuration.hostname
        portSpinner.value = configuration.port
    }

    override fun saveTo(configuration: BlackConnectProjectSettings) {
        configuration.hostname = hostnameText.text.ifBlank { "localhost" }
        configuration.port = portSpinner.value as Int
    }

    override fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        return hostnameText.text != configuration.hostname ||
            portSpinner.value != configuration.port
    }
}