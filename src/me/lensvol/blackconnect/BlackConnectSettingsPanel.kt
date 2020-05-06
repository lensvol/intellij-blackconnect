package me.lensvol.blackconnect

import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel


class BlackConnectSettingsPanel : JPanel() {
    private val hostnameText = JTextField("127.0.0.1")
    private val portSpinnerModel = SpinnerNumberModel(9000, 1, 65535, 1)
    private val portSpinner = JSpinner(portSpinnerModel)

    init {
        portSpinner.editor = JSpinner.NumberEditor(portSpinner, "#")

        layout = BorderLayout()
        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)
        val contentPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Hostname:", hostnameText)
                .addLabeledComponent("Port:", portSpinner)
                .panel

        add(contentPanel, BorderLayout.NORTH)
    }

    fun apply(configuration: BlackConnectSettingsConfiguration) {
        configuration.hostname = hostnameText.text
        configuration.port = portSpinner.value as Int
    }

    fun load(configuration: BlackConnectSettingsConfiguration) {
        hostnameText.text = configuration.hostname
        portSpinner.value = configuration.port
    }
}