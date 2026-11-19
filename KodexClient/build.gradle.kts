import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
}

// The client side of KodexRenderer's content protocol (GET /screens, GET /content/{screen}.json),
// shared by the SampleApp's Dashboard tab and by KodexViewer. Not published.
kotlin {
    jvmToolchain(21)

    android {
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        namespace = "com.iktwo.kodex.client"
    }

    jvm()

    iosArm64()
    iosSimulatorArm64()

    // Tests run on the JVM; the browser tests are off. Compose Multiplatform 1.13 still fails the
    // build for a wasmJs target with Compose UI tests and no executable binary (CMP-4906), hence
    // the binary, as in :Piktographs.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.piktographs)
            api(libs.ktor.client.core)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.icons.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        // Compose UI tests run on the desktop target; they need the current OS's skiko runtime.
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
