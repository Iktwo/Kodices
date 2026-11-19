package com.iktwo.piktographs.ui.previews

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.PageUI
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/*
 * Previews live in commonMain (the common @Preview annotation works on every target), and are
 * private so they stay out of the published API.
 */

private fun element(
    type: String,
    id: String,
    text: String? = null,
    textSecondary: String? = null,
    style: String? = null,
    nested: List<ProcessedElement> = emptyList(),
    vararg values: Pair<String, JsonElement>,
) = ProcessedElement(
    type = type,
    id = id,
    text = text,
    textSecondary = textSecondary,
    style = style,
    nestedElements = nested,
    jsonValues = values.toMap(),
)

private fun numbers(vararg values: Number) = JsonArray(values.map { JsonPrimitive(it) })

private fun strings(vararg values: String) = JsonArray(values.map { JsonPrimitive(it) })

private val textElements = listOf(
    element("text", "headline", "Good morning", style = "headlineMedium"),
    element("text", "body", "Body text follows the theme and wraps when it runs out of room on the line."),
    element("text", "centered", "Centered caption", style = "caption", values = arrayOf("align" to JsonPrimitive("center"))),
)

private val statElements = listOf(
    element(
        "grid",
        "stats",
        nested = listOf(
            element(
                "stat",
                "inside",
                "Living room",
                "2° warmer than yesterday",
                values = arrayOf(
                    "value" to JsonPrimitive(21.5),
                    "unit" to JsonPrimitive("°C"),
                    "trend" to JsonPrimitive("up"),
                    "icon" to JsonPrimitive("home"),
                ),
            ),
            element("stat", "outside", "Outside", "Light rain", values = arrayOf("value" to JsonPrimitive("7"), "unit" to JsonPrimitive("°C"))),
            element("stat", "power", "Power", values = arrayOf("value" to JsonPrimitive(412), "unit" to JsonPrimitive("W"))),
        ),
        values = arrayOf("columns" to JsonPrimitive(2)),
    ),
)

private val listElements = listOf(
    element(
        "listItem",
        "standup",
        "Stand-up",
        "Kitchen table",
        values = arrayOf("icon" to JsonPrimitive("calendar"), "value" to JsonPrimitive("09:30")),
    ),
    element("listItem", "mail", "Inbox", values = arrayOf("icon" to JsonPrimitive("email"), "value" to JsonPrimitive(12), "trailingIcon" to JsonPrimitive("forward"))),
    element("badge", "online", "Online"),
    element("badge", "outlined", "3 new", values = arrayOf("variant" to JsonPrimitive("outlined"))),
)

private val chartElements = listOf(
    element(
        "chart",
        "line",
        "Temperature, last 24 h",
        "Low 17.2 · High 22.4",
        values = arrayOf(
            "values" to numbers(18.1, 17.6, 17.2, 17.9, 19.4, 21.0, 22.4, 22.1, 20.8, 19.6),
            "labels" to strings("00:00", "12:00", "now"),
        ),
    ),
    element(
        "chart",
        "bars",
        "Steps this week",
        values = arrayOf(
            "variant" to JsonPrimitive("bar"),
            "values" to numbers(4200, 8100, 6300, 0, 9800, 12100, 3000),
            "labels" to strings("M", "T", "W", "T", "F", "S", "S"),
        ),
    ),
)

@Composable
private fun ElementsPreview(
    elements: List<ProcessedElement>,
    dark: Boolean = false,
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface {
            PageUI(content = Content(elements))
        }
    }
}

@Preview(widthDp = 360, heightDp = 200)
@Composable
private fun TextUIPreview() {
    ElementsPreview(textElements)
}

@Preview(widthDp = 360, heightDp = 300)
@Composable
private fun StatGridPreview() {
    ElementsPreview(statElements)
}

@Preview(widthDp = 360, heightDp = 300)
@Composable
private fun ListItemAndBadgePreview() {
    ElementsPreview(listElements)
}

@Preview(widthDp = 360, heightDp = 340)
@Composable
private fun ChartUIPreview() {
    ElementsPreview(chartElements)
}

@Preview(widthDp = 360, heightDp = 900)
@Composable
private fun DashboardElementsDarkPreview() {
    ElementsPreview(textElements + statElements + listElements + chartElements, dark = true)
}
