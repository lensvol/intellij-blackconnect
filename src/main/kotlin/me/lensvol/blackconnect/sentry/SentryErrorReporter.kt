package me.lensvol.blackconnect.sentry

import com.intellij.diagnostic.IdeaReportingEvent
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.Consumer
import io.sentry.connection.EventSendCallback
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

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Report error to sentry.io") {
            override fun run(indicator: ProgressIndicator) {
                val event = EventBuilder()
                event.withLevel(Event.Level.ERROR)

                if (pluginDescriptor is IdeaPluginDescriptor) {
                    event.withRelease((pluginDescriptor as IdeaPluginDescriptor).version)
                }
                // Empty server name to avoid tracking personal data
                event.withServerName("<redacted>")

                val errors = LinkedList<SentryException>()
                for (ideaEvent in events) {
                    if (ideaEvent is IdeaReportingEvent) {
                        val ex: Throwable = ideaEvent.data.throwable
                        errors.add(SentryException(ex, ex.stackTrace))
                    }
                }

                event.withSentryInterface(ExceptionInterface(errors))
                val sentryClient = SentryIntegration.client()
                sentryClient.addEventSendCallback(object: EventSendCallback {
                    override fun onSuccess(event: Event?) {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showInfoMessage(
                                parentComponent,
                                "Thank you for submitting your report!",
                                "Reporting to Sentry"
                            )
                            consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                        }
                    }

                    override fun onFailure(event: Event?, exception: Exception?) {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(
                                parentComponent,
                                exception.toString(),
                                "Reporting to Sentry"
                            )
                            consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED))
                        }
                    }
                })
                sentryClient.sendEvent(event)
            }
        })
        return true
    }
}
