package me.lensvol.blackconnect.ui

import java.awt.Component
import javax.swing.JPanel

fun setComponentStatus(component: Component, enabled: Boolean) {
    if (component is JPanel) {
        component.components.map { setComponentStatus(it, enabled) }
    } else {
        component.isEnabled = enabled
    }
}

fun JPanel.disableContents() = setComponentStatus(this, false)
fun JPanel.enableContents() = setComponentStatus(this, true)
