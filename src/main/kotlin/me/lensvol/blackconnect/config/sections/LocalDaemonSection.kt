package me.lensvol.blackconnect.config.sections

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import me.lensvol.blackconnect.BlackdExecutor
import me.lensvol.blackconnect.ExecutionResult
import me.lensvol.blackconnect.config.DEFAULT_BLACKD_PORT
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.AdditionalInformationDialog
import me.lensvol.blackconnect.ui.disableContents
import me.lensvol.blackconnect.ui.enableContents
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.concurrent.thread

class LocalDaemonSection(val project: Project) : ConfigSection(project) {
    private val startLocalServerCheckbox = JCheckBox("Start local blackd instance when plugin loads")
    private val remotePortModel = SpinnerNumberModel(DEFAULT_BLACKD_PORT, 1, 65535, 1)
    private val bindOnHostnameText = JTextField("127.0.0.1")
    private val localPortSpinner = JSpinner(remotePortModel)
    private val blackdExecutableChooser = TextFieldWithBrowseButton().apply {
        val fileChooserDescriptor = FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        )
        addBrowseFolderListener(TextBrowseFolderListener(fileChooserDescriptor))
    }
    private val startDaemonButton = JButton("Start", AllIcons.Actions.Execute)
    private val stopDaemonButton = JButton("Stop", AllIcons.Actions.Suspend)

    private val blackdExecutor = service<BlackdExecutor>()

    private val localServerPanel = createLocalServerPanel()

    init {
        localPortSpinner.editor = JSpinner.NumberEditor(localPortSpinner, "#")

        localServerPanel.disableContents()

        installUiListeners()
    }

    override val panel: JPanel = createPanel()

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = GridBagLayout()
            border = IdeBorderFactory.createTitledBorder("Local Instance")
            alignmentX = Component.LEFT_ALIGNMENT

            val constraints = GridBagConstraints().apply {
                anchor = GridBagConstraints.NORTHWEST
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                gridwidth = GridBagConstraints.REMAINDER
            }

            constraints.gridy = 0
            add(startLocalServerCheckbox, constraints)

            constraints.gridy = 1
            add(localServerPanel, constraints)
        }
    }

    private fun createLocalServerPanel(): JPanel {
        val bindingSettingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Bind on:    ", bindOnHostnameText)
            .addComponent(Box.createRigidArea(Dimension(6, 0)) as JComponent)
            .addLabeledComponent("Port:", localPortSpinner)
            .panel

        val localServerPanel = JPanel().apply {
            layout = GridBagLayout()
            alignmentX = Component.LEFT_ALIGNMENT

            val constraints = GridBagConstraints().apply {
                anchor = GridBagConstraints.NORTHWEST
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
                gridwidth = GridBagConstraints.REMAINDER
            }

            constraints.gridy = 0
            add(bindingSettingsPanel, constraints)

            constraints.gridy = 1
            add(
                FormBuilder.createFormBuilder()
                    // FIXME: Do it properly with insets
                    .addLabeledComponent("Path:      ", blackdExecutableChooser)
                    .panel,
                constraints
            )

            constraints.gridy = 2
            constraints.gridx = 0
            constraints.gridwidth = GridBagConstraints.RELATIVE
            add(startDaemonButton, constraints)

            constraints.gridx = 1
            constraints.gridwidth = GridBagConstraints.REMAINDER
            add(stopDaemonButton, constraints)
        }

        bindingSettingsPanel.layout = BoxLayout(bindingSettingsPanel, BoxLayout.X_AXIS)

        return localServerPanel
    }

    private fun installUiListeners() {
        blackdExecutableChooser.textField.document.addDocumentListener(object : DocumentListener {
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
                updateButtonState()
            }
        })

        startLocalServerCheckbox.addItemListener {
            if(startLocalServerCheckbox.isSelected) {
                localServerPanel.enableContents()
            } else {
                localServerPanel.disableContents()
            }
            updateButtonState()
        }

        startDaemonButton.addActionListener {
            // FIXME: Maybe there is a primitive already for a delayed one-off jobs?
            thread {
                val executionResult = blackdExecutor.startDaemon(
                    blackdExecutableChooser.text,
                    bindOnHostnameText.text,
                    localPortSpinner.value as Int
                )

                if (executionResult is ExecutionResult.Failed) {
                    invokeLater(modalityState = ModalityState.any()) {
                        AdditionalInformationDialog(project, executionResult.reason).show()
                    }
                }

                updateButtonState()
            }
        }

        stopDaemonButton.addActionListener {
            thread {
                blackdExecutor.stopDaemon()
                updateButtonState()
            }
        }
    }

    private fun updateButtonState() {
        if (startLocalServerCheckbox.isSelected) {
            startDaemonButton.isEnabled = !blackdExecutor.isRunning() && bindOnHostnameText.text.isNotEmpty()
            stopDaemonButton.isEnabled = !startDaemonButton.isEnabled
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        localPortSpinner.value = globalConfig.bindOnPort
        bindOnHostnameText.text = globalConfig.bindOnHostname
        blackdExecutableChooser.text = globalConfig.blackdBinaryPath

        if (globalConfig.spawnBlackdOnStartup) {
            startLocalServerCheckbox.doClick()
            startLocalServerCheckbox.isSelected = true
            updateButtonState()
        }
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        globalConfig.spawnBlackdOnStartup = startLocalServerCheckbox.isSelected
        globalConfig.bindOnHostname = bindOnHostnameText.text.ifBlank { "localhost" }
        globalConfig.bindOnPort = localPortSpinner.value as Int
        globalConfig.blackdBinaryPath = blackdExecutableChooser.text
    }

    override fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return localPortSpinner.value != globalConfig.bindOnPort ||
            bindOnHostnameText.text != globalConfig.bindOnHostname ||
            blackdExecutableChooser.text != globalConfig.blackdBinaryPath ||
            startLocalServerCheckbox.isSelected != globalConfig.spawnBlackdOnStartup
    }
}