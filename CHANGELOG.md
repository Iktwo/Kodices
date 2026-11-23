# Changelog

`Kodices`, `Piktographs` and `KodexRenderer` are versioned independently.

## Build

### Unreleased

- Toolchain and dependency bump: Gradle 9.9.1, Kotlin 2.4.21, Compose Multiplatform 1.13.0-rc01
  (CMP material3 1.13.0-rc01), AGP 9.5.1, Ktor 3.6.1, Coil 3.6.3, kotlinx-serialization 1.13.0,
  androidx core 1.19.1 / appcompat 1.8.0 / datastore 1.3.0, okio 3.18.2, Kover 0.9.11, SKIE 0.10.16,
  JaCoCo 0.8.16. Compose Multiplatform is a release candidate: move to 1.13.0 once it is final.
- Removed the `maven.pkg.jetbrains.space/public/p/compose/dev` plugin repository. JetBrains Space is
  shut down and the host no longer resolves, which made Gradle fail to resolve the build classpath
  (`No target component for edge ... commons-lang3`) before any task ran.
- ABI validation is back on for `:Kodices` and `:Piktographs`, using the Kotlin 2.4 DSL
  (`abiValidation()`; the `enabled`/`klib.enabled` properties it used before no longer exist). `check`
  runs `checkKotlinAbi`; refresh the dumps with `./gradlew updateKotlinAbi`. The dumps in `api/` had
  not been updated since validation was switched off, so they now also record the changes made in
  the meantime, including binary-incompatible ones to `PageUI`, `ElementUI`, `NestedElementUI`
  (new `modifier` parameters) and `SimpleAction` (now `open`, with a `jsonValues` constructor).
- Karma (JS/Wasm browser tests) listens on `127.0.0.1` only (`karma.config.d/loopback.js`). On every
  interface, it started even when another process held `localhost:9876`, and ChromeHeadless then
  connected to that process and timed out without running any test.
- SampleApp: new **Dashboard** tab, a client for KodexRenderer's `/content` endpoints (screen list,
  polling with `ETag`, bearer token sent to that server only, `navigate` between screens). The
  catalog shows the new dashboard elements. `./gradlew :SampleApp:run` failed with
  `ClassNotFoundException`: `mainClass` was `MainKt` without its package.
- New module **KodexClient** (not published): the client side of the content protocol, taken out of
  the SampleApp so it has one implementation. `DashboardClient` (screen list, `ETag` polling),
  `DashboardAuth` (bearer token for the dashboard server only; now one instance per HTTP client
  instead of a process-wide object, and redirects are not followed), `DashboardController` (connect,
  poll, select, refresh) and the `Dashboard` composable. Failures carry a kind
  (`DashboardFailure`: unreachable, unauthorized, not found, invalid screen...), which the UI uses to
  offer "Change token" or "Retry". A screen with its own `tabs` hides the client's screen chips, and
  the chips scroll instead of overflowing. The SampleApp's Dashboard tab is now this.
- **KodexServer is retired.** It served one hard-coded screen at `/content` and a WebSocket broadcast
  that no client in the repository used. KodexRenderer is the sample server now: it serves a folder
  of screens as JSON (`/screens`, `/content/<screen>.json`) and as PNG. The module, its IDE run
  configuration and the two version catalog entries only it used (`ktor-server-host-common`,
  `ktor-server-websockets`) are gone.
- KodexClient: the token field is a `OutlinedSecureTextField` instead of a text field with the
  deprecated `PasswordVisualTransformation`.
- SampleApp: every screen has `@Preview`s (`SampleAppPreviews.kt`), and so does KodexViewer
  (`ViewerPreviews.kt`). To make that possible the screens are split into a part that loads and a
  part that draws (`AppScaffold`, `SampleList`, `SamplePage`, `ResourceContent`). The tab row
  scrolls, because "Dashboard" broke mid-word at phone width; the JSON editor marks text that is
  invalid when it arrives; the countdown compares its target with the instant it is given, not the
  system clock.
