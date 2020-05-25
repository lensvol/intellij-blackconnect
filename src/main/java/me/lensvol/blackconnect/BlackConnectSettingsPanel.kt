package me.lensvol.blackconnect

import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*


class BlackConnectSettingsPanel : JPanel() {
    private val hostnameText = JTextField("127.0.0.1")

    private val portSpinnerModel = SpinnerNumberModel(45484, 1, 65535, 1)
    private val portSpinner = JSpinner(portSpinnerModel)

    private val lineLengthModel = SpinnerNumberModel(88, 45, 255, 1)
    private val lineLengthSpinner = JSpinner(lineLengthModel)

    private val fastModeCheckbox = JCheckBox("Skip sanity checks")
    private val skipStringNormalCheckbox = JCheckBox("Skip string normalization")

    private val triggerOnEachSave = JCheckBox("Trigger on each file save")

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)

        val formattingPanel = HorizontalFillPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Formatting options")

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(fastModeCheckbox)
                    .addComponent(skipStringNormalCheckbox)
                    .addLabeledComponent("Line length:", lineLengthSpinner)
                    .panel,
                BorderLayout.NORTH
            )
        }

        portSpinner.editor = JSpinner.NumberEditor(portSpinner, "#")

        val connectionSettingPanel = HorizontalFillPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Connection settings")

            add(
                FormBuilder.createFormBuilder()
                    .addLabeledComponent("Hostname:", hostnameText)
                    .addLabeledComponent("Port:", portSpinner)
                    .addComponent(formattingPanel)
                    .panel,
                BorderLayout.NORTH
            )
        }

        triggerOnEachSave.alignmentX = Component.LEFT_ALIGNMENT
        connectionSettingPanel.alignmentX = Component.LEFT_ALIGNMENT
        formattingPanel.alignmentX = Component.LEFT_ALIGNMENT

        add(triggerOnEachSave)
        add(connectionSettingPanel)
        add(formattingPanel)
    }

    fun apply(configuration: BlackConnectSettingsConfiguration) {
        configuration.hostname = hostnameText.text.ifBlank { "localhost" }
        configuration.port = portSpinner.value as Int
        configuration.lineLength = lineLengthSpinner.value as Int
        configuration.fastMode = fastModeCheckbox.isSelected
        configuration.skipStringNormalization = skipStringNormalCheckbox.isSelected
        configuration.triggerOnEachSave = triggerOnEachSave.isSelected
    }

    fun load(configuration: BlackConnectSettingsConfiguration) {
        hostnameText.text = configuration.hostname
        portSpinner.value = configuration.port
        lineLengthSpinner.value = configuration.lineLength
        fastModeCheckbox.isSelected = configuration.fastMode
        skipStringNormalCheckbox.isSelected = configuration.skipStringNormalization
        triggerOnEachSave.isSelected = configuration.triggerOnEachSave
    }

    fun isModified(configuration: BlackConnectSettingsConfiguration): Boolean {
        return hostnameText.text != configuration.hostname ||
                portSpinner.value != configuration.port ||
                lineLengthSpinner.value != configuration.lineLength ||
                fastModeCheckbox.isSelected != configuration.fastMode ||
                skipStringNormalCheckbox.isSelected != configuration.skipStringNormalization ||
                triggerOnEachSave.isSelected != configuration.triggerOnEachSave
    }
}

class HorizontalFillPanel : JPanel() {
    override fun getMaximumSize(): Dimension {
        return Dimension(super.getMaximumSize().width, super.getPreferredSize().height)
    }
}
