# Piktographs Module

Piktographs is a reference implementation that renders UI models from the `Kodices` module using [Jetpack Compose](https://www.jetbrains.com/lp/compose-multiplatform/). It provides a set of pre-built Composables for common UI elements, as well as a mechanism for customizing the look and feel of your server-driven UI.

## Requirements

| | |
|---|---|
| Kotlin | 2.3+ |
| JDK (JVM target) | 17+ |
| Android | minSdk 28 |

Kotlin 2.3 is a hard floor, not a preference: the Kotlin/Native, JS and wasm artifacts are klibs
carrying `abi_version=2.3`, and an older compiler cannot read them. It fails as
`Missing stdlib class` / `KLIB resolver: Could not find ...` rather than a clear version error, so
check this first if resolution behaves strangely.

## Usage

To use Piktographs in your Compose Multiplatform app, you first need a `Content` object from the `KodicesParser`. Then, you can pass this object to the `PageUI` Composable.

```kotlin
import com.iktwo.kodices.KodicesParser
import com.iktwo.piktographs.PageUI
import androidx.compose.runtime.Composable

@Composable
fun MyScreen(jsonString: String) {
    val kodicesParser = KodicesParser()
    val content = kodicesParser.parseJSONToContent(jsonString)

    if (content != null) {
        PageUI(content)
    }
}
```

## Elements

`PageUI` renders these `type`s. Values are read from the element's `constants` (or `processors`).

| Type | What it is | Keys beyond `text` / `textSecondary` |
|---|---|---|
| `row` | Title and subtitle | |
| `text` | Text in a Material type scale style | `style`, `align`, `fontWeight`, `fontFamily`, `maxLines`, `tone`, `color` |
| `stat` | A figure with a label, unit and caption | `value`, `unit`, `trend`, `icon`, `align`, `size`, `tone` |
| `listItem` | Icon, title, subtitle, value at the end | `icon`, `value`, `badge`, `trailingIcon`, `tone`, `variant`, `dense` |
| `badge` | A status pill | `variant`, `tone` |
| `chart` | One series as a line or bars | `values`, `variant`, `height`, `min`, `max`, `labels`, `tone` |
| `tableHeader` / `tableRow` | A table, one element per row | `columns`, `values`, `tone`, `dense`, `collapseBelow` |
| `tabs` | Links between screens, the current one marked | nested tabs: `icon`, `selected` |
| `progress` | Linear or circular progress | `progress`, `variant` |
| `card` | A container with a title | `variant` |
| `grid` | Equal-width columns | `columns`, `minColumnWidth`, `spacing`, `padding` |
| `container` | A row, or a column with `"orientation": "vertical"` | `orientation`, `spacing`, `padding`, `paddingHorizontal`, `paddingVertical`, `arrangement`, `alignment`; `weight` on children |
| `separator` | A gap or a rule | `style`, `size` |
| `image` | An image from a URL | `url`, `width`, `height`, `aspectRatio`, `contentScale`, `cornerRadius`, `alpha` |
| `icon` | A Material icon by name | `name`, `size`, `tint` |
| `button` | A button | `variant`, `loading`, `fillMaxWidth` |
| `textInput`, `textArea`, `checkbox`, `switch` | Inputs | `validation`, `active` |
| `topbar` | The page's top bar | `style` |

[Building a dashboard](../KodexRenderer/screens/README.md) has the JSON for each of the dashboard
elements, and the SampleApp's Catalog tab shows them all.

## Customization

Piktographs is designed to be customizable, allowing you to match your app's branding and provide your own Composables for specific element types.

### Custom Element Rendering

The `PageUI` Composable has an `elementOverrides` parameter that allows you to provide your own `@Composable` function for rendering a specific element type. This is useful when you want to render a custom element that is not included in Piktographs by default, or when you want to change the appearance of a standard element.

Here is an example of how to provide a custom Composable for a "countdown" element:

```kotlin
PageUI(
    content = content,
    elementOverrides = { element ->
        when (element.type) {
            "countdown" -> {
                CountdownView(element) // Your custom @Composable
                true // Return true to indicate that the element has been handled
            }
            
            else -> false // Return false for default rendering
        }
    }
)
```

### Styling

Pass a `Theme` to `PageUI`. It groups three value classes - `FontSizes`, `Colors` and `Dimensions` -
and every field has a default, so override only what you need:

```kotlin
val myTheme = Theme(
    colors = Colors(
        mainTextColor = Color(0xFF6200EE),
        secondaryTextColor = Color.DarkGray,
    ),
    fonts = FontSizes(primary = 20.sp),
    dimensions = Dimensions(padding = 12.dp),
)

PageUI(
    content = content,
    theme = myTheme,
)
```

### Tones

`text`, `badge`, `stat`, `listItem`, `chart` and `tableRow` take a `tone`: `info`, `success`,
`warning` or `error` (log-level names such as `WARN` work too). A tone only says what the element
means; `Colors` decides what that looks like:

```kotlin
PageUI(
    content = content,
    theme = Theme(colors = Colors(warningColor = Color(0xFFB26A00), successColor = Color(0xFF2E7D32))),
)
```

Left unset, `infoColor` and `errorColor` follow the Material theme (`primary`, `error`), and success
and warning are a green and an amber chosen for the theme's light or dark surface.

### Unsupported element types

When no built-in renderer and no `elementOverrides` entry handles an element, `UnknownElementUI`
renders a message naming the type. Change that with `LocalUnknownElementPlaceholder`:

```kotlin
CompositionLocalProvider(
    LocalUnknownElementPlaceholder provides UnknownElementPlaceholder.Nothing,
) {
    PageUI(content = content)
}
```

`Verbose` additionally prints the element's parsed contents, which is useful while authoring JSON.
