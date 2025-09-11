import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

//region plugins
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.skie)
    alias(libs.plugins.vanniktech.publish)
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
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    //endregion

    //region iOS/MacOS
    val frameworkName = "Kodices"
    val xcf = XCFramework(frameworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.serialization.json)
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
                        minValue = 60
                        aggregationForGroup = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                    }
                }
            }
        }
    }
}
//endregion

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