- SampleApp: the Android `web` element is the platform `WebView` in an `AndroidView`; accompanist-web
  is deprecated and unmaintained, and is no longer a dependency. The countdown says "1 month", not
  "1 months", and "18 days ago" for a target in the past instead of "-18 days". `LastSecond` is
  `LocalLastSecond`. `sample_android` no longer depends on `ktor-client-websockets`, which nothing
  used.
- KodexViewer: the browser session code opts in to `ExperimentalWasmJsInterop` instead of warning.
- Sample screens, the catalog and the guides use `container` in place of `hrow` / `vrow`.
- Kodices README: the Wake-on-LAN example's JSON used the action type `WakeOnLAN` while the
  descriptor above it registers `wol`.
- New module **KodexViewer**: a standalone viewer for any server speaking the content protocol.
  Desktop (`./gradlew :KodexViewer:run --args="--url ... --token ..."`, or `KODEX_URL` /
  `KODEX_TOKEN`) and a static web bundle (`./gradlew :KodexViewer:wasmJsBrowserDistribution`) that
  defaults to the origin it is served from and keeps the token in `sessionStorage` only.
  `./gradlew :KodexViewer:renderScreen -Pfile=<screen.json>` renders a screen file to a PNG with the
  viewer's theme at any window size. See [KodexViewer](KodexViewer/README.md).
- KodexServer: package renamed `com.kodices.kodex.server` -> `com.iktwo.kodex.server` to match the
  rest of the repository; depends on `projects.kodices` (typesafe accessor); the `KodexServer` IDE
  run configuration called a task that does not exist (`runDebugExecutableNative`).

## Kodices

### 0.7.0 - unreleased

**Added**

- `emptyElement` on an element with `expandWithProcessors`: what to show in its place when the
  array is empty or missing ("No alerts"). It is processed with the same data as the element.
  `InterimElement` has a new `emptyElement` property (constructor, `copy` and `componentN` change).
- `MapProcessor` (`"map"`): replaces a value with the one listed for it, with an optional
  `default`: `{ "type": "map", "values": { "0": "neutral" }, "default": "warning" }`. Registered by
  default.

**Fixed**

- A nested element's `constants` and `processors` were silently dropped when its parent declared
  neither (`{"type": "grid", "nestedElements": [{"type": "text", "constants": {...}}]}` rendered
  empty children). Such a parent is now processed like one with constants, keeping the values it
  declares directly on itself.
- An element with `expandWithProcessors` lost its `action`/`actions` on every copy, so a list built
  from data could not be clickable. Each copy now keeps them, processed with its own entry.

### 0.6.0

**Added**

- Dynamic action payload preservation in `SimpleAction`: unmapped actions now retain their raw JSON properties in `jsonValues`, allowing generic action handlers to inspect action parameters without custom descriptors.
- Added `asFloatOrNull()` and `asDoubleOrNull()` JSON parsing helper extensions.
- Added structural `equals` and `hashCode` implementation to `ProcessedElement`, enabling Compose recomposition optimizations.
- Added `switch` and `toggle` input element constants to `InputProvider`.

**Fixed**

- Cached regex pattern matching in `Validation.kt` with `runCatching` to prevent crashes on invalid server regexes.

### 0.5.0

**Breaking**

- Element, action and data processor types now resolve against a per-parser `KodicesRegistry`
  instead of the process-global `ElementRegistry` / `ActionsRegistry` / `DataProcessorRegistry`.
  Two parsers configured with different descriptors no longer see each other's types.
  Build one with `KodicesRegistry.of(...)` and pass it to `KodicesParser(registry = ...)`.
  The three global registries are deprecated but still work: `KodicesParser(elements, actions)` also
  writes into them, and a type missing from a parser's own registry still falls back to the global
  one with a warning. Both the dual-write and the fallback are removed in 1.0.
