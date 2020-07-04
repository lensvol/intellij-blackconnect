package me.lensvol.blackconnect.sentry

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.ui.Messages
import com.intellij.util.Consumer
import io.sentry.SentryClient
import io.sentry.SentryClientFactory
import io.sentry.connection.EventSendCallback
import io.sentry.event.Event
import java.awt.Component

const val DSN = "https://74ace242ccf94335bf6917d5b7f4aad6@o413179.ingest.sentry.io/5305122"

fun SentryClient.showResultInUi(parentComponent: Component, consumer: Consumer<SubmittedReportInfo>) {
    this.addEventSendCallback(object: EventSendCallback {
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
}

class SentryIntegration {
    companion object {
        fun client(): SentryClient {
            val sentryClient: SentryClient = SentryClientFactory.sentryClient(DSN)
            sentryClient.addBuilderHelper { e ->
                val build = ApplicationInfo.getInstance().build.asString()
                e.withTag("ide.build", build)
            }
            return sentryClient
        }
    }
}
