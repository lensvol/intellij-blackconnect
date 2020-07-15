package me.lensvol.blackconnect

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import java.lang.ref.WeakReference

@Service
class BlackConnectProgressTracker {
    private val indicators = HashMap<String, WeakReference<ProgressIndicator>>()

    fun registerOperationOnTag(tag: String, indicator: ProgressIndicator) {
        cancelOperationOnTag(tag)
        indicators[tag] = WeakReference(indicator)
    }

    fun cancelOperationOnTag(tag: String) {
        indicators[tag]?.get()?.cancel()
    }
}