- `KodicesParser.debug` and `KodicesParser.logger` are deprecated. Pass `logger` and `debug` to
  `KodicesParser(registry, json, logger, debug)` instead.
- `InterimAction.process(data)` is deprecated in favour of `process(data, json)`, which resolves the
  action against the parser's registry rather than the global one.
- `explicitApi()` is enabled. `CommonElementProperties` and
  `Map<String, JsonElement?>.toCommonElementProperties` are now `internal`.
- `ProcessedElement.jsonValues` is now an immutable `Map<String, JsonElement?>` rather than a
  `MutableMap`. This also makes `ProcessedElement` a stable Compose parameter.
- Removed `DialogAction` (its constructor argument was discarded and it was never registered),
  `ProcessedAction` (an exact duplicate of `SimpleAction`), `Constants.jsonPrettyPrinter`, and the
  scratch `main()` that was shipping in the JVM artifact.
- The `ElementBuilder` identifier parameter is renamed `id` -> `elementId`. Function-type parameters
  are positional in Kotlin, so calling code is unaffected.

**Changed**

- JVM and Android artifacts now target **Java 17** bytecode instead of Java 21. The build still
  compiles on JDK 21 for reproducibility; only the emitted target changed, so consumers no longer
  need JDK 21. `-Xjdk-release=17` keeps a JDK 21-only API from compiling and failing at runtime.
- Documented the real requirements: Kotlin 2.3+ on every target (the klibs carry `abi_version=2.3`,
  which older compilers cannot read) and JDK 17+ on the JVM.

**Fixed**

- The iOS/macOS frameworks failed to link. The `ElementBuilder` parameter named `id` shadowed the
  Objective-C `id` keyword in the generated header, so `swiftc` could not compile it. This blocked
  every Apple target, including the `Package.swift` binary target.
- Elements without an `id` all received the literal id `"id"`, which renderers using it as a list
  key reject as a duplicate. Ids are now generated from the element's type and position, matching
  the scheme already used elsewhere.
- Elements produced by `expandWithProcessor` all shared the declared `id`. Each expanded copy is now
  suffixed with its index.
- `"visible": false` was ignored on elements that declare neither processors nor constants.
- `StringProcessor` substituted array values in ascending index order, so `%1` was replaced inside
  `%10`. Tokens are now matched in a single pass, and a token with no matching entry is left as-is.
- Malformed data processor JSON threw `IllegalArgumentException` while building the error message
  instead of the intended `DataProcessorException`.
- `parseJSONWithDataToContent` swallowed every failure silently. All three parse entry points now
  report failures through the configured `Logger` rather than `println`.

### 0.4.0 - unreleased

Superseded by 0.5.0; the fixes above were developed together and released as one version.

### 0.1.1 - 2025-08-22

_Initial config for maven-publish._

### 0.1.0 - 2025-08-21

_First release._

## Piktographs

### 0.9.0 - unreleased

**Changed**

- **One name per layout concept.** A row or column is `"type": "container"`, a row unless it says
  `"orientation": "vertical"`. The other spellings still render but are deprecated, log a warning
  that says what to write instead, and are removed in 1.0:
  - types `hrow`, `vrow`, `horizontal_layout`, `vertical_layout` (and their `*_ELEMENT_TYPE`
    constants, now `@Deprecated`);
  - keys `direction` (use `orientation`), `horizontalPadding` / `verticalPadding` (use
    `paddingHorizontal` / `paddingVertical`);
  - values `left` / `right` (use `start` / `end`), `spacebetween` and the like (use
    `space_between`), `center_vertically` / `center_horizontally` (use `center`), and `column` /
    `vrow` as an orientation (use `vertical`).
- New `LocalLogger` (a Kodices `Logger`): where the renderer reports what it only learns while
  drawing, such as the deprecated names above. It prints warnings and errors unless a host provides
  its own.
- `ContentBottomSheet` uses `rememberBottomSheetState` in place of the deprecated
  `rememberModalBottomSheetState`; it still opens fully, with no half-open state.
