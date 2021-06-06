package me.lensvol.blackconnect.config.sections

import com.intellij.application.options.CodeStyle
import com.intellij.lang.Language
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.moandjiezana.toml.Toml
import me.lensvol.blackconnect.config.DEFAULT_LINE_LENGTH
import me.lensvol.blackconnect.config.PYPROJECT_TOML
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.io.BufferedReader
import java.util.Collections
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class FormattingSection(private val project: Project) : ConfigSection(project) {

    private val lineLengthModel = SpinnerNumberModel(DEFAULT_LINE_LENGTH, 45, 255, 1)
    private val lineLengthSpinner = JSpinner(lineLengthModel)

    private val fastModeCheckbox = JCheckBox("Skip sanity checks")
    private val skipStringNormalCheckbox = JCheckBox("Skip string normalization")
    private val skipMagicTrailingCommaCheckbox = JCheckBox("Don't use trailing commas as a reason to split lines")
    private val targetSpecificVersionsCheckbox = JCheckBox("Target specific Python versions")

    private val targetVersions = linkedMapOf(
        "py27" to "2.7",
        "py33" to "3.3",
        "py34" to "3.4",
        "py35" to "3.5",
        "py36" to "3.6",
        "py37" to "3.7",
        "py38" to "3.8",
        "py39" to "3.9"
    )

    private val versionCheckboxes = linkedMapOf<String, JCheckBox>().apply {
        targetVersions.values.map { version ->
            this.put("py$version", JCheckBox(version))
        }
    }

    override val panel: JPanel by lazy { createPanel() }

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
            Messages.showErrorDialog(this.panel, "<b>[tool.black]</b> section not found!", "Error")
            return
        }

        val blackSettings = toml.getTable("tool.black")

        val targetVersionsFromFile = blackSettings.getList<String>("target-version", Collections.emptyList())
        if (targetVersionsFromFile.count() > 0) {
            targetSpecificVersionsCheckbox.isSelected = true
            targetVersions.entries.map { entry ->
                versionCheckboxes["py" + entry.value]?.isSelected = targetVersionsFromFile.contains(entry.key)
            }
        } else {
            targetSpecificVersionsCheckbox.isSelected = false
        }

        lineLengthSpinner.value = blackSettings.getLong("line-length", DEFAULT_LINE_LENGTH.toLong()).toInt()
        skipStringNormalCheckbox.isSelected = blackSettings.getBoolean("skip-string-normalization", false)
        skipMagicTrailingCommaCheckbox.isSelected = blackSettings.getBoolean("skip-magic-trailing-comma", false)
        fastModeCheckbox.isSelected = blackSettings.getBoolean("fast", false)
    }

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = BorderLayout()
            border = IdeBorderFactory.createTitledBorder("Formatting Options")
            alignmentX = Component.LEFT_ALIGNMENT

            val targetVersionsPanel = createTargetVersionsPanel()

            targetSpecificVersionsCheckbox.addItemListener {
                targetVersionsPanel.components.map { checkbox ->
                    checkbox.isEnabled = targetSpecificVersionsCheckbox.isSelected
                }
            }

            val loadPyprojectTomlButton = createPyprojectTomlButton()
            val lineLengthPanel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT)

                add(lineLengthSpinner)
                add(createIdeMarginCopyButton())
            }

            add(
                FormBuilder.createFormBuilder()
                    .addLabeledComponent("Line length:", lineLengthPanel)
                    .addComponent(fastModeCheckbox)
                    .addComponent(skipStringNormalCheckbox)
                    .addComponent(skipMagicTrailingCommaCheckbox)
                    .addComponent(targetSpecificVersionsCheckbox)
                    .addComponent(targetVersionsPanel)
                    .addComponent(loadPyprojectTomlButton)
                    .panel,
                BorderLayout.NORTH
            )
        }
    }

    private fun createIdeMarginCopyButton(): JButton {
        return JButton("Copy from IDE").apply {
            isEnabled = true
            addActionListener {
                val pythonLanguage = Language.findLanguageByID("Python")
                lineLengthSpinner.value = CodeStyle.getSettings(project).getRightMargin(pythonLanguage)
            }
        }
    }

    private fun createTargetVersionsPanel(): JPanel {
        return JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT)
            border = IdeBorderFactory.createEmptyBorder(JBUI.insetsLeft(16))

            versionCheckboxes.values.forEach { checkbox ->
                checkbox.isEnabled = false
                add(checkbox)
            }
        }
    }

    private fun createPyprojectTomlButton(): JButton {
        return JButton("Load from pyproject.toml").apply {
            isEnabled = true

            addActionListener {
                val pyprojectTomlDescriptor = createPyprojectSpecificDescriptor()
                val candidates =
                    FilenameIndex.getVirtualFilesByName(
                        project,
                        "pyproject.toml",
                        GlobalSearchScope.projectScope(project)
                    )

                FileChooser.chooseFile(pyprojectTomlDescriptor, project, parent, candidates.firstOrNull()) { file ->
                    val contents = file.inputStream.bufferedReader().use(BufferedReader::readText)
                    processPyprojectToml(contents)
                }
            }
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        lineLengthSpinner.value = projectConfig.lineLength
        fastModeCheckbox.isSelected = projectConfig.fastMode
        skipStringNormalCheckbox.isSelected = projectConfig.skipStringNormalization
        skipMagicTrailingCommaCheckbox.isSelected = projectConfig.skipMagicTrailingComma

        projectConfig.pythonTargets.split(",").forEach { version ->
            versionCheckboxes[version]?.isSelected = true
        }

        if (projectConfig.targetSpecificVersions) {
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

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        projectConfig.lineLength = lineLengthSpinner.value as Int
        projectConfig.fastMode = fastModeCheckbox.isSelected
        projectConfig.skipStringNormalization = skipStringNormalCheckbox.isSelected
        projectConfig.skipMagicTrailingComma = skipMagicTrailingCommaCheckbox.isSelected
        projectConfig.targetSpecificVersions = targetSpecificVersionsCheckbox.isSelected
        projectConfig.pythonTargets = generateVersionSpec()
    }

    override fun isModified(
        globalConfig: BlackConnectGlobalSettings,
        projectConfig: BlackConnectProjectSettings
    ): Boolean {
        return lineLengthSpinner.value != projectConfig.lineLength ||
            fastModeCheckbox.isSelected != projectConfig.fastMode ||
            skipStringNormalCheckbox.isSelected != projectConfig.skipStringNormalization ||
            skipMagicTrailingCommaCheckbox.isSelected != projectConfig.skipMagicTrailingComma ||
            targetSpecificVersionsCheckbox.isSelected != projectConfig.targetSpecificVersions ||
            generateVersionSpec() != projectConfig.pythonTargets
    }
}
