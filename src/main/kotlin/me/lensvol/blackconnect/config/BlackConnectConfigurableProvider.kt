package me.lensvol.blackconnect.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project

class BlackConnectConfigurableProvider(private val project: Project) : ConfigurableProvider() {
    override fun createConfigurable(): Configurable? {
        return BlackConnectConfigurable(project)
    }
}
