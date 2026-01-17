rootProject.name = "KodicesProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":Kodices",
    ":Piktographs",
    ":SampleApp",
    ":KodexServer",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
include(":SampleApp:sample_android")
