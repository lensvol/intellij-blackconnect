package me.lensvol.blackconnect.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackdResponse
import me.lensvol.blackconnect.CodeReformatter
import me.lensvol.blackconnect.DocumentUtil
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager
import kotlin.io.path.Path

class ReformatWholeFileAction : AnAction(), DumbAware {
    companion object {
        private val logger = Logger.getInstance(ReformatWholeFileAction::class.java.name)

        fun file_in_excludes(configuration: BlackConnectProjectSettings, fileName: String): Boolean {
            // blackd doesn't support excludes in their headers, so copying the logic from black.
            // Unfortunately as well, re.VERBOSE from python does not exist in kotlin as 1:1.
            // So approximating with the two RegexOptions.
            val extendRegex = Regex(configuration.extendExclude, setOf(RegexOption.COMMENTS, RegexOption.MULTILINE))
            val forceRegex = Regex(configuration.forceExclude, setOf(RegexOption.COMMENTS, RegexOption.MULTILINE))
            return extendRegex.matches(fileName) || forceRegex.matches(fileName)
        }

        fun reformatWholeDocument(
            fileName: String,
            project: Project,
            document: Document,
            projectRelativeFilePath: String,
        ) {
            val configuration = BlackConnectProjectSettings.getInstance(project)
            val codeReformatter = project.service<CodeReformatter>()
            val notificationService = project.service<NotificationManager>()
            val documentUtil = project.service<DocumentUtil>()

            if (file_in_excludes(configuration,projectRelativeFilePath)) {
                return
            }
            codeReformatter.process(
                fileName,
                document.text,
                fileName.endsWith(".pyi")
            ) { response ->
                when (response) {
                    is BlackdResponse.Blackened -> {
                        documentUtil.updateCodeInDocument(document) {
                            logger.debug("Code is going to be updated in $document")
                            if (!documentUtil.isInUndoRedo()) {
                                document.setText(response.sourceCode)
                            }
                        }
                    }
                    BlackdResponse.NoChangesMade -> {
                    }
                    is BlackdResponse.SyntaxError -> {
                        if (configuration.showSyntaxErrorMsgs) {
                            notificationService.showError("Source code contained syntax errors.")
                        }
                    }
                    is BlackdResponse.InternalError -> {
                        notificationService.showError(
                            "Internal server error, please see blackd output.",
                            viewPromptText = "View response",
                            additionalInfo = response.reason
                        )
                    }
                    is BlackdResponse.UnknownStatus -> notificationService.showError(
                        "Something unexpected happened:<br>${response.responseText}"
                    )
                    is BlackdResponse.InvalidRequest -> {
                        /*
                        Sadly, blackd does not reply with easily parseable error codes, so we have to implement
                        crude error detection heuristics here. This one specifically looks for unsupported
                        target versions, as different versions of blackd may have different sets of them.
                         */
                        if (response.reason.startsWith("Invalid value for X-Python-Variant")) {
                            val startPos = response.reason.indexOf(": ")
                            val endPos = response.reason.indexOf(" is not supported")
                            val unsupportedVersion = response.reason.slice(startPos + 2..endPos)

                            notificationService.showError(
                                "Your version of <b>blackd</b> does not support targeting " +
                                    "Python <b>$unsupportedVersion</b>.",
                            )
                        } else {
                            notificationService.showError(
                                "Server did not understand our request.<br>Maybe you need to connect over SSL?",
                                viewPromptText = "View response",
                                additionalInfo = response.reason
                            )
                        }
                    }
                }
            }
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = FileEditorManagerEx.getInstance(project).selectedTextEditor ?: return

        val vFile: VirtualFile? = FileDocumentManager.getInstance().getFile(editor.document)
        val fileName = vFile?.name ?: "unknown"
        // Would cause an issue if "unknown" is in the exclude list.
        val relativeFilePath = Path(project.basePath as String).relativize(Path(vFile?.path ?: "unknown")).toString()
        reformatWholeDocument(fileName, project, editor.document, relativeFilePath)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = false

        val project: Project = event.project ?: return
        val vFile: VirtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        val codeReformatter = project.service<CodeReformatter>()
        event.presentation.isEnabled = codeReformatter.isFileSupported(vFile)
    }
}
