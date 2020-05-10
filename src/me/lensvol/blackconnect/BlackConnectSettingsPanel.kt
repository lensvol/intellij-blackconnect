package me.lensvol.blackconnect

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.layout.selected
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.*


class BlackConnectSettingsPanel : JPanel() {
    private val hostnameText = JTextField("127.0.0.1")
    private val portSpinnerModel = SpinnerNumberModel(45484, 1, 65535, 1)
    private val portSpinner = JSpinner(portSpinnerModel)
    private val fastModeCheckbox = JCheckBox()

    private val lineLengthModel = SpinnerNumberModel(88, 45, 255, 1)
    private val lineLengthSpinner = JSpinner(lineLengthModel)

    init {
        portSpinner.editor = JSpinner.NumberEditor(portSpinner, "#")

        layout = BorderLayout()
        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)
        val contentPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Hostname:", hostnameText)
                .addLabeledComponent("Port:", portSpinner)
                .addLabeledComponent("Line length:", lineLengthSpinner)
                .addLabeledComponent("Skip sanity checks:", fastModeCheckbox)
                .panel

        add(contentPanel, BorderLayout.NORTH)
    }

    fun apply(configuration: BlackConnectSettingsConfiguration) {
        configuration.hostname = hostnameText.text.ifBlank { "localhost" }
        configuration.port = portSpinner.value as Int
        configuration.lineLength = lineLengthSpinner.value as Int
        configuration.fastMode = fastModeCheckbox.isSelected
    }

    fun load(configuration: BlackConnectSettingsConfiguration) {
        hostnameText.text = configuration.hostname
        portSpinner.value = configuration.port
        lineLengthSpinner.value = configuration.lineLength
        fastModeCheckbox.isSelected = configuration.fastMode
    }

    fun isModified(configuration: BlackConnectSettingsConfiguration): Boolean {
        return hostnameText.text != configuration.hostname ||
                portSpinner.value != configuration.port ||
                lineLengthSpinner.value != configuration.lineLength ||
                fastModeCheckbox.isSelected != configuration.fastMode
    }
}