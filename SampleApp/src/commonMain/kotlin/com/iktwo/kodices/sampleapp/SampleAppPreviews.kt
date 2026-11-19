@file:OptIn(ExperimentalTime::class)

package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodex.client.DashboardFailure
import com.iktwo.kodex.client.DashboardScreen
import com.iktwo.kodex.client.DashboardUiState
import com.iktwo.kodices.sampleapp.theme.AppTheme
import com.iktwo.kodices.sampleapp.ui.CountdownUI
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.CountdownStyle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

/*
 * Previews of the sample's screens. They draw the stateless half of each screen (AppScaffold,
 * SampleList, ResourceContent...) with JSON written here, so none of them reads a resource, the
 * data store or the network.
 */

private const val PREVIEW_CONTENT = """
{
  "elements": [
    { "type": "text", "constants": { "text": "Wake a device", "style": "headlineSmall" } },
    { "type": "row", "constants": { "text": "Mac address", "textSecondary": "(AA:BB:CC:DD:EE:FF)" } },
    { "type": "textInput", "id": "macAddress", "constants": { "textSecondary": "AA:BB:CC:DD:EE:FF" } },
    { "type": "separator" },
    { "type": "row", "processors": { "text": { "type": "path", "element": "owner" } }, "constants": { "textSecondary": "Owner" } },
    { "type": "button", "constants": { "text": "Wake", "variant": "filled" } }
  ]
}
"""

private const val PREVIEW_DATA = """{ "owner": "Office desktop" }"""

private const val PREVIEW_CATALOG = """
{
  "elements": [
    { "type": "text", "constants": { "text": "Catalog", "style": "headlineSmall" } },
    { "type": "card", "constants": { "text": "Card", "textSecondary": "An outlined card.", "variant": "outlined" } },
    {
      "type": "grid",
      "constants": { "columns": 2 },
      "nestedElements": [
        { "type": "stat", "constants": { "text": "Inside", "value": 21.5, "unit": "°C", "trend": "up" } },
        { "type": "stat", "constants": { "text": "Humidity", "value": 48, "unit": "%" } }
      ]
    },
    { "type": "listItem", "constants": { "text": "Stand-up", "textSecondary": "Kitchen table", "icon": "calendar", "value": "09:30" } }
  ]
}
"""

/** Fixed, so the countdown previews say the same thing every day. */
private val previewNow = LocalDateTime(2026, 11, 19, 12, 0)

private fun countdown(
    target: LocalDateTime,
    style: CountdownStyle,
) = CountdownElement(id = "countdown", jsonValues = emptyMap(), title = "Release day", target = target, elementStyle = style)

@Composable
private fun SamplePreview(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = dark) {
        Surface(content = content)
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun AppSamplesTabPreview() {
    SamplePreview {
        AppScaffold(selectedTab = Tabs.TabSamples, onTabSelected = {}) {
            SampleList(onSampleSelected = {})
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun AppCatalogTabDarkPreview() {
    SamplePreview(dark = true) {
        AppScaffold(selectedTab = Tabs.ComponentCatalog, onTabSelected = {}) {
            JsonContent(contentString = PREVIEW_CATALOG, dataString = "{}")
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun TabCatalogPreview() {
    SamplePreview { TabCatalog(PREVIEW_CATALOG) }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun SamplePagePreview() {
    SamplePreview {
        SamplePage(onBack = {}) {
            ResourceContent(contentString = PREVIEW_CONTENT, dataString = PREVIEW_DATA, errorMessage = null)
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun SamplePageErrorPreview() {
    SamplePreview {
        SamplePage(onBack = {}) {
            ResourceContent(
                contentString = PREVIEW_CONTENT,
                dataString = PREVIEW_DATA,
                errorMessage = "Enter a MAC address before sending a Wake-on-LAN packet.",
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun TabInputCompactPreview() {
    SamplePreview { TabInput(PREVIEW_CONTENT.trim(), PREVIEW_DATA, {}, {}) }
}

@Preview(widthDp = 900, heightDp = 640)
@Composable
private fun TabInputWidePreview() {
    SamplePreview { TabInput(PREVIEW_CONTENT.trim(), PREVIEW_DATA, {}, {}) }
}

@Preview(widthDp = 400, heightDp = 320)
@Composable
private fun JSONTextEditorPreview() {
    SamplePreview {
        Column { JSONTextEditor(initialValue = PREVIEW_DATA, title = "Data", modifier = Modifier.weight(1f).fillMaxWidth()) {} }
    }
}

@Preview(widthDp = 400, heightDp = 320)
@Composable
private fun JSONTextEditorInvalidPreview() {
    SamplePreview {
        Column { JSONTextEditor(initialValue = """{ "owner": """, title = "Data", modifier = Modifier.weight(1f).fillMaxWidth()) {} }
    }
}

@Preview(widthDp = 400)
@Composable
private fun CountdownPreview() {
    SamplePreview {
        Column {
            val now = previewNow.toInstant(TimeZone.currentSystemDefault())
            CountdownUI(countdown(LocalDateTime(2026, 12, 24, 18, 30), CountdownStyle.DAYS_HOURS_MINUTES_SECONDS), now)
            CountdownUI(countdown(LocalDateTime(2027, 3, 1, 9, 0), CountdownStyle.SHORT), now)
            // In the past: struck through.
            CountdownUI(countdown(LocalDateTime(2026, 11, 1, 9, 0), CountdownStyle.SHORT), now)
        }
    }
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun TabDashboardPreview() {
    SamplePreview {
        DashboardScreen(DashboardUiState(serverUrl = DEFAULT_SERVER_URL), {}, {}, {}, {}, {}, intro = DashboardTabIntro)
    }
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun TabDashboardWrongTokenDarkPreview() {
    SamplePreview(dark = true) {
        DashboardScreen(
            DashboardUiState(
                serverUrl = DEFAULT_SERVER_URL,
                token = "not-the-token",
                error = "The server did not accept the token.",
                failure = DashboardFailure.Unauthorized,
            ),
            {},
            {},
            {},
            {},
            {},
            intro = DashboardTabIntro,
        )
    }
}
