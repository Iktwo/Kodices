<img src="resources/images/kodices.svg" alt="Kodices" style="width:50%; height:auto;">

# Kodices

[![License](https://img.shields.io/github/license/Iktwo/Kodices)](LICENSE)

Kodices is a Server-Driven UI framework for Kotlin Multiplatform. It allows you to define your user interfaces using JSON, which can be fetched from a server at runtime. This enables you to update your app's UI without shipping a new version to the app store. A reference implementation and an example that use Compose multiplatform are provided, however you can integrate Kodices with Native UI or other UI frameworks.

## Repository content

This repository contains different modules to help you get started:

```
+--------------+     +--------------------+     +-----------------+
|              |     |                    |     |                 |
|  KodexServer |---->|     Kodices        |---->|   Piktographs   |
| (JSON source)|     | (Core functonality)|     | (Reference UI)  |
|              |     |                    |     |                 |
+--------------+     +--------------------+     +-----------------+
```

*   **KodexServer**: A sample Ktor server that provides the JSON UI definitions. (*Note: This module is currently a work-in-progress.*)
*   **Kodices**: The core module that parses the JSON and creates a platform-agnostic model of the UI.
*   **Piktographs**: A reference implementation that renders the UI models using Jetpack Compose for Android, iOS, and Desktop.
*   **SampleApp**: An example application that demonstrates how to use these components together.

## Getting Started

To get started, clone the repository and build the project.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Iktwo/Kodices.git
    cd Kodices
    ```

2.  **Build the project:**
    This command will build all the modules and make them ready for use.
    ```bash
    ./gradlew build
    ```

3.  **Run the SampleApp:**
    The easiest way to see Kodices in action is to run the `SampleApp` on your desktop.
    ```bash
    ./gradlew :SampleApp:run
    ```
    You should see a window with the sample application, which demonstrates various features of the Kodices framework.

## Usage

You can add Kodices to your own Kotlin Multiplatform project by adding the following dependencies to your `build.gradle.kts` file.

```kotlin
dependencies {
    // Core parsing logic
    implementation("com.iktwo:kodices:<latest_version>")

    // Jetpack Compose UI rendering
    implementation("com.iktwo:piktographs:<latest_version>")
}
```
*Please replace `<latest_version>` with the latest version from the [releases page](https://github.com/Iktwo/Kodices/releases).*

Here is a minimal example of how to parse a JSON string and render it with `Piktographs`:

```kotlin
import com.iktwo.kodices.KodicesParser
import com.iktwo.piktographs.PageUI
import androidx.compose.runtime.Composable

@Composable
fun MyScreen(jsonString: String) {
    // 1. Initialize the parser
    val kodicesParser = KodicesParser()

    // 2. Parse the JSON string into a Content object
    val content = kodicesParser.parseJSONToContent(jsonString)

    // 3. Render the content using PageUI
    if (content != null) {
        PageUI(content)
    }
}
```

## Subprojects

*   [**Kodices**](Kodices/README.md): The core KMP library for parsing UI models.
*   [**Piktographs**](Piktographs/README.md): A reference UI implementation using Compose Multiplatform.
*   [**SampleApp**](SampleApp/README.md): A sample application demonstrating the usage of the library.
*   [**KodexServer**](KodexServer/README.md): A sample Ktor-based server for serving UI models.
