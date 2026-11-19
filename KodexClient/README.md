# KodexClient

The client side of the screens-as-JSON protocol that [KodexRenderer](../KodexRenderer/README.md)
serves: fetch a server's screens, render them with [Piktographs](../Piktographs/README.md), and keep
them fresh. It is what the [SampleApp](../SampleApp/README.md)'s Dashboard tab and
[KodexViewer](../KodexViewer/README.md) are built from. Kotlin Multiplatform (Android, JVM, iOS,
wasmJs); not published to Maven Central.

## The protocol

Any server can speak it; nothing here is specific to KodexRenderer.

| Request | Answer |
|---|---|
| `GET /screens` | `{"default": "overview", "screens": ["overview", "logs"]}` |
| `GET /content/{screen}.json` | `{"content": <Kodices content>, "data": {...}, "refresh_seconds": 10}` with an `ETag`; `304` when `If-None-Match` matches |

`401` means the bearer token is missing or wrong, `404` that there is no such screen, `422` that the
screen exists but its JSON is not valid (the body says why).

## Using it

```kotlin
val auth = DashboardAuth()
val httpClient = dashboardHttpClient(auth)          // needs a Ktor engine on the classpath
installDashboardImageLoader(httpClient)             // images on the server carry the token too

val controller = DashboardController(
    client = DashboardClient(httpClient, dashboardParser()),
    auth = auth,
    serverUrl = "http://localhost:8090",
)

@Composable
fun Screen() {
    Dashboard(controller)   // a connect form, then the server's screens
}
```

- `DashboardClient` does the two requests and parses the answers. Failures come back as a message
  for a person and a `DashboardFailure` for the UI (`Unreachable`, `Unauthorized`, `NotFound`,
  `InvalidScreen`...).
- `DashboardController` holds the state: connecting, the selected screen, and the polling loop,
  which waits `refresh_seconds` between requests (never less than 5 s), revalidates with the `ETag`,
  keeps the last good screen on a failure and retries within 30 s.
- `Dashboard` is the Compose UI. A `navigate` action in a screen opens the screen named by its
  `targetRoute`. A screen with a top-level [`tabs`](../KodexRenderer/screens/README.md#tabs) element
  brings its own navigation, so the list of screens is left out for it.
- Pass your own `KodicesParser` and `elementOverrides` to use custom elements and actions, as the
  SampleApp does.

## The token

`DashboardAuth` adds `Authorization: Bearer <token>` to requests whose scheme, host and port are the
dashboard server's, and to no others: the same HTTP client loads the images a screen refers to,
which can be anywhere. `dashboardDefaults` also turns redirects off, so a request to the server
cannot be sent on to another host. `DashboardConnection` and `DashboardUiState` leave the token out
of `toString()`. Where the token is kept between runs, if anywhere, is the host app's decision
(`onConnected` / `onDisconnected`); this module only holds it in memory.

## Tests

```shell
./gradlew :KodexClient:jvmTest
```

The client and the controller are tested against Ktor's `MockEngine` (the polling loop on virtual
time), the UI with Compose UI tests.
