rootProject.name = "KodicesProject"

include(
    ":Kodices",
    ":Piktographs",
    ":SampleApp",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
