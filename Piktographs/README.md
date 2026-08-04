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
