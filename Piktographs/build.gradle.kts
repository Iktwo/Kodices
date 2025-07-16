import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

group = "com.iktwo"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvmToolchain(21)

    androidTarget()

    jvm("desktop")

    val xcf = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "Piktographs"
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":Kodices"))

            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.lifecycle.viewmodel)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core)
                implementation(libs.kotlinx.coroutines.android)
                implementation(compose.preview)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.preview)
            }
        }
        val desktopTest by getting
    }

    task("testClasses")
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    namespace = "com.iktwo.piktographs"

    dependencies {
        implementation(libs.accompanist.webview)
    }
}
