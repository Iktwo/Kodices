# Kodices Sample App

This sample application demonstrates the capabilities of the Kodices framework. It showcases how to parse JSON UI definitions, render them with `Piktographs`, and extend the framework with custom components and actions.

## Running the App

You can run the sample app on Android, iOS, or Desktop.

### Android
Open the project in Android Studio and run the `SampleApp` configuration.

### iOS
Open `iosApp/iosApp.xcworkspace` in Xcode and run the `iosApp` scheme.

### Desktop
The easiest way to run the app is on the desktop using Gradle:
```bash
./gradlew :SampleApp:run
```

## Features

The sample app is organized into several tabs, each demonstrating a different aspect of the Kodices framework.

### Samples Tab
This tab showcases various UI examples loaded from local JSON files.

*   **Countdown:** A simple example of a dynamic UI that uses a custom `CountdownElement` to display a countdown timer.
*   **Wake-on-LAN (WoL):** A more advanced example that demonstrates a custom `WakeOnLANAction`. This allows the UI to trigger a platform-specific networking task.
*   **Catalog:** A comprehensive list of all the standard UI components available in `Piktographs`.

### Catalog Tab
This tab displays a catalog of all the standard elements available in the `Piktographs` library, loaded from `catalog.json`. It's a great way to see all the out-of-the-box components you can use.

### Dynamic Input Tab
This powerful tab allows you to edit the JSON UI definition in real-time and see the changes instantly. This is an excellent tool for prototyping, debugging, and learning how the Kodices JSON structure works.

### Dashboard Tab
A live dashboard fetched from a [KodexRenderer](../KodexRenderer/README.md) server. Start the server
(`./gradlew :KodexRenderer:run`), enter its address (`http://localhost:8090`; from an Android
emulator, `http://10.0.2.2:8090`) and connect. The tab lists the server's screens, renders the
selected one from `/content/<screen>.json`, and fetches it again as often as the server asks. It is
the app counterpart of an e-ink device showing the same screen as a picture. [KodexViewer](../KodexViewer/README.md) is the
same dashboard as an app of its own, for the desktop and the browser.

> Screenshots of these tabs have not been captured yet. Run the sample to see them:
> `./gradlew :SampleApp:run` for desktop, `./gradlew :SampleApp:sample_android:installDebug` for
> Android, or open `SampleApp/iosApp/iosApp.xcodeproj` for iOS.

## Code Tour

To help you understand how the sample app is built, here are some key files to look at:

*   `SampleApp/src/commonMain/kotlin/com/iktwo/kodices/sampleapp/App.kt`: The main entry point of the Compose application. This is where the `KodicesParser` is initialized with custom elements and actions.
*   `SampleApp/src/commonMain/composeResources/files/`: This directory contains all the JSON UI definitions used in the app.
*   `SampleApp/src/commonMain/kotlin/com/iktwo/kodices/sampleapp/TabDashboard.kt`: The Dashboard tab, which is [KodexClient](../KodexClient/README.md)'s dashboard with the sample's parser and element overrides. Fetching screens, polling with `ETag`s and sending the server's token only to that server live in that module.
*   `SampleApp/src/commonMain/kotlin/com/iktwo/kodices/sampleapp/actions/`: This directory contains the implementation of the custom `WakeOnLANAction`.
*   `Piktographs/src/commonMain/kotlin/com/iktwo/piktographs/elements/`: This directory in the `Piktographs` module contains the implementation of the custom `CountdownElement` and `WebElement`.
