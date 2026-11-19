package com.iktwo.kodex.renderer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iktwo.kodex.renderer.render.Page
import com.iktwo.kodex.renderer.render.ScreenData
import com.iktwo.kodex.renderer.render.ScreenParser
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.DefaultTheme
import kotlinx.serialization.json.JsonPrimitive
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

private val PREVIEW_SCREEN = """
    {
      "content": {
        "elements": [
          { "type": "topbar", "constants": { "text": "Kodex Dashboard" } },
          {
            "type": "row",
            "id": "today",
            "processors": {
              "text": [{ "type": "path", "elements": ["now", "date"] }],
              "textSecondary": [
                { "type": "path", "elements": ["now", "time"] },
                { "type": "string", "element": "Updated at %" }
              ]
            }
          },
          { "type": "separator", "constants": { "style": "line", "size": 2 } },
          {
            "type": "row",
            "id": "agenda",
            "expandWithProcessors": [{ "type": "path", "elements": ["agenda"] }],
            "processors": {
              "text": [{ "type": "path", "elements": ["title"] }],
              "textSecondary": [{ "type": "path", "elements": ["when"] }]
            }
          },
          {
            "type": "card",
            "constants": { "text": "Home", "textSecondary": "Living room 21 °C", "variant": "outlined" }
          },
          {
            "type": "progress",
            "constants": { "text": "Week", "textSecondary": "3 of 7 days", "variant": "linear", "progress": 0.43 }
          }
        ]
      },
      "data": {
        "agenda": [
          { "title": "Stand-up", "when": "09:30 · Kitchen table" },
          { "title": "Dentist", "when": "12:15 · Dr. Rivera" }
        ]
      }
    }
""".trimIndent()

/** A dashboard as the device receives it (before grey quantization), in portrait. */
@Preview(widthDp = 477, heightDp = 848)
@Composable
public fun EinkDashboardPreview() {
    val content = remember {
        val clock = Clock.fixed(Instant.parse("2026-11-18T09:41:00Z"), ZoneOffset.UTC)
        ScreenParser(ScreenData(clock, ZoneOffset.UTC, Locale.US)).parse(PREVIEW_SCREEN)
    }
    Page(content)
}

/** The same dashboard in landscape. */
@Preview(widthDp = 848, heightDp = 477)
@Composable
public fun EinkDashboardLandscapePreview() {
    EinkDashboardPreview()
}

/** The e-ink type scale on the bundled font. */
@Preview(widthDp = 400)
@Composable
public fun EinkTypographyPreview() {
    EinkTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Headline", style = MaterialTheme.typography.headlineMedium)
            Text("Title large", style = MaterialTheme.typography.titleLarge)
            Text("Title medium", style = MaterialTheme.typography.titleMedium)
            Text("Body large: pure black on white.", style = MaterialTheme.typography.bodyLarge)
            Text("Body medium: no greys, no hairlines.", style = MaterialTheme.typography.bodyMedium)
            Text("Label small", style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Cards (every variant renders outlined) and progress indicators as drawn for e-ink. */
@Preview(widthDp = 477)
@Composable
public fun EinkElementsPreview() {
    fun element(
        type: String,
        title: String,
        subtitle: String,
        vararg values: Pair<String, JsonPrimitive>,
    ) = ProcessedElement(type = type, id = title, text = title, textSecondary = subtitle, jsonValues = values.toMap())

    EinkTheme {
        CompositionLocalProvider(DefaultTheme provides EinkPageTheme) {
            Column {
                EinkCard(element("card", "Elevated card", "No shadow on e-ink", "variant" to JsonPrimitive("elevated")))
                EinkCard(element("card", "Filled card", "No tonal fill either", "variant" to JsonPrimitive("filled")))
                EinkProgress(element("progress", "Linear, 43%", "Outlined track", "progress" to JsonPrimitive(0.43)))
                EinkProgress(element("progress", "Indeterminate", "Empty track: no animation in a still"))
                EinkProgress(
                    element(
                        "progress",
                        "Circular, 70%",
                        "Ring with a solid arc",
                        "variant" to JsonPrimitive("circular"),
                        "progress" to JsonPrimitive(0.7),
                    ),
                )
            }
        }
    }
}
