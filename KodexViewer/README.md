# KodexViewer

A standalone viewer for servers that serve [Kodices](../Kodices/README.md) screens as JSON: it lists
the server's screens, renders the selected one natively with [Piktographs](../Piktographs/README.md)
and fetches it again as often as the server asks. Use it for the screens of
[KodexRenderer](../KodexRenderer/README.md), or as the operator dashboard of your own service: the
service describes its screens and fills in the data, and needs no UI code of its own.

It runs as a desktop window (JVM) and in a browser (WebAssembly). The protocol and the client code
are [KodexClient](../KodexClient/README.md)'s.

## Desktop

```shell
./gradlew :KodexViewer:run                                                    # opens the connect form
./gradlew :KodexViewer:run --args="--url http://127.0.0.1:8089 --token s3cret"  # connects at once
KODEX_URL=http://127.0.0.1:8089 KODEX_TOKEN=s3cret ./gradlew :KodexViewer:run
```

| Argument | Environment | |
|---|---|---|
| `--url <address>` | `KODEX_URL` | The server. With one, the viewer connects at once; without, it opens the form on `http://localhost:8090` |
| `--token <token>` | `KODEX_TOKEN` | The server's bearer token, if it has one |
| `--help` | | |

Arguments win over the environment. Prefer `KODEX_TOKEN` to `--token`: arguments are visible to
every user of the machine in the process list.

`./gradlew :KodexViewer:packageDistributionForCurrentOS` builds an installer for the current OS.

## Web

```shell
./gradlew :KodexViewer:wasmJsBrowserDistribution
# KodexViewer/build/dist/wasmJs/productionExecutable/   index.html, kodexViewer.js, *.wasm
```

The output is a directory of static files. Serve it from the **same origin as the API** (the server
answering `/screens` and `/content/...` also serves these files at `/`): the viewer then connects to
the origin it was loaded from and only asks for the token.

- The static files must be readable **without** the token: a browser cannot attach one to the page
  and script requests. Only `/screens`, `/content/...` and whatever the screens' images need are
  authenticated.
- `.wasm` files must be served as `application/wasm`.
- The token is kept in the tab's `sessionStorage`, so a reload does not ask again, and removed by
  **Disconnect**. It is never put in the URL or in `localStorage`, and it is only sent to the
  configured server (see [KodexClient](../KodexClient/README.md#the-token)).
- Served from somewhere else, the address in the form can be changed; that server then has to allow
  the page's origin with CORS and expose the `ETag` header.
- The browser has no system fonts, so `monospace` text is drawn in the default font there.

For development, `./gradlew :KodexViewer:wasmJsBrowserDevelopmentRun` serves the viewer with reload.

## What it shows

A status bar (server, when the screen was last updated, refresh, disconnect), then the screen. A
screen with a top-level [`tabs`](../KodexRenderer/screens/README.md#tabs) element navigates with its
own tabs; for any other screen the status bar lists the server's screens. When a request fails the
screen that was showing stays, under a banner that says what happened: the server is unreachable,
the token was refused (`401`, with a way to change it), or the screen's JSON is not valid (`422`,
with the server's explanation).

The theme is Material 3, light or dark with the system.

## Checking a screen without a server

```shell
./gradlew :KodexViewer:renderScreen -Pfile=KodexRenderer/screens/status.json
./gradlew :KodexViewer:renderScreen -Pfile=my-screen.json -Pout=/tmp/phone.png -Psize=400x900 -Pdark
```

Renders a `{"content": ..., "data": ...}` file to a PNG exactly as the viewer draws it (default
`KodexViewer/build/render/<name>.png`, 1280x900 dp). The picture is as tall as you ask, so give a
long screen a tall size to see all of it. For the e-ink rendering of the same file use
`:KodexRenderer:renderSample`.

## Tests

```shell
./gradlew :KodexViewer:desktopTest
```
