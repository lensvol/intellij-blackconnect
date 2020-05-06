package me.lensvol.blackconnect

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL


class FormatUsingBlackdAction : AnAction() {

    private fun callBlackd(path: String, sourceCode: String, pyi: Boolean = false): Pair<Int, String> {
        val url = URL(path)

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true

                setRequestProperty("X-Protocol-Version", "1")
                setRequestProperty("X-Fast-Or-Safe", "safe")
                if (pyi) {
                    setRequestProperty("X-Python-Variant", "pyi")
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

    override fun actionPerformed(event: AnActionEvent) {
        val notificationGroup = NotificationGroup("BlackConnect", NotificationDisplayType.BALLOON, true)

        event.project?.let { project ->
            FileEditorManagerEx.getInstance(project).selectedTextEditor?.let { editor ->
                val configuration = BlackConnectSettingsConfiguration.getInstance(project)

                val vFile: VirtualFile? = event.getData(PlatformDataKeys.VIRTUAL_FILE)
                val fileName = vFile?.name ?: "unknown"

                val result = callBlackd(
                        "http://" + configuration.hostname + ":" + configuration.port + "/",
                        editor.document.text,
                        pyi = fileName.endsWith(".pyi")
                )
                val notification: Notification

                when (result.first) {
                    200 -> {
                        ApplicationManager.getApplication().runWriteAction(Computable {
                            CommandProcessor.getInstance().executeCommand(
                                    project,
                                    { editor.document.setText(result.second) },
                                    "Reformat code using blackd",
                                    null,
                                    UndoConfirmationPolicy.DEFAULT,
                                    editor.document
                            )
                        })
                        notification = notificationGroup.createNotification(
                                "Code was re-formatted.",
                                NotificationType.INFORMATION
                        )
                    }
                    204 -> {
                        notification = notificationGroup.createNotification(
                                "No changes were needed.",
                                NotificationType.INFORMATION
                        )
                    }
                    400 -> {
                        notification = notificationGroup.createNotification(
                                "Source code contained syntax errors.",
                                NotificationType.ERROR
                        )
                    }
                    500 -> {
                        notification = notificationGroup.createNotification(
                                "Internal error, please see blackd output.",
                                NotificationType.ERROR
                        )
                    }
                    else -> {
                        notification = notificationGroup.createNotification(
                                "Something unexpected happened:\n" + result.second,
                                NotificationType.ERROR
                        )
                    }
                }

                notification.notify(project)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.project
        if (project == null) {
            event.presentation.isEnabled = false
        } else {
            val vFile: VirtualFile? = event.getData(PlatformDataKeys.VIRTUAL_FILE)
            vFile?.name?.let { filename ->
                event.presentation.isEnabled = filename.endsWith(".py") || filename.endsWith(".pyi")
            }
        }
    }
}