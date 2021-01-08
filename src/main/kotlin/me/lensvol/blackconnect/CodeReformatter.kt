package me.lensvol.blackconnect

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.string.printToString
import me.lensvol.blackconnect.BlackdResponse.Blackened
import me.lensvol.blackconnect.settings.BlackConnectProjectSettings
import me.lensvol.blackconnect.ui.NotificationManager

data class FragmentFormatting(
    val whitespaceBefore: String,
    val whitespaceAfter: String,
    val indent: String,
    val endsWithNewline: Boolean
)

@Service
open class CodeReformatter(project: Project) {
    private val currentProject: Project = project
    private val currentConfig = BlackConnectProjectSettings.getInstance(project)
    private val notificationService: NotificationManager = project.service()

    private val logger = Logger.getInstance(CodeReformatter::class.java.name)

    fun isFileSupported(file: VirtualFile): Boolean {
        if (file.name.endsWith(".py") || file.name.endsWith(".pyi")) {
            return true
        }

        return currentConfig.enableJupyterSupport &&
            file.fileType !is UnknownFileType &&
            (file.fileType as LanguageFileType).language.id == "Jupyter"
    }

    open fun process(tag: String, sourceCode: String, isPyi: Boolean, receiver: (BlackdResponse) -> Unit) {
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

    fun processFragment(tag: String, fragment: String, isPyi: Boolean, receiver: (BlackdResponse) -> Unit) {
        val progressIndicator = EmptyProgressIndicator()
        progressIndicator.isIndeterminate = true

        val projectService = currentProject.service<BlackConnectProgressTracker>()
        projectService.registerOperationOnTag(tag, progressIndicator)

        val (formatting, deindentedFragment) = extractIndent(fragment)

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            object : Task.Backgroundable(currentProject, "Calling blackd") {
                override fun run(indicator: ProgressIndicator) {
                    logger.debug("Reformatting code with tag '$tag'")
                    reformatWithBlackd(currentConfig, deindentedFragment, isPyi)?.let { response ->
                        when (response) {
                            is Blackened -> {
                                receiver(Blackened(restoreFormatting(response.sourceCode.trimEnd('\n'), formatting)))
                            }
                            else -> receiver(response)
                        }
                    }
                }
            },
            progressIndicator
        )
    }

    private fun extractIndent(codeFragment: String): Pair<FragmentFormatting, String> {
        val builder = StringBuilder()
        val firstLine = codeFragment.lines().dropWhile { it.isBlank() }.first()
        val indentLen = firstLine.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) firstLine.length else it }
        val indentString = "".padStart(indentLen)

        val whitespacePrefix = codeFragment.takeWhile { it.isWhitespace() }
        val whitespaceSuffix = codeFragment.takeLastWhile { it.isWhitespace() }
        val actualCode = codeFragment.substring(whitespacePrefix.length, codeFragment.length - whitespaceSuffix.length)

        actualCode.lines()
            .map { line ->
                builder.appendLine(line.removePrefix(indentString))
            }

        return Pair(
            FragmentFormatting(whitespacePrefix, whitespaceSuffix, indentString, actualCode.endsWith("\n")),
            builder.printToString()
        )
    }

    private fun restoreFormatting(code: String, formatting: FragmentFormatting): String {
        return buildString {
            val codeLines = code.lines()

            if (!code.contains('\n')) {
                append(formatting.indent)
                append(code)
                if (formatting.endsWithNewline) {
                    append('\n')
                }
                return@buildString
            }

            append(formatting.whitespaceBefore)
            append(codeLines.first())
            for (line in codeLines.listIterator(1)) {
                appendLine()
                append(line.prependIndent(formatting.indent))
            }
            append(formatting.whitespaceAfter)
        }
    }

    private fun reformatWithBlackd(
        configuration: BlackConnectProjectSettings,
        sourceCode: String,
        isPyi: Boolean
    ): BlackdResponse? {
        val progressIndicator = ProgressManager.getGlobalProgressIndicator()
        logger.debug("Reformatting cancelled before we could begin")
        if (progressIndicator?.isCanceled == true) return null

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
        if (progressIndicator?.isCanceled == true) return null

        return when (response) {
            is Failure -> {
                val reason = response.reason.message ?: "Connection failed."
                notificationService.showError("Failed to connect to <b>blackd</b>:<br>$reason")
                null
            }
            is Success -> {
                notificationService.expireAllErrors()
                response.value
            }
        }
    }
}
