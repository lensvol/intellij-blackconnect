package me.lensvol.blackconnect.sentry

import com.intellij.diagnostic.IdeaReportingEvent
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import io.sentry.event.interfaces.SentryException
import java.awt.Component
import java.util.LinkedList

class SentryErrorReporter : ErrorReportSubmitter() {
    /*
        This whole class was copied almost verbatim from "Error Reporting in a Plugin"
        article by Joachim Ansorg. Kudos to him!
    */
    override fun getReportActionText(): String {
        return "Send to author"
    }

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<SubmittedReportInfo>
    ): Boolean {
        val context = DataManager.getInstance().getDataContext(parentComponent)
        val project: Project? = CommonDataKeys.PROJECT.getData(context)

        val sentryClient = SentryIntegration.client()

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Report error to sentry.io") {
            override fun run(indicator: ProgressIndicator) {
                val errors = LinkedList<SentryException>()
                for (ideaEvent in events) {
                    if (ideaEvent is IdeaReportingEvent) {
                        val ex: Throwable = ideaEvent.data.throwable
                        errors.add(SentryException(ex, ex.stackTrace))
                    }
                }

                val event = EventBuilder().apply {
                    // Empty server name to avoid tracking personal data
                    withServerName("<redacted>")
                    withLevel(Event.Level.ERROR)
                    withMessage(additionalInfo)
                    withSentryInterface(ExceptionInterface(errors))
                    if (pluginDescriptor is IdeaPluginDescriptor) {
                        withRelease((pluginDescriptor as IdeaPluginDescriptor).version)
                    }
                }
                sentryClient.showResultInUi(parentComponent, consumer)
                sentryClient.sendEvent(event)
            }
        })
        return true
    }
}
