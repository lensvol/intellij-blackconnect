package me.lensvol.blackconnect

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile

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