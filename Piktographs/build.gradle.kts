import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.vanniktech.publish)
}

version = "0.4.0"

kotlin {
    jvmToolchain(21)

    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        namespace = "com.iktwo.piktographs"

        androidResources {
            enable = true
        }
    }

    jvm()

    val xcf = XCFramework()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "Piktographs"
            xcf.add(this)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core)
            implementation(libs.compose.ui.tooling)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
        }

        commonMain.dependencies {
            api(projects.kodices)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.material.icons.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.core)
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    task("testClasses")
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "piktographs", version.toString())

    pom {
        name = "Piktographs library"
        description = "Library to parse JSON models that describe user interfaces."
        inceptionYear = "2023"
        url = "https://github.com/Iktwo/Kodices/tree/main/Piktographs"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "iktwo"
                name = "Isaac SH"
                url = "https://github.com/iktwo/"
            }
        }
        scm {
            url = "https://github.com/iktwo/kodices"
            connection = "scm:git:git://github.com/iktwo/kodices.git"
            developerConnection = "scm:git:ssh://git@github.com/iktwo/kodices.git"
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.iktwo.piktographs"
    generateResClass = auto
}
