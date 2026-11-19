import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
}

version = "0.1.0"

// A standalone client for the content protocol (GET /screens, GET /content/{screen}.json): a
// desktop window, and a static web bundle a dashboard server can serve next to its API.
kotlin {
    jvmToolchain(21)

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "kodexViewer"

        browser {
            commonWebpackConfig {
                outputFileName = "kodexViewer.js"
            }

            // The tests are plain Kotlin and Compose UI tests of shared code; they run on the
            // desktop target. A browser run would only repeat them behind a Karma setup.
            testTask {
                enabled = false
            }
        }

        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        commonMain.dependencies {
            implementation(projects.kodexClient)

            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
        }

        desktopTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.iktwo.kodex.viewer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KodexViewer"
            packageVersion = "1.0.0"
        }
    }
}

// Renders a screen file as the viewer draws it, to a PNG: for checking a screen's layout at a
// window size without a server, and for documentation pictures.
//   ./gradlew :KodexViewer:renderScreen -Pfile=<screen.json> [-Pout=<file.png>] [-Psize=1280x900] [-Pdark]
val renderScreen by tasks.registering(JavaExec::class) {
    group = "application"
    description = "Renders a {\"content\", \"data\"} screen file to a PNG with the viewer's theme. " +
        "-Pfile=<screen.json> [-Pout=<file.png>] [-Psize=<width>x<height> in dp] [-Pdark]"

    val desktop = kotlin.targets.getByName("desktop").compilations.getByName("main")
    classpath = files(desktop.output.allOutputs, desktop.runtimeDependencyFiles)
    mainClass = "com.iktwo.kodex.viewer.RenderScreenKt"
    jvmArgs("-Djava.awt.headless=true")
    // Relative paths are the caller's, not this module's.
    workingDir = rootDir

    val file = providers.gradleProperty("file")
    val out = providers.gradleProperty("out")
    val size = providers.gradleProperty("size").orElse("1280x900")
    val dark = providers.gradleProperty("dark").map { true }.orElse(false)
    val defaultOut = layout.buildDirectory.dir("render")

    outputs.upToDateWhen { false }

    argumentProviders.add(
        CommandLineArgumentProvider {
            val source = file.orNull ?: throw GradleException("Pass the screen file: -Pfile=<screen.json>")
            val target = out.orNull
                ?: defaultOut.get().file(File(source).nameWithoutExtension + ".png").asFile.absolutePath

            listOfNotNull(source, target, size.get(), "--dark".takeIf { dark.get() })
        },
    )
}
