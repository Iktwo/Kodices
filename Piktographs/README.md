# Piktographs Module

Piktographs is a reference implementation that renders UI models from the `Kodices` module using [Jetpack Compose](https://www.jetbrains.com/lp/compose-multiplatform/). It provides a set of pre-built Composables for common UI elements, as well as a mechanism for customizing the look and feel of your server-driven UI.

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

You can customize the appearance of the Composables rendered by Piktographs by providing a `Theme` object to the `PageUI` Composable. The `Theme` object allows you to specify your own colors, typography, and other style attributes.

```kotlin
val myTheme = Theme(
    primaryColor = Color(0xFF6200EE),
    textColor = Color.Black,
    // ... other theme properties
)

PageUI(
    content = content,
    theme = myTheme
)
```
