package me.lensvol.blackconnect.config.sections

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.io.isDirectory
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import me.lensvol.blackconnect.BlackdExecutor
import me.lensvol.blackconnect.ExecutionResult
import me.lensvol.blackconnect.config.DEFAULT_BLACKD_HOST
import me.lensvol.blackconnect.config.DEFAULT_BLACKD_PORT
import me.lensvol.blackconnect.settings.BlackConnectGlobalSettings
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.AdditionalInformationDialog
import me.lensvol.blackconnect.ui.BlackdExecutableVariant
import me.lensvol.blackconnect.ui.ExecutableVariantsDialog
import me.lensvol.blackconnect.ui.disableContents
import me.lensvol.blackconnect.ui.enableContents
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.isExecutable

const val PATH_FIELD_RIGHT_INSET = 36
const val SERVER_SETTINGS_DELIMETER_WIDTH = 6

@Suppress("MagicNumber")
class LocalDaemonSection(val project: Project) : ConfigSection(project) {
    private val startLocalServerCheckbox = JCheckBox("Start local blackd instance when plugin loads")
    private val remotePortModel = SpinnerNumberModel(DEFAULT_BLACKD_PORT, 1, 65535, 1)
    private val bindOnHostnameText = JTextField(DEFAULT_BLACKD_HOST)
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
    private val detectBinaryButton = JButton("Detect")
    private val currentlyRunningText = JLabel("<html><b>Currently running:</b> <i>none</i>")

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
            @Suppress("DialogTitleCapitalization")
            border = IdeBorderFactory.createTitledBorder("Local Instance (shared between projects)")
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
            .addComponent(Box.createRigidArea(Dimension(SERVER_SETTINGS_DELIMETER_WIDTH, 0)) as JComponent)
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
                JPanel().apply {
                    layout = GridBagLayout()
                    alignmentX = Component.LEFT_ALIGNMENT

                    val c = GridBagConstraints().apply {
                        anchor = GridBagConstraints.WEST
                        fill = GridBagConstraints.NONE
                    }

                    c.gridy = 0
                    c.gridx = 0
                    c.insets = JBUI.insets(0, 0, 0, PATH_FIELD_RIGHT_INSET)
                    add(JLabel("Path:"), c)

                    c.gridx = 1
                    c.weightx = 1.0
                    c.fill = GridBagConstraints.HORIZONTAL
                    c.insets = JBUI.emptyInsets()
                    add(blackdExecutableChooser, c)

                    c.gridx = 2
                    c.weightx = 0.1
                    c.fill = GridBagConstraints.HORIZONTAL
                    c.insets = JBUI.emptyInsets()
                    add(detectBinaryButton, c)
                },
                constraints
            )

            constraints.gridy = 2
            constraints.gridx = 0
            constraints.gridwidth = 2
            constraints.insets = JBUI.insets(5, 0, 5, 0)
            add(currentlyRunningText, constraints)

            constraints.insets = JBUI.emptyInsets()
            constraints.gridy = 3
            constraints.gridx = 0
            constraints.gridwidth = GridBagConstraints.RELATIVE
            add(startDaemonButton, constraints)

            constraints.gridx = 1
            constraints.gridwidth = GridBagConstraints.RELATIVE
            add(stopDaemonButton, constraints)

            updateRunStateUI()
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
                updateRunStateUI()
            }
        })

        startLocalServerCheckbox.addItemListener {
            if (startLocalServerCheckbox.isSelected) {
                localServerPanel.enableContents()
            } else {
                localServerPanel.disableContents()
            }
            updateRunStateUI()
        }

        startDaemonButton.addActionListener {
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

                updateRunStateUI()
            }
        }

        stopDaemonButton.addActionListener {
            thread {
                blackdExecutor.stopDaemon()
                updateRunStateUI()
            }
        }

        installDetectBinaryHandler()
    }

    @Suppress("ComplexMethod")
    private fun installDetectBinaryHandler() {
        detectBinaryButton.addActionListener {
            val variants = mutableListOf<BlackdExecutableVariant>()
            val blackdInPath = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("blackd")
            blackdInPath?.let {
                variants.add(BlackdExecutableVariant("PATH", it.absolutePath))
            }

            @Suppress("LoopWithTooManyJumpStatements")
            for (module in ModuleManager.getInstance(project).modules) {
                val sdk = ModuleRootManager.getInstance(module).sdk ?: continue

                // We need to do it in a less fragile way...
                if (sdk.sdkType.name != "Python SDK" || sdk.homePath == null) {
                    continue
                }

                val homePath = sdk.homePath ?: continue

                val parts = FileUtil.splitPath(File(homePath).path)
                if (parts.last() != "python" && parts.last() != "python3") {
                    continue
                }

                parts.removeLast()
                parts.add("blackd")
                @Suppress("SpreadOperator")
                val pathToBlackd = FileUtil.join(*parts.map { it }.toTypedArray())

                if (!FileUtil.exists(pathToBlackd)) {
                    continue
                }

                variants.add(BlackdExecutableVariant(module.name, pathToBlackd))
            }

            when (variants.size) {
                0 -> {
                    Messages.showErrorDialog(
                        "No <b>blackd</b> executables were found in PATH or virtualenvs.",
                        "Nothing Found",
                    )
                }
                1 -> {
                    blackdExecutableChooser.text = variants[0].path
                }
                else -> {
                    invokeLater(modalityState = ModalityState.defaultModalityState()) {
                        val executableChooserDialog = ExecutableVariantsDialog(
                            project,
                            variants
                        ) {
                            blackdExecutableChooser.text = it.path
                        }
                        executableChooserDialog.show()
                    }
                }
            }
        }
    }

    private fun updateRunStateUI() {
        if (!startLocalServerCheckbox.isSelected) return

        if (blackdExecutor.isRunning()) {
            val instance = blackdExecutor.currentInstance()
            currentlyRunningText.text = "<html><b>Currently running:</b> ${instance?.path}"

            startDaemonButton.isEnabled = false
            stopDaemonButton.isEnabled = true
        } else {
            currentlyRunningText.text = "<html><b>No instances are running at this moment.</b>"
            stopDaemonButton.isEnabled = false
            startDaemonButton.isEnabled = blackdExecutableChooser.text.isNotEmpty() &&
                bindOnHostnameText.text.isNotEmpty()
        }
    }

    override fun loadFrom(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        localPortSpinner.value = globalConfig.bindOnPort
        bindOnHostnameText.text = globalConfig.bindOnHostname
        blackdExecutableChooser.text = globalConfig.blackdBinaryPath

        if (globalConfig.spawnBlackdOnStartup) {
            startLocalServerCheckbox.doClick()
            startLocalServerCheckbox.isSelected = true
            updateRunStateUI()
        }
    }

    override fun saveTo(globalConfig: BlackConnectGlobalSettings, projectConfig: BlackConnectProjectSettings) {
        globalConfig.spawnBlackdOnStartup = startLocalServerCheckbox.isSelected
        globalConfig.bindOnHostname = bindOnHostnameText.text.ifBlank { "localhost" }
        globalConfig.bindOnPort = localPortSpinner.value as Int

        if (blackdExecutableChooser.text.isNotEmpty()) {
            globalConfig.blackdBinaryPath = blackdExecutableChooser.text
        }
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

    override fun validate() {
        if (startLocalServerCheckbox.isSelected) {
            val userProvidedPath = blackdExecutableChooser.text

            if (userProvidedPath.isEmpty()) {
                throw ConfigurationException("No path provided for a local 'blackd' binary.")
            }

            val pathToBlackdBinary = Path(userProvidedPath)

            if (pathToBlackdBinary.isDirectory()) {
                throw ConfigurationException("'blackd' binary path points to a directory, not a file.")
            }

            if (!pathToBlackdBinary.isExecutable()) {
                throw ConfigurationException("'blackd' binary path does not point to an executable.")
            }
        }
    }
}
