plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktor)
}

group = "com.iktwo.kodices"
version = "0.0.1"

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64()
        hostOs == "Mac OS X" && !isArm64 -> macosX64()
        hostOs == "Linux" && isArm64 -> linuxArm64()
        hostOs == "Linux" && !isArm64 -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val nativeMain by creating {
            dependencies {
                implementation(project(":Kodices"))

                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.server.host.common)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.server.cio)

                implementation(libs.okio)
            }
        }
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "com.kodices.kodex.server.main"
            }
        }
    }
}
