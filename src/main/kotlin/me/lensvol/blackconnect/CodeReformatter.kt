package me.lensvol.blackconnect

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackdResponse.Blackened
import me.lensvol.blackconnect.BlackdResponse.InternalError
import me.lensvol.blackconnect.BlackdResponse.NoChangesMade
import me.lensvol.blackconnect.BlackdResponse.SyntaxError
import me.lensvol.blackconnect.BlackdResponse.UnknownStatus
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationGroupManager
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class CodeReformatter(project: Project, configuration: BlackConnectProjectSettings) {
    private val currentProject: Project = project
    private val currentConfig: BlackConnectProjectSettings = configuration
    private val notificationGroup: NotificationGroup = NotificationGroupManager.mainGroup()

    private val logger = Logger.getInstance(CodeReformatter::class.java.name)

    fun isFileSupported(file: VirtualFile): Boolean {
        return file.name.endsWith(".py") || file.name.endsWith(".pyi") ||
            (currentConfig.enableJupyterSupport &&
                (file.fileType as LanguageFileType).language.id == "Jupyter")
    }

    fun process(document: Document) {
        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(document)
        val fileName = vFile?.name ?: "unknown"

        val progressIndicator = EmptyProgressIndicator()
        progressIndicator.isIndeterminate = true

        val projectService = currentProject.service<BlackConnectProgressTracker>()
        projectService.registerOperationOnPath(vFile!!.path, progressIndicator)

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            object : Task.Backgroundable(currentProject, "Calling blackd") {
                override fun run(indicator: ProgressIndicator) {
                    logger.debug("Reformatting code in '$fileName'")
                    reformatCodeInDocument(currentConfig, document, fileName, project)
                }
            },
            progressIndicator
        )
    }

    private fun showError(text: String) {
        notificationGroup
            .createNotification(text, NotificationType.ERROR)
            .setTitle("BlackConnect")
            .notify(currentProject)
    }

    private fun reformatCodeInDocument(
        configuration: BlackConnectProjectSettings,
        document: @Nullable Document,
        fileName: @NotNull String,
        project: @Nullable Project
    ) {
        val progressIndicator = ProgressManager.getGlobalProgressIndicator()
        logger.debug("Reformatting cancelled before we could begin")
        if (progressIndicator?.isCanceled == true)
            return

        val blackdClient = BlackdClient(configuration.hostname, configuration.port)

        val response = blackdClient.reformat(
            document.text,
            pyi = fileName.endsWith(".pyi"),
            lineLength = configuration.lineLength,
            fastMode = configuration.fastMode,
            skipStringNormalization = configuration.skipStringNormalization,
            targetPythonVersions = if (configuration.targetSpecificVersions) configuration.pythonTargets else ""
        )

        logger.debug("Reformatting cancelled after call to blackd")
        if (progressIndicator?.isCanceled == true)
            return

        when (response) {
            is Failure -> {
                val reason = response.reason.message ?: "Connection failed."
                showError("Failed to connect to <b>blackd</b>:\n${reason}")
            }
            is Success -> {
                when (response.value) {
                    is Blackened -> {
                        updateCodeInDocument(project, progressIndicator, document, response.value.sourceCode)
                    }
                    is NoChangesMade -> {
                        // Nothing was modified, nothing to do here, move along.
                    }
                    is SyntaxError -> {
                        if (configuration.showSyntaxErrorMsgs) {
                            showError("Source code contained syntax errors.")
                        }
                    }
                    is InternalError -> {
                        showError("Internal error, please see blackd output.")
                    }
                    is UnknownStatus -> {
                        showError("Something unexpected happened:\n${response.value.responseText}")
                    }
                }
            }
        }
    }

    private fun updateCodeInDocument(project: Project, progressIndicator: ProgressIndicator?, document: Document, sourceCode: String) {
        with(ApplicationManager.getApplication()) {
            invokeLater {
                runWriteAction(Computable {
                    if (progressIndicator?.isCanceled == true) {
                        logger.debug("Reformatting cancelled before updating the document")
                        return@Computable
                    }

                    CommandProcessor.getInstance().executeCommand(
                        project,
                        {
                            logger.debug("Code is going to be updated in $document")
                            document.setText(sourceCode)
                        },
                        "Reformat code using blackd",
                        null,
                        UndoConfirmationPolicy.DEFAULT,
                        document
                    )
                })
            }
        }
    }
}