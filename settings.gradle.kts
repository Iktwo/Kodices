pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "KodicesProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":Kodices",
    ":Piktographs",
    ":SampleApp",
    ":SampleApp:sample_android",
    ":KodexServer",
)
