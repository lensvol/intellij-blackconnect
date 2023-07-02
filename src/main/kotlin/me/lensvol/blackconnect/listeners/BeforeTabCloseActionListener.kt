package me.lensvol.blackconnect.listeners

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.impl.tabActions.CloseTab
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackConnectProgressTracker

class BeforeTabCloseActionListener : AnActionListener {
    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        super.beforeActionPerformed(action, event)

        val project = event.project ?: return
        if (project.isDisposed) {
            return
        }

        if (action is CloseTab) {
            val vFile: VirtualFile? = event.getData(PlatformDataKeys.VIRTUAL_FILE)
            vFile?.let { file ->
                val projectService = event.project!!.service<BlackConnectProgressTracker>()
                projectService.cancelOperationOnTag(file.path)
            }
        }
    }
}
