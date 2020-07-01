package me.lensvol.blackconnect.sentry

import com.intellij.openapi.application.ApplicationInfo
import io.sentry.SentryClient
import io.sentry.SentryClientFactory

const val DSN = "https://74ace242ccf94335bf6917d5b7f4aad6@o413179.ingest.sentry.io/5305122"

class SentryIntegration {
    companion object {
        private val sentryClient: SentryClient = SentryClientFactory.sentryClient(DSN)

        init {
            sentryClient.addBuilderHelper { e ->
                val build = ApplicationInfo.getInstance().build.asString()
                e.withTag("ide.build", build)
            }
        }

        fun client(): SentryClient {
            return sentryClient
        }
    }
}
