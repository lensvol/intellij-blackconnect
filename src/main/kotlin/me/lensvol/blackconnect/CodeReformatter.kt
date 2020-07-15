package me.lensvol.blackconnect

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackdResponse.Blackened
import me.lensvol.blackconnect.BlackdResponse.InternalError
import me.lensvol.blackconnect.BlackdResponse.NoChangesMade
import me.lensvol.blackconnect.BlackdResponse.SyntaxError
import me.lensvol.blackconnect.BlackdResponse.UnknownStatus
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationGroupManager

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

    fun process(tag: String, sourceCode: String, isPyi: Boolean, receiver: (String) -> Unit) {
        val progressIndicator = EmptyProgressIndicator()
        progressIndicator.isIndeterminate = true

        val projectService = currentProject.service<BlackConnectProgressTracker>()
        projectService.registerOperationOnTag(tag, progressIndicator)

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            object : Task.Backgroundable(currentProject, "Calling blackd") {
                override fun run(indicator: ProgressIndicator) {
                    logger.debug("Reformatting code with tag '$tag'")
                    reformatWithBlackd(currentConfig, sourceCode, isPyi)?.let {
                        receiver(it)
                    }
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

    private fun reformatWithBlackd(
        configuration: BlackConnectProjectSettings,
        sourceCode: String,
        isPyi: Boolean
    ): String? {
        val progressIndicator = ProgressManager.getGlobalProgressIndicator()
        logger.debug("Reformatting cancelled before we could begin")
        if (progressIndicator?.isCanceled == true)
            return null

        val blackdClient = BlackdClient(configuration.hostname, configuration.port)

        val response = blackdClient.reformat(
            sourceCode,
            pyi = isPyi,
            lineLength = configuration.lineLength,
            fastMode = configuration.fastMode,
            skipStringNormalization = configuration.skipStringNormalization,
            targetPythonVersions = if (configuration.targetSpecificVersions) configuration.pythonTargets else ""
        )

        logger.debug("Reformatting cancelled after call to blackd")
        if (progressIndicator?.isCanceled == true)
            return null

        when (response) {
            is Failure -> {
                val reason = response.reason.message ?: "Connection failed."
                showError("Failed to connect to <b>blackd</b>:\n${reason}")
            }
            is Success -> {
                when (response.value) {
                    is Blackened -> {
                        return response.value.sourceCode
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

        return null
    }
}