- `CountdownElement` no longer logs through the deprecated global `KodicesParser.logger`. A target
  that is not a date and time throws `IllegalArgumentException` naming the value, which the parser
  reports through its own logger.
- **Renamed** the theme CompositionLocal `DefaultTheme` to `LocalTheme`, the Compose naming for a
  CompositionLocal. `DefaultTheme` stays as a deprecated alias.
- Built against Compose Multiplatform 1.13.0-rc01 and Kotlin 2.4.21.
- `ContentDialog` on JVM/desktop is an in-window `Dialog`, like on every other platform, instead of a
  separate OS `DialogWindow`. The OS window could not be driven by Compose UI tests or used in a
  headless scene: since Compose Multiplatform 1.12, touching it off the event dispatch thread throws.
- `ContentDialog` now shows its content on a surface with `title` at the top, on every platform
  (the same arrangement as `ContentBottomSheet`). The title used to appear only on desktop, as the
  window title.
- `elementOverrides` (on `PageUI` and `KodicesNavHost`) is now consulted for every element, built-in
  types included, before Piktographs' own rendering. It used to be called only for types Piktographs
  did not know, so a host could not restyle, say, a card. An override that returns `false` for
  built-in types (like the sample app's) behaves as before.
- `Colors.mainTextColor` and `Colors.secondaryTextColor` default to `Color.Unspecified`, meaning
  "follow the Material theme". They were black and near-black, but nothing used them.

**Added**

- Dashboard elements:
  - `TextUI` (`"text"`): text in any Material type scale style, with `align`, `fontWeight`,
    `maxLines` and `color`.
  - `StatUI` (`"stat"`): a figure with a label, `unit`, caption, `trend` arrow and `icon`.
  - `ListItemUI` (`"listItem"`): leading `icon`, title, subtitle, trailing `value` and
    `trailingIcon`; clickable with actions.
  - `BadgeUI` (`"badge"`): a `filled` or `outlined` pill.
  - `GridUI` (`"grid"`): nested elements in equal-width `columns`.
  - `ChartUI` (`"chart"`): one series of `values` as a `line` or `bar` chart, with `labels`,
    `min`/`max` and `height`; its values are exposed as a content description.
  - Values these elements show or compute with accept a JSON number or a numeric string.
  - Icon names: `calendar`, `email`, `phone`, `check_circle`, `arrow_up`, `arrow_down`, `build`,
    `share`.
  - Previews for the new elements in `commonMain` (`ui/previews/DashboardElementsPreview.kt`).
- Elements for dense, data-driven screens (logs, tables of devices, status pages):
  - `tone` (`info`, `success`, `warning`, `error`; also log-level names) on `text`, `badge`, `stat`,
    `listItem`, `chart` and `tableRow`. The colors are `Colors.infoColor`, `successColor`,
    `warningColor` and `errorColor`; unset, they follow the Material theme. `Colors` has four new
    fields (constructor, `copy` and `componentN` change).
  - `TableHeaderUI` (`"tableHeader"`) and `TableRowUI` (`"tableRow"`): rows with shared `columns`
    (`key`, `label`, `width`/`weight`, `align`, `display` = `text`/`mono`/`badge`/`icon`,
    `secondaryKey`, `toneKey`, `maxLines`, `primary`). A row is its own element, so a page stays
    lazy over hundreds of them. Under `collapseBelow` dp of width (default 600) a row stacks and
    labels its values, and the header disappears.
  - `TabsUI` (`"tabs"`): links between screens with the current one marked. `PageUI` keeps a
    top-level one above the scrolling content.
  - `grid`: `minColumnWidth` fits as many columns as the width allows.
  - `listItem`: `badge`, `dense`, `variant: tonal`. `badge`: `variant: tonal`; a blank badge draws
    nothing. `stat`: `size` (`large`/`medium`/`small`), clickable with actions. `text`:
    `fontFamily` (`monospace`, `serif`, `sans`).
  - Previews in `ui/previews/OperatorElementsPreview.kt`.
- `chart`: fewer `labels` than bars (4 under 24 hourly bars) now start at the bar they mark instead
  of sitting in the middle of their stretch.
- A JSON `null` in `constants` is no value, not the text "null" (`stat`, `listItem` and the other
  dashboard elements).

**Fixed**

- `PageUI` seeds its input maps for every input on the page, not only the top-level ones. An input
  inside a `card`, `container` or `grid` was missing from `textInputData` until the user typed in
  it, so a host restoring or submitting a form in `onInputIdsPopulated` did not see it.
  `booleanInputData` is seeded as well (from `active`, else `false`), so an untouched `checkbox` or
  `switch` has a value in the map. Keys a host already set are left alone.
- Two inputs with the same id share one value, because the maps are keyed by id. `PageUI` now
  reports each such id through `LocalLogger`.
- `RowUI` now takes its font family from `LocalTextStyle`, so a host's typography applies to rows
  like it does to every other element. It used to build a bare `TextStyle`, which always resolved
  the platform default font. Size and line height are unchanged.
- `RowUI` uses `Theme.colors.mainTextColor` / `secondaryTextColor` when they are set, instead of
  always using the Material colors.
- The JVM `RowUIPreview` used the deprecated `androidx.compose.desktop.ui.tooling.preview.Preview`.

### 0.8.0

**Added**

- Core UI Primitives:
  - `SwitchUI` (`"switch"`, `"toggle"`): Material 3 animated toggle integrated with `LocalBooleanInputData` and `InputHandler`.
  - `IconUI` (`"icon"`): Material Icons resolution (`settings`, `favorite`, `star`, etc.) with custom sizes, tint colors, and click actions.
  - `ImageUI` enhancements: added `width`, `height`, `aspectRatio`, `contentScale` (`crop`, `fit`, `inside`, `fillBounds`), `cornerRadius`, `alpha`, and clickable action dispatching.
- True Horizontal Layout System:
  - `ContainerUI` (`"hrow"`, `"container"`, `"horizontal_layout"`, `"vrow"`, `"vertical_layout"`): Compose `Row` / `Column` supporting horizontal and vertical arrangements, alignments, and per-child `weight`.
  - `ElementUI` and `NestedElementUI` now support an optional `modifier: Modifier = Modifier` parameter for parent layout constraint propagation.
- Server-Driven Modals & Bottom Sheets:
  - Added `ContentBottomSheet` (platform-agnostic Material 3 `ModalBottomSheet` wrapper).
  - Added `ModalAction` (`"modal"`) and `DismissModalAction` (`"dismiss_modal"`).
  - Enhanced `NavigateAction` with `presentation: String = "push"` (`"dialog"`, `"bottom_sheet"`) and `title: String?`.
  - Full modal overlay handling in `KodicesNavHost` and `KodicesNavController`, with automatic dismissal on `back`, `close`, or `dismiss_modal`.

**Fixed**

- Fixed empty input validation state bug in `TextInputUI` where initially blank inputs failed to enter the error state.
- Aligned `TextAreaUI` error state and `fillMaxWidth` styling with `TextInputUI`.
- Fixed `TOP_BAR_ELEMENT_TYPE` duplicate slot allocation in `PageUI.renderElements`.
- Renamed typo `SeperatorUI` to `SeparatorUI` with `@Deprecated` backwards-compatible alias.

### 0.7.0

**Added**

- Added built-in Material 3 components: `ButtonUI` (supporting filled, outlined, text, elevated variants, and loading spinners), `CardUI` (supporting elevated, outlined, and filled cards), and `ProgressUI` (supporting linear and circular progress indicators).
- Added `KodicesNavHost` and `KodicesNavController` for multi-page server-driven UI navigation with automatic handling for `NavigateAction` ("navigate") and "back" actions, custom payload fetching, loading states, and error recovery with retry.
- Added `LocalActionPerformer` and `ActionPerformer` factory function to automatically dispatch element actions on user interaction.

### 0.6.0 - unreleased

**Breaking**

- `explicitApi()` is enabled.
- `LazyColumn` no longer re-applies the caller's `modifier`, which was already applied to the
  `Scaffold`. Callers passing sizing or padding modifiers will see a layout change - this matches
  what the horizontal page style already did.
- `UnknownElementUI` no longer renders nothing when `KodicesParser.debug` is false. It now always
  renders a placeholder naming the unsupported type; control this with
  `LocalUnknownElementPlaceholder`.
- Removed the unused `Refreshable` and `BackNavigator` interfaces.

**Fixed**

- Input elements lost whatever the user had typed whenever `PageUI` recomposed: the seeding of
  `textInputData` and `validityMap` ran in the composition body on every pass. It now runs once per
  `Content`, in an effect, and leaves existing entries alone.
- `elementOverrides` has a default, so `PageUI(content)` - the example in both READMEs - compiles.
- `Modifier.wipOverlay` ignored a changed `textColor` after first composition.

**Changed**

- JVM and Android artifacts now target **Java 17** bytecode instead of Java 21, so consumers no
  longer need JDK 21. See the Kodices entry for details.
- `ProcessedElement` and `Content` are declared stable to the Compose compiler, so `ElementUI` and
  its subtree are now skippable.
- The `title` parameter of `ContentDialog` is documented as desktop-only; the Android, iOS and web
  actuals ignore it.

### 0.5.0 - unreleased

Superseded by 0.6.0; the fixes above were developed together and released as one version.

### 0.4.0 and earlier

Released alongside Kodices; see the Kodices entries above.

## KodexRenderer

### 0.1.0 - unreleased

**Added**

- New JVM module that renders Kodices screens through Piktographs headlessly (`ImageComposeScene`)
  to PNG for e-ink displays, served by Ktor (CIO):
  - `GET /health`, `GET /display?device=`, `GET /render/{screen}.png`, `GET /render/{screen}.json`.
  - Strong `ETag` (hash of the PNG bytes), `Cache-Control: no-cache`, `304` on `If-None-Match`.
  - `gray16` (8-bit grayscale, 16 levels), `bw` (Floyd–Steinberg) and `color` modes; bounded
    `width`/`height` for landscape.
  - Waits for Coil image loads before capturing; frozen frame clock, bundled Noto Sans, injected
    clock and fixed locale for byte-identical output.
  - High-contrast `EinkTheme`.
  - Optional bearer-token auth (`KODEX_TOKEN`); configuration through `application.conf` and env vars.
  - `renderSample` Gradle task, sample `dashboard.json` screen, multi-arch `Dockerfile`.
  - E-ink renderings of cards (outlined, no shadow or tonal fill, for every variant) and progress
    indicators (outlined track with a solid fill, no stop dot), through `elementOverrides`.
  - `X-Render-Cache: hit|miss` header next to `X-Render-Millis`.
  - `dashboard-landscape.json`, used by `renderSample` for the landscape image.
  - Screens as JSON for clients that render them themselves: `GET /screens`,
    `GET /content/{screen}.json` (`{"content", "data", "refresh_seconds"}`, with `now` merged into
    the data and `asset:` URLs rewritten to the server, `ETag`/`304`) and `GET /assets/{path}`.
    `KODEX_PUBLIC_URL` sets the address used in asset URLs. The `/display` and `/render` contract
    is unchanged.
  - Sample screens `home.json` and `status.json`; `dashboard.json` now uses the stat, chart and
    list item elements.
  - [Building a dashboard](KodexRenderer/screens/README.md): an authoring guide with the JSON for
    every dashboard element.
  - Every `tone` is black in the e-ink theme.
  - The fat jar carries only the Linux Skiko natives (65 MB instead of 86 MB). `installDist`,
    `distZip` and `distTar` work: JetBrains' empty redirect jars are left out of `lib/`.
