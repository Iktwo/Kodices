pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "KodicesProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":Kodices",
    ":Piktographs",
    ":SampleApp",
    ":SampleApp:sample_android",
    ":KodexRenderer",
    ":KodexClient",
    ":KodexViewer",
)
