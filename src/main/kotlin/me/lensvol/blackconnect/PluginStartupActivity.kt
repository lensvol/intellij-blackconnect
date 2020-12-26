package me.lensvol.blackconnect

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.util.Properties

class PluginStartupActivity : StartupActivity, DumbAware {
    private val logger = Logger.getInstance(PluginStartupActivity::class.java.name)

    override fun runActivity(project: Project) {
        val properties = Properties()
        val versionResource = PluginStartupActivity::class.java.getResourceAsStream("/version.properties")
        if (versionResource != null) {
            properties.load(versionResource)
            versionResource.close()
            logger.info("BlackConnect plugin (version: " + properties["version"] + ") is ready to start.")
        } else {
            logger.info("Unknown version of BlackConnect plugin is ready to start.")
        }
    }
}
