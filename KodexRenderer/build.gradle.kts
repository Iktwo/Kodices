import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktor)
}

version = "0.1.0"

kotlin {
    explicitApi()

    jvmToolchain(21)
}

application {
    mainClass = "com.iktwo.kodex.renderer.ApplicationKt"

    // Rendering never opens a window; headless also keeps AWT from probing for a display server.
    applicationDefaultJvmArgs = listOf("-Djava.awt.headless=true")
}

ktor {
    fatJar {
        archiveFileName = "kodex-renderer.jar"
    }
}

// The fat jar is for Linux (the Docker image, amd64 and arm64); local runs use `run`. Skiko's macOS
// and Windows natives would add about 55 MB to it.
tasks.named<Jar>("shadowJar") {
    exclude("libskiko-macos-*", "skiko-windows-*")
}

dependencies {
    implementation(projects.kodices)
    implementation(projects.piktographs)

    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)

    // Skiko native runtimes. The host OS is for local runs and tests; both Linux variants are
    // always bundled so a single fat jar (and the Docker image) works on amd64 and arm64.
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.linux_arm64)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.ktor.client.cio)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.server.test.host)
}

// JetBrains publishes some androidx artifacts for desktop (lifecycle) as empty jars that only
// redirect to Google's artifact, which has the same file name. lib/ cannot hold both, so jars with
// nothing but META-INF entries are left out of the installDist/distZip/distTar layout. The fat jar
// merges classes and is unaffected.
distributions.main {
    contents {
        exclude { element ->
            element.name.endsWith(".jar") &&
                ZipFile(element.file).use { zip ->
                    zip.entries().asSequence().all { it.name.startsWith("META-INF/") }
                }
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("java.awt.headless", "true")
}

// The server resolves its default screens directory (KodexRenderer/screens) against the repository
// root, the same place the fat jar is normally launched from.
tasks.named<JavaExec>("run") {
    workingDir = rootDir
}

val renderSample by tasks.registering(JavaExec::class) {
    group = "application"
    description = "Renders a screen to KodexRenderer/build/render/ without starting the server. " +
        "Pass -Pscreen=<name> to pick a screen (default: dashboard)."

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.iktwo.kodex.renderer.RenderSampleKt"
    jvmArgs("-Djava.awt.headless=true")

    val screen = providers.gradleProperty("screen").orElse("dashboard")
    val screensDir = layout.projectDirectory.dir("screens")
    val outputDir = layout.buildDirectory.dir("render")

    inputs.dir(screensDir)
    inputs.property("screen", screen)
    outputs.dir(outputDir)
    // The output embeds the current date, so it is never up to date.
    outputs.upToDateWhen { false }

    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                screensDir.asFile.absolutePath,
                screen.get(),
                outputDir.get().asFile.absolutePath,
            )
        },
    )
}
