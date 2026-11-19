# KodexRenderer

Renders Kodices screens through [Piktographs](../Piktographs/README.md) **headlessly** to PNG
images, served over HTTP. It is built for e-ink dashboards: a device polls the server, downloads a
picture of the screen and shows it, similar to TRMNL but self-hosted. The reference device is a
reMarkable Paper Pro Move (954×1696 portrait).

It is a JVM module, not part of [KodexServer](../KodexServer/README.md): Compose UI cannot run on
Kotlin/Native. It is not published to Maven Central.

## Running

```shell
./gradlew :KodexRenderer:run                 # server on http://0.0.0.0:8090
./gradlew :KodexRenderer:renderSample        # writes KodexRenderer/build/render/dashboard*.png
./gradlew :KodexRenderer:renderSample -Pscreen=other
./gradlew :KodexRenderer:buildFatJar         # KodexRenderer/build/libs/kodex-renderer.jar (Linux only)
./gradlew :KodexRenderer:installDist         # KodexRenderer/build/install/KodexRenderer/bin/KodexRenderer
```

`renderSample` renders without starting the server and writes four files: the device default
(`dashboard.png`, gray16) plus `-bw`, `-color` and `-landscape` variants. A layout is designed for
one orientation, so the landscape image uses `<screen>-landscape.json` when it exists
([`dashboard-landscape.json`](screens/dashboard-landscape.json) is a two-column version).

The fat jar only carries the Linux Skiko natives (it is what the Docker image runs); on macOS or
Windows use `run` or `installDist`.

### Docker

The build context is the repository root, since the renderer depends on `:Kodices` and
`:Piktographs`. The Dockerfile uses BuildKit, so it needs `docker buildx`:

```shell
docker buildx build -f KodexRenderer/Dockerfile --platform linux/amd64,linux/arm64 -t kodex-renderer .
docker run -p 8090:8090 -v "$PWD/KodexRenderer/screens:/app/screens:ro" -e KODEX_TOKEN=change-me kodex-renderer
```

The fat jar carries the Skiko natives for linux-x64 and linux-arm64, so it is built once on the
build machine's architecture. The runtime image is `eclipse-temurin:21-jre` (glibc, since Skiko does
not run on Alpine/musl) plus `libfontconfig1`, `libgl1` and `libegl1`, which Skiko's native library
links against even though rendering is CPU-only. System fonts are deleted from the image on purpose.

## API

This is the contract the device uses. Keep it stable.

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | `200 ok`. Never needs a token. |
| `GET` | `/display?device=<id>` | JSON telling the device what to show and when to poll again. |
| `GET` | `/render/{screen}.png?width=&height=&mode=&density=` | The rendered screen. |
| `GET` | `/render/{screen}.json` | The screen's source JSON, for debugging. |

`/display` response:

```json
{"image_url": "/render/dashboard.png?width=954&height=1696&mode=gray16", "refresh_seconds": 900, "etag": "\"263ae482ae6bbff68338a20c178f9e5d\""}
```

`etag` is the exact `ETag` the image will be served with, so a device can compare it with the one
it is showing and skip both the download and the panel refresh. For now every device gets the
default screen.

`/render/{screen}.png`:

- `width`, `height`: 64–2000 px, default 954×1696. Use `width=1696&height=954` for landscape.
- `mode`:
  - `gray16` (default): grayscale quantized to 16 levels, written as an **8-bit grayscale** PNG.
  - `bw`: Floyd–Steinberg dithered to pure black and white (still an 8-bit grayscale PNG).
  - `color`: unprocessed RGB.
- `density`: pixels per dp, 0.5–4, default 2. At 2, a 1 dp line is 2 px, the thinnest the panel
  shows cleanly.
- Responses carry a strong `ETag` (a hash of the PNG bytes) and `Cache-Control: no-cache`. A
  matching `If-None-Match` gets `304 Not Modified`.
- Diagnostics: `X-Render-Millis` (how long the render took) and `X-Render-Cache` (`hit` when the
  image came from the 60 s render cache, `miss` when it was rendered for this request).
