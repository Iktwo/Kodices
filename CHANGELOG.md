# Changelog

`Kodices` and `Piktographs` are versioned independently.

## Kodices

### 0.5.0 - unreleased

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
