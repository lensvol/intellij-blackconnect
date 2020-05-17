package me.lensvol.blackconnect

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import java.lang.ref.WeakReference

@Service
class BlackConnectProgressTracker {
    private val indicators = HashMap<String, WeakReference<ProgressIndicator>>()

    fun registerOperationOnPath(path: String, indicator: ProgressIndicator) {
        cancelOperationOnPath(path)
        indicators[path] = WeakReference(indicator)
    }

    fun cancelOperationOnPath(path: String) {
        indicators[path]?.get()?.cancel()
    }
}