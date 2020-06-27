package me.lensvol.blackconnect.config

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.moandjiezana.toml.Toml
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.BufferedReader
import java.util.Collections
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel

const val PYPROJECT_TOML: String = "pyproject.toml"
const val DEFAULT_LINE_LENGTH: Long = 88
const val DEFAULT_BLACKD_PORT: Int = 45484

class BlackConnectSettingsPanel(project: Project) : JPanel() {
    private val hostnameText = JTextField("127.0.0.1")

    private val portSpinnerModel = SpinnerNumberModel(DEFAULT_BLACKD_PORT, 1, 65535, 1)
    private val portSpinner = JSpinner(portSpinnerModel)

    private val lineLengthModel = SpinnerNumberModel(DEFAULT_LINE_LENGTH, 45, 255, 1)
    private val lineLengthSpinner = JSpinner(lineLengthModel)

    private val fastModeCheckbox = JCheckBox("Skip sanity checks")
    private val skipStringNormalCheckbox = JCheckBox("Skip string normalization")
    private val targetSpecificVersionsCheckbox = JCheckBox("Target specific Python versions")

    private val triggerOnEachSave = JCheckBox("Trigger when saving changed files")

    private val targetVersions = mapOf(
        "py27" to "2.7",
        "py33" to "3.3",
        "py34" to "3.4",
        "py35" to "3.5",
        "py36" to "3.6",
        "py37" to "3.7",
        "py38" to "3.8"
    )

    private val loadPyprojectTomlButton = JButton("Load from pyproject.toml")

    private val versionCheckboxes = sortedMapOf<String, JCheckBox>().apply {
        targetVersions.values.map { version ->
            this.put("py$version", JCheckBox(version))
        }
    }

    private val jupyterSupportCheckbox = JCheckBox("Enable Jupyter Notebook support (whole file only)")
    private val showSyntaxErrorMsgsCheckbox = JCheckBox("Show notifications about syntax errors")

    init {
        loadPyprojectTomlButton.isEnabled = true
        loadPyprojectTomlButton.addActionListener {
            val pyprojectTomlDescriptor = createPyprojectSpecificDescriptor()
            val candidates =
                FilenameIndex.getVirtualFilesByName(project, "pyproject.toml", GlobalSearchScope.projectScope(project))

            FileChooser.chooseFile(pyprojectTomlDescriptor, project, parent, candidates.firstOrNull()) { file ->
                val contents = file.inputStream.bufferedReader().use(BufferedReader::readText)
                processPyprojectToml(contents)
            }
        }

        layout = GridBagLayout()
        border = IdeBorderFactory.createEmptyBorder(UIUtil.PANEL_SMALL_INSETS)

        val constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.NORTHWEST
        constraints.weightx = 1.0
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER

        val targetVersionsPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT)
            border = IdeBorderFactory.createEmptyBorder(JBUI.insetsLeft(16))

