plugins {
    id("org.jetbrains.intellij") version "1.5.2"
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    jacoco

    id("io.gitlab.arturbosch.detekt") version "1.15.0"
}

group = "me.lensvol"
version = "0.5.0"

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
    version.set(ideaVersion)
    updateSinceUntilBuild.set(false)
    pluginName.set("intellij-blackconnect")
    plugins.set(listOf("python-ce"))
}

tasks.patchPluginXml {
    changeNotes.set(
        """
      <p>0.5.0</p>
      <p>Well, that one was long overdue. The groundwork for starting <b>blackd</b> from inside the plugin was
      done more than a year ago, but alas - mental health is a fickle thing and burnout is no laughing matter.
      Funnily enough, it took being depressed from the ongoing world crisis to finally push me into
      releasing this as a futile effort to allay my anxiety.</p>
      
      <p>Anyways, here it is. Go play with it, have fun, and come back with helpful suggestions.</p>
      
      <p>Stay safe. Stay sane.</p>
      
      <ul>
        <li>Support starting <b>blackd</b> when the plugin starts.</li>
      </ul>
            
      <p>0.4.6</p>
      <p>A small release to tide you over till bigger features ship.</p>
      <ul>
        <li>Support 3.10 as a target version. (kudos to <a href="https://github.com/lxop">Alex Opie
</a>)</li>
        <li>Fix broken link to <b>blackd</b> documentation in plugin description.</li>
      </ul>

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
      """
    )
}

tasks.processResources {
    // We need bogus second entry due to https://github.com/gradle/gradle/issues/14733 *facepalm*
    val properties = mapOf("version" to project.version, "hello" to "world")
    inputs.properties(properties)
    filesMatching("**/plugin.properties") {
        expand(properties)
    }
}

tasks.publishPlugin {
    token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
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
    jvmTarget = "11"
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "11"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
}

tasks.runPluginVerifier {
    ideVersions.set(listOf("2021.3", "2021.2", "2021.1.3"))
}

tasks.test {
    systemProperty("idea.force.use.core.classloader", "true")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}