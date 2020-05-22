package me.lensvol.blackconnect

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

class BlackdReformatter(project: Project, configuration: BlackConnectSettingsConfiguration) {
    private val currentProject: Project = project
    private val currentConfig: BlackConnectSettingsConfiguration = configuration

    public fun process(document: Document) {
        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(document)
        val fileName = vFile?.name ?: "unknown"

        val progressIndicator = EmptyProgressIndicator()
        progressIndicator.isIndeterminate = true

        val projectService = currentProject.service<BlackConnectProgressTracker>()
        projectService.registerOperationOnPath(vFile!!.path, progressIndicator)

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
                object : Task.Backgroundable(currentProject, "Calling blackd") {
                    override fun run(indicator: ProgressIndicator) {
                        reformatCodeInDocument(currentConfig, document, fileName, project)
                    }
                },
                progressIndicator
        )
    }

    private fun callBlackd(
            path: String,
            sourceCode: String,
            pyi: Boolean = false,
            lineLength: Int = 88,
            fastMode: Boolean = false,
            skipStringNormalization: Boolean = false
    ): Pair<Int, String> {
        val url = URL(path)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true

                setRequestProperty("X-Protocol-Version", "1")
                setRequestProperty("X-Fast-Or-Safe", if (fastMode) "fast" else "safe")
                setRequestProperty("X-Line-Length", lineLength.toString())

                if (pyi) {
                    setRequestProperty("X-Python-Variant", "pyi")
                }

                if (skipStringNormalization) {
                    setRequestProperty("X-Skip-String-Normalization", "yes")
                }

                try {
                    outputStream.use { os ->
                        val input: ByteArray = sourceCode.toByteArray()
                        os.write(input, 0, input.size)
                    }

                    inputStream.bufferedReader().use { return Pair(responseCode, it.readText()) }
                } catch (e: IOException) {
                    return Pair(responseCode, errorStream.readBytes().toString())
                }
            }
        } catch (e: ConnectException) {
            return Pair(-1, e.message ?: "Connection failed.")
        }
    }

    private fun showError(text: String) {
        NotificationGroup("BlackConnect", NotificationDisplayType.BALLOON, true)
                .createNotification(text, NotificationType.ERROR)
                .notify(currentProject)
    }

    private fun reformatCodeInDocument(configuration: BlackConnectSettingsConfiguration, document: @Nullable Document, fileName: @NotNull String, project: @Nullable Project) {
        val progressIndicator = ProgressManager.getGlobalProgressIndicator()
        if(progressIndicator?.isCanceled == true)
            return

        val (responseCode, responseText) = callBlackd(
                "http://" + configuration.hostname + ":" + configuration.port + "/",
                document.text,
                pyi = fileName.endsWith(".pyi"),
                lineLength = configuration.lineLength,
                fastMode = configuration.fastMode,
                skipStringNormalization = configuration.skipStringNormalization
        )

        if(progressIndicator?.isCanceled == true)
            return

        when (responseCode) {
            200 -> {
                ApplicationManager.getApplication().invokeLater {
                    ApplicationManager.getApplication().runWriteAction(Computable {
                        if(progressIndicator?.isCanceled == false) {
                            CommandProcessor.getInstance().executeCommand(
                                    project,
                                    {
                                        document.setText(responseText)
                                    },
                                    "Reformat code using blackd",
                                    null,
                                    UndoConfirmationPolicy.DEFAULT,
                                    document
                            )
                        }
                    })
                }
            }
            204 -> {
                // Nothing was modified, nothing to do here, move along.
            }
            400 -> {
                showError("Source code contained syntax errors!")
            }
            500 -> {
                showError("Internal error, please see blackd output.")
            }
            else -> {
                showError("Something unexpected happened:\n$responseText")
            }
        }
    }

}