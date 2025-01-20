plugins {
    id("org.jetbrains.intellij") version "1.17.2"
    jacoco

    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    kotlin("jvm") version "1.9.0"
}

group = "me.lensvol"
version = "0.6.3-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

apply(plugin = "io.gitlab.arturbosch.detekt")
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")

    testImplementation("org.mock-server:mockserver-netty:5.14.0") {
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
      <p><b>0.6.2</b></p>
      <ul>
        <li>Fix compatibility with PyCharm 2024.1+</li>
      </ul>

      <p><b>0.6.1</b></p>
      <p>Ain't no rest for the wicked, huh.</p>

      <ul>
        <li>Fix crash when no <b>blackd</b> executable is chosen in the dialog window.</li>
        <li>Fix exception appearing when you try to open Settings page in PyCharm EAP.</li>
        <li>Fix crash when trying to reformat fragment consisting only of blank lines.</li>
        <li>Fix plugin interfering with the <i>Ctrl-W</i> shortcut in IDEA.</li>
        <li>Fix tab closure code taking too long in the event thread.</li>
        <li>Remove superfluous <i>"<b>blackd</b> already started"</i> notification.</li>
      </ul>            
            
      <p><b>0.6.0</b></p>
      <p>Releases that are long overdue are becoming my jam! Oh, well.</p>

      <ul>
        <li>Support for targeting Python 3.11</li>
        <li>Support for <i>--preview</i> option.</li>
        <li>Perform validation before running our own <b>blackd</b> instance (h/t to <b>@KotlinIsland</b>).</li>
        <li>Fix rare cases when indentation gets broken during reformatting (h/t to <b>@shanx</b>)</li> 
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
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks {
    withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
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
    ideVersions.set(listOf("2023.1.3", "2022.3.3", "2021.3"))
}

tasks.test {
    systemProperty("idea.force.use.core.classloader", "true")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
