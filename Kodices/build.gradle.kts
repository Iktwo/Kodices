import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

//region plugins
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
}
//endregion

//region repositories
repositories {
    mavenCentral()
}
//endregion

//region multiplatform configuration
kotlin {
    //region JVM
    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    //endregion

    //region iOS/MacOS
    val xcf = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
    ).forEach {
        it.binaries.framework {
            baseName = "Kodices"
            xcf.add(this)
        }
    }
    //endregion

    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
//endregion

//region kover
kover {
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
                        minValue = 70
                        aggregationForGroup = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                    }
                }
            }
        }
    }


}
//endregion