- Errors: `400` bad parameters, `401` missing or wrong token, `404` unknown screen, `422` the screen
  JSON is not valid Kodices content, `503` (with `Retry-After`) images did not load in time.

### Auth

If `KODEX_TOKEN` is set, `/display` and `/render` require `Authorization: Bearer <token>`.

## Configuration

Set in [`application.conf`](src/main/resources/application.conf); each value can be overridden by
an environment variable.

| Variable | Default | |
|---|---|---|
| `KODEX_PORT` | `8090` | |
| `KODEX_HOST` | `0.0.0.0` | |
| `KODEX_SCREENS_DIR` | `KodexRenderer/screens` | Relative to the working directory. `run` uses the repository root. |
| `KODEX_DEFAULT_SCREEN` | `dashboard` | What `/display` points at. |
| `KODEX_REFRESH_SECONDS` | `900` | |
| `KODEX_TOKEN` | *(empty: auth off)* | |
| `KODEX_TIMEZONE` | system default | For the `now` data, e.g. `America/Chicago`. |
| `KODEX_LOCALE` | `en-US` | For date formatting. Fixed rather than taken from the system, so output does not depend on the host. |
| `KODEX_DISPLAY_WIDTH` / `_HEIGHT` / `_MODE` / `_DENSITY` | `954` / `1696` / `gray16` / `2.0` | What `/display` asks for. |

## Adding a screen

Drop `<name>.json` into the screens directory; it is served as `/render/<name>.png`. Files are read
on every request, so no restart is needed. Names may contain only letters, digits, `-` and `_`.

A screen is either plain Kodices content (`{"elements": [...]}`) or an envelope with data for the
processors:

```json
{
  "content": { "elements": [ ... ] },
  "data": { "agenda": [ { "title": "Stand-up", "when": "09:30" } ] }
}
```

The renderer adds `now` to the data (truncated to the minute): `now.date` ("Wednesday, November 18,
2026"), `now.dateShort`, `now.weekday`, `now.time` ("14:05"), `now.time12`, `now.year` and `now.iso`.

```json
{ "type": "row", "id": "today", "processors": { "text": [{ "type": "path", "elements": ["now", "date"] }] } }
```

Images: `"url": "asset:logo.png"` loads `<screensDir>/assets/logo.png`, so a screen renders with no
network access; `http(s)://` URLs work too. See [`screens/dashboard.json`](screens/dashboard.json)
for a complete example.

## How rendering works

1. The JSON is parsed with `KodicesParser` and composed as `PageUI` inside `EinkTheme`: pure black on
   white, every Material color role collapsed to black or white, no style lighter than Normal,
   separators at 2 dp. Cards and progress indicators are drawn by the renderer itself
   (`EinkElements.kt`): outlined with 2 dp black strokes, since a card's shadow and a progress
   track's tonal color do not survive the panel.
2. An `ImageComposeScene` renders frames at a **frozen frame time** until every Coil image request
   has finished (loaded or failed) and nothing changed for three frames. A capture never contains a
   placeholder for an image that is still loading; if loading exceeds 10 s the request fails with 503.
3. The clock then jumps a fixed 10 s, so finite animations finish and infinite ones (indeterminate
   progress) are always captured at the same phase.
4. The pixels are converted for the panel (`mode`) and encoded as PNG.

**Deterministic output.** The same screen, data and parameters give byte-identical PNGs on the same
platform: fonts are bundled (Noto Sans; nothing falls back to system fonts), the clock is injected,
the locale is fixed, animations are frozen and there is no disk image cache. Renders inside the same
minute share a 60 s render cache.

## Notes

- Renders are serialized on one thread: Piktographs' `ImageUI` uses Coil's process-wide image
  loader, so concurrent renders would see each other's image requests. That also bounds memory.
- Measured on an Apple M-series laptop at 954×1696: about 0.5–0.9 s for the first render (JIT and
  font loading), 70–170 ms after. The Docker container used about 380 MB RSS after rendering.
- Fonts: Noto Sans, [SIL Open Font License 1.1](src/main/resources/fonts/NotoSans-OFL.txt). The
  license allows bundling and redistribution; it must ship with the font, which it does inside the jar.
