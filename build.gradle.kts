plugins {
  id("org.jetbrains.intellij") version "0.7.2"
  id("org.jetbrains.kotlin.jvm") version "1.4.21-2"
  id("jacoco")

  id("io.gitlab.arturbosch.detekt") version "1.15.0"
}

group = "me.lensvol"
version = "0.4.5"

repositories {
  jcenter()
  mavenCentral()
}

apply(plugin = "io.gitlab.arturbosch.detekt")
dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.moandjiezana.toml:toml4j:0.7.2")

  testImplementation("org.mock-server:mockserver-netty:5.3.0") {
    exclude(group = "ch.qos.logback")
  }

  implementation("io.sentry:sentry:1.7.30") {
    exclude(group = "org.slf4j")
  }

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")
}

intellij {
  val ideaVersion: String by project
  version = ideaVersion
  updateSinceUntilBuild = false
  pluginName = "intellij-blackconnect"
  setPlugins("python-ce")
}

tasks.compileKotlin {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.patchPluginXml {
  version(project.version)
  changeNotes("""
      <p>0.4.5</p>
      <ul>
        <li>Support new '--skip-magic-trailing-comma' option.</li>
        <li>Support Python 3.9 as target version.</li>
        <li>Added "Trigger on Code Reformat" option (kudos to <a href="https://github.com/vlasovskikh">Andrey Vlasovskikh</a>).</li>
        <li>Fix rare crash when saving non-Python files with Jupyter support enabled (kudos to <a href="
https://github.com/elliotwaite">Elliot Waite</a>).</li>
        <li>Fix for rare crash when updating document during Undo/Redo operation.</li>
      </ul>

      <p>0.4.4</p>
      <br>
      <p>This release is dedicated to the memory of our cat <b>Luna</b>, who passed away due to cancer last year.</p><br>
      <p>She was kind, smart and loyal. Best cat in the world.</p><br>
      <p><b>We miss you, girl.</b></p>
      <br>
      <ul>
        <li>Add button to copy line length settings from the IDE ("right margin").</li>
        <li>Support for connecting to blackd over SSL (kudos to <a href="https://github.com/studioj">studioj</a>)</li>
        <li>Make server error notifications more descriptive.</li>
        <li>Miscellaneous fixes and improvements.</li>
      </ul>

      <p>0.4.3</p>
      <ul>
        <li>Fix rare crash when processing source code with CR/LF sequence inside.</li>
        <li>Fix rare crash when saving files without type and Jupyter support is enabled.</li>
      </ul>

      <p>0.4.2</p>
      <ul>
        <li>Automatically close any outstanding error messages on successful call to blackd.</li>
        <li>Bump lower compatibility boundary to IDEA 2020.1.4 and up.</li>
      </ul>

      <p>0.4.1</p>
      <ul>
        <li>Fix regression with file save trigger reformatting non-Python files (reported by Matthew R. Scott).</li>
      </ul>

      <p>0.4.0</p>
      <ul>
        <li>Added "Check connection" button to "Settings" screen.</li>
        <li>Added "Reformat Selected Fragment" action to support partial reformatting.</li>
        <li>Reformatting actions moved to <b>BlackConnect</b> submenu under "Tools".</li>
        <li>Fix rare crash when closing tab with window handle set to <i>null</i>.</li>
        <li>Notification balloons no longer show up in "Event log".</li>
      </ul>
      """)
}

tasks.processResources {
  val properties = mapOf("version" to project.version)
  inputs.properties(properties)
  filesMatching("*/version.properties") {
    expand(properties)
  }
}

tasks.publishPlugin {
  token(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true // coveralls plugin depends on xml format report
    html.isEnabled = true
  }
}

detekt {
  config = files("./detekt-config.yml")
  buildUponDefaultConfig = true // preconfigure defaults
  baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

  reports {
    html.enabled = false // observe findings in your browser with structure and code snippets
    xml.enabled = false // checkstyle like format mainly for integrations like Jenkins
    txt.enabled = false // similar to the console output, contains issue signature to manually edit baseline files
    sarif.enabled = false // SARIF integration (https://sarifweb.azurewebsites.net/) for integrations with Github
  }
}

tasks.detekt {
  jvmTarget = "1.8"
}

tasks.runPluginVerifier {
  ideVersions("2020.1.4, 2020.2.3, 2020.3")
}

tasks.test {
  systemProperty("idea.force.use.core.classloader", "true")
}
