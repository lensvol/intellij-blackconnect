package me.lensvol.blackconnect.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import me.lensvol.blackconnect.BlackConnectProgressTracker

class BeforeTabClosedAction : AnAction(), DumbAware {

    init {
        ActionUtil.copyFrom(this, IdeActions.ACTION_CLOSE);
    }

    override fun actionPerformed(event: AnActionEvent) {
        if (event.project == null)
            return

        val fileMgr = FileEditorManagerEx.getInstanceEx(event.project!!)

        val vFile: VirtualFile? = event.getData(PlatformDataKeys.VIRTUAL_FILE)
        val projectService = event.project!!.service<BlackConnectProgressTracker>()
        vFile?.let {
            file -> projectService.cancelOperationOnPath(file.path)

            fileMgr.currentWindow?.findFileComposite(vFile).let {
                fileMgr.closeFile(vFile, fileMgr.currentWindow)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.VIRTUAL_FILE) != null;
    }
}