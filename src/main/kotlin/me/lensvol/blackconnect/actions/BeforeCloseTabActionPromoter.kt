package me.lensvol.blackconnect.actions

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.util.containers.ContainerUtil
import java.util.Collections

class BeforeCloseTabActionPromoter : ActionPromoter {
    override fun promote(actions: List<AnAction?>?, context: DataContext?): List<AnAction?>? {
        val action = ContainerUtil.findInstance(actions!!, BeforeTabClosedAction::class.java)
        return if (action != null) Collections.singletonList(action) else Collections.emptyList()
    }
}
