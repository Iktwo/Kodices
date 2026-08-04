# KodexServer

A sample [Ktor](https://ktor.io) server that serves the JSON UI definitions consumed by
[Kodices](../Kodices/README.md) and rendered by [Piktographs](../Piktographs/README.md).

> **Work in progress.** This module exists to demonstrate the "server" half of server-driven UI. It
> is not published to Maven Central and is not part of the library's public API.

## Running

The server is a Kotlin/Native executable. The target is chosen from the host OS at configuration
time, so no cross-compilation setup is needed:

```shell
./gradlew :KodexServer:runDebugExecutable<HostTarget>
```

For example, on Apple silicon:

```shell
./gradlew :KodexServer:runDebugExecutableMacosArm64
```

There is also a `KodexServer` run configuration under [`.run/`](../.run) for IntelliJ IDEA.

The server binds **127.0.0.1:8081**.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Returns `OK`. Useful to check the server is up. |
| `GET` | `/content` | Returns a sample UI definition as `application/json`. |
| `WS` | `/kodices` | WebSocket. Text frames are broadcast to every connected client. |

Fetch a UI definition and render it:

```shell
curl http://127.0.0.1:8081/content
```

```kotlin
val json = HttpClient().get("http://127.0.0.1:8081/content").bodyAsText()
val content = KodicesParser().parseJSONToContent(json)
```

## Layout

| File | Purpose |
|---|---|
| `src/nativeMain/.../Application.kt` | Entry point, binds the port and installs the modules below. |
| `src/nativeMain/.../plugins/Routing.kt` | HTTP routes. |
| `src/nativeMain/.../plugins/Sockets.kt` | WebSocket endpoint and broadcast. |
| `src/nativeMain/.../plugins/Serialization.kt` | Content negotiation. |
