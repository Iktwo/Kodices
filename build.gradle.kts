buildscript {
    dependencies {
        classpath(libs.gradle)
    }
}
plugins {
    alias(libs.plugins.allopen).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.benchmark).apply(false)
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kover).apply(false)
    alias(libs.plugins.ktlint).apply(false)
    alias(libs.plugins.ktor).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.jvm).apply(false)
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.skie).apply(false)
    alias(libs.plugins.vanniktech.publish).apply(false)
}

allprojects {
    group = "com.iktwo"
    version = "0.1.2"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        google()
        mavenCentral()
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)

        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}