            versionCheckboxes.values.forEach { checkbox ->
                this.add(checkbox)
            }
        }

        targetVersionsPanel.components.map { checkbox ->
            checkbox.isEnabled = false
        }

        targetSpecificVersionsCheckbox.addItemListener {
            targetVersionsPanel.components.map { checkbox ->
                checkbox.isEnabled = targetSpecificVersionsCheckbox.isSelected
            }
        }

        val formattingPanel = JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Formatting options")

            add(
                FormBuilder.createFormBuilder()
                    .addLabeledComponent("Line length:", lineLengthSpinner)
                    .addComponent(fastModeCheckbox)
                    .addComponent(skipStringNormalCheckbox)
                    .addComponent(targetSpecificVersionsCheckbox)
                    .addComponent(targetVersionsPanel)
                    .addComponent(loadPyprojectTomlButton)
                    .panel,
                BorderLayout.NORTH
            )
        }

        portSpinner.editor = JSpinner.NumberEditor(portSpinner, "#")

        val connectionSettingPanel = JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Connection settings")

            add(
                FormBuilder.createFormBuilder()
                    .addLabeledComponent("Hostname:", hostnameText)
                    .addLabeledComponent("Port:", portSpinner)
                    .panel,
                BorderLayout.NORTH
            )
        }

        val miscSettingsPanel = JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Miscellaneous settings")

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(jupyterSupportCheckbox)
                    .addComponent(showSyntaxErrorMsgsCheckbox)
                    .panel,
                BorderLayout.NORTH
            )
        }

        triggerOnEachSave.alignmentX = Component.LEFT_ALIGNMENT
        connectionSettingPanel.alignmentX = Component.LEFT_ALIGNMENT
        formattingPanel.alignmentX = Component.LEFT_ALIGNMENT
        miscSettingsPanel.alignmentX = Component.LEFT_ALIGNMENT

        add(triggerOnEachSave, constraints)
        add(connectionSettingPanel, constraints)
        add(formattingPanel, constraints)
        add(miscSettingsPanel, constraints)

        // Add empty filler to push our other panels to the top
        constraints.fill = GridBagConstraints.VERTICAL
        constraints.gridheight = GridBagConstraints.REMAINDER
        constraints.weighty = 2.0
        constraints.gridx = 0
        constraints.anchor = GridBagConstraints.NORTH
        add(JPanel(), constraints)
    }

    private fun createPyprojectSpecificDescriptor(): FileChooserDescriptor {
        val fileSpecificDescriptor = object : FileChooserDescriptor(true, false, false, false, false, false) {
            override fun isFileSelectable(file: VirtualFile?): Boolean {
                return super.isFileSelectable(file) && file?.name.equals(PYPROJECT_TOML)
            }

            override fun isFileVisible(file: VirtualFile?, showHiddenFiles: Boolean): Boolean {
                if (file == null) {
                    return false
                }

                if (!showHiddenFiles && FileElement.isFileHidden(file)) {
                    return false
                }

                return file.isDirectory || file.name == PYPROJECT_TOML
            }
        }
        fileSpecificDescriptor.isForcedToUseIdeaFileChooser = true
        return fileSpecificDescriptor
    }

    private fun processPyprojectToml(tomlContents: String) {
        val toml: Toml = Toml().read(tomlContents)
        if (!toml.contains("tool.black")) {
            return
        }

        val blackSettings = toml.getTable("tool.black")
        lineLengthSpinner.value =
            blackSettings.getLong("line-length", DEFAULT_LINE_LENGTH).toInt()
        val targetVersionsFromFile = blackSettings.getList<String>("target-version", Collections.emptyList())
        if (targetVersionsFromFile.count() > 0) {
            targetSpecificVersionsCheckbox.isSelected = true
            targetVersions.entries.map { entry ->
                versionCheckboxes["py" + entry.value]?.isSelected = targetVersionsFromFile.contains(entry.key)
            }
        } else {
            targetSpecificVersionsCheckbox.isSelected = false
        }
        skipStringNormalCheckbox.isSelected =
            blackSettings.getBoolean("skip-string-normalization", false)
        fastModeCheckbox.isSelected = blackSettings.getBoolean("fast", false)
    }

    fun apply(configuration: BlackConnectProjectSettings) {
        configuration.hostname = hostnameText.text.ifBlank { "localhost" }
        configuration.port = portSpinner.value as Int
        configuration.lineLength = lineLengthSpinner.value as Int
        configuration.fastMode = fastModeCheckbox.isSelected
        configuration.skipStringNormalization = skipStringNormalCheckbox.isSelected
        configuration.triggerOnEachSave = triggerOnEachSave.isSelected
        configuration.targetSpecificVersions = targetSpecificVersionsCheckbox.isSelected
        configuration.pythonTargets = generateVersionSpec()
        configuration.enableJupyterSupport = jupyterSupportCheckbox.isSelected
        configuration.showSyntaxErrorMsgs = showSyntaxErrorMsgsCheckbox.isSelected
    }

    fun load(configuration: BlackConnectProjectSettings) {
        hostnameText.text = configuration.hostname
        portSpinner.value = configuration.port
        lineLengthSpinner.value = configuration.lineLength
        fastModeCheckbox.isSelected = configuration.fastMode
        skipStringNormalCheckbox.isSelected = configuration.skipStringNormalization
        triggerOnEachSave.isSelected = configuration.triggerOnEachSave
        jupyterSupportCheckbox.isSelected = configuration.enableJupyterSupport
        showSyntaxErrorMsgsCheckbox.isSelected = configuration.showSyntaxErrorMsgs

        configuration.pythonTargets.split(",").forEach { version ->
            versionCheckboxes[version]?.isSelected = true
        }

        if (configuration.targetSpecificVersions) {
            // This is done to trigger dependent item change logic
            // and enable version checkboxes
            targetSpecificVersionsCheckbox.doClick()
            targetSpecificVersionsCheckbox.isSelected = true
        }
    }

    private fun generateVersionSpec(): String {
        return versionCheckboxes
            .filter { it.value.isSelected }
            .map { it.key }
            .joinToString(",")
    }

    fun isModified(configuration: BlackConnectProjectSettings): Boolean {
        return hostnameText.text != configuration.hostname ||
            portSpinner.value != configuration.port ||
            lineLengthSpinner.value != configuration.lineLength ||
            fastModeCheckbox.isSelected != configuration.fastMode ||
            skipStringNormalCheckbox.isSelected != configuration.skipStringNormalization ||
            triggerOnEachSave.isSelected != configuration.triggerOnEachSave ||
            targetSpecificVersionsCheckbox.isSelected != configuration.targetSpecificVersions ||
            generateVersionSpec() != configuration.pythonTargets ||
            jupyterSupportCheckbox.isSelected != configuration.enableJupyterSupport ||
            showSyntaxErrorMsgsCheckbox.isSelected != configuration.showSyntaxErrorMsgs
    }
}
