import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.skie)
    alias(libs.plugins.vanniktech.publish)
}

repositories {
    mavenCentral()
}

version = "0.5.0"

kotlin {
    // Every public declaration must state its visibility and return type, so the published
    // surface is deliberate rather than incidental.
    explicitApi()

    //region JVM
    jvm {
        // Java 17 bytecode. The toolchain still compiles on 21 for a reproducible build; without
        // this the bytecode would default to the toolchain and force every consumer onto JDK 21.
        // -Xjdk-release also limits the visible JDK API to 17, so a JDK 21-only method cannot slip
        // in and fail at runtime on a 17 consumer.
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    //endregion

    //region iOS/MacOS
    val frameworkName = "Kodices"
    val xcf = XCFramework(frameworkName)

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
    ).forEach {
        it.binaries.framework {
            binaryOption("bundleId", "com.iktwo.kodices")
            baseName = frameworkName
            xcf.add(this)
            isStatic = true
        }
    }
    //endregion

    linuxX64()
    mingwX64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

kover {
    useJacoco("0.8.13")

    reports {
        total {
            filters {
                includes {
                    classes("com.iktwo.kodices*")
                }

                excludes {
                    annotatedBy("*Generated*")
                }
            }

            html {
                onCheck = true
                htmlDir = layout.buildDirectory.dir("reports/html-result")
            }

            verify {
                onCheck = true

                rule {
                    bound {
                        minValue = 75
                        aggregationForGroup = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                    }
                }
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "kodices", version.toString())

    pom {
        name = "Kodices library"
        description = "Library to parse JSON models that describe user interfaces."
        inceptionYear = "2023"
        url = "https://github.com/iktwo/kodices"
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
