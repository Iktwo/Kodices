import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.vanniktech.publish)
}

version = "0.4.0"

kotlin {
    // Every public declaration must state its visibility and return type, so the published
    // surface is deliberate rather than incidental.
    explicitApi()

    jvmToolchain(21)

    // Guards the published ABI: `updateLegacyAbi` refreshes the checked-in dump,
    // `check` fails on any unreviewed change to the public surface.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
        klib.enabled.set(true)
    }

    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }

        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        namespace = "com.iktwo.piktographs"

        androidResources {
            enable = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }

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
            implementation(libs.kotlinx.coroutines.test)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        // Compose UI tests run on the desktop target; they need the current OS's skiko runtime.
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

tasks.named("check") {
    dependsOn("checkLegacyAbi")
}

kover {
    useJacoco("0.8.13")

    reports {
        total {
            filters {
                includes {
                    classes("com.iktwo.piktographs*")
                }
            }

            html {
                onCheck = true
                htmlDir = layout.buildDirectory.dir("reports/html-result")
            }

            // Reporting only, no bound yet: the UI test suite is new and a gate here would just be
            // a number to game. Add a `verify { }` block once the suite covers the render paths.
        }
    }
}

composeCompiler {
    // Marks the Kodices model types stable. They are immutable, but live in a module with no
    // Compose dependency, so they cannot be annotated at the source.
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose-stability.conf"))

    // Stability/recomposition report, off by default because it slows compilation.
    // Run with: ./gradlew :Piktographs:compileKotlinJvm -Pcompose.reports --rerun-tasks
    // Output:   Piktographs/build/compose-reports/
    if (project.hasProperty("compose.reports")) {
        reportsDestination = layout.buildDirectory.dir("compose-reports")
        metricsDestination = layout.buildDirectory.dir("compose-reports")
    }
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
