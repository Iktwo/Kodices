package com.iktwo.piktographs.ui.previews

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.content.Content
import com.iktwo.piktographs.PageUI

/*
 * Previews of the elements a dense, data-driven screen is made of: tones, tables, tabs, adaptive
 * grids and empty states. They are written as the JSON a server would send, with its data, because
 * that is what these elements are for; the parser is the stock one.
 */

private const val TABS = """
  { "type": "tabs", "nestedElements": [
    { "type": "tab", "constants": { "text": "Overview", "icon": "home", "selected": true } },
    { "type": "tab", "constants": { "text": "Devices", "icon": "phone" } },
    { "type": "tab", "constants": { "text": "Events", "icon": "notifications" } },
    { "type": "tab", "constants": { "text": "Logs", "icon": "list" } }
  ] }
"""

private const val TONES = """
  { "type": "container", "nestedElements": [
    { "type": "badge", "constants": { "text": "Neutral" } },
    { "type": "badge", "constants": { "text": "Info", "tone": "info" } },
    { "type": "badge", "constants": { "text": "OK", "tone": "success" } },
    { "type": "badge", "constants": { "text": "Warning", "tone": "warning" } },
    { "type": "badge", "constants": { "text": "Error", "tone": "error" } }
  ] },
  { "type": "container", "nestedElements": [
    { "type": "badge", "constants": { "text": "Tonal", "variant": "tonal" } },
    { "type": "badge", "constants": { "text": "Online", "variant": "tonal", "tone": "success" } },
    { "type": "badge", "constants": { "text": "Unproven", "variant": "tonal", "tone": "warning" } },
    { "type": "badge", "constants": { "text": "Failed", "variant": "outlined", "tone": "error" } }
  ] },
  { "type": "listItem", "constants": {
      "text": "23 failed pairing attempts from 203.0.113.9", "textSecondary": "Banned until 14:17",
      "icon": "warning", "value": "14:02", "tone": "warning", "variant": "tonal" } },
  { "type": "listItem", "constants": {
      "text": "No alerts", "textSecondary": "Nothing needs your attention.",
      "icon": "check_circle", "tone": "success", "variant": "tonal" } },
  { "type": "grid", "constants": { "minColumnWidth": 100 }, "nestedElements": [
    { "type": "stat", "constants": { "text": "Uploads", "value": 321, "size": "medium" } },
    { "type": "stat", "constants": { "text": "Auth failures", "value": 5, "size": "medium", "tone": "warning" } },
    { "type": "stat", "constants": { "text": "Push failures", "value": 2, "size": "medium", "tone": "error" } }
  ] },
  { "type": "listItem", "constants": { "text": "Device registered", "textSecondary": "Alex's phone · via invite",
      "icon": "person", "value": "12:57", "badge": "New", "tone": "info", "dense": true } },
  { "type": "listItem", "constants": { "text": "Uptime", "value": "3d 4h", "dense": true } },
  { "type": "chart", "constants": { "text": "Failures per hour", "variant": "bar", "tone": "error",
      "values": [0, 0, 1, 0, 2, 0, 1, 4, 23, 0, 0, 0], "labels": ["00", "06"] } },
  { "type": "text", "constants": { "text": "14:02:11.120 WARN PairingRoutes", "fontFamily": "monospace", "style": "bodySmall", "tone": "warning" } }
"""

private const val LOG_COLUMNS = """[
  { "key": "time", "label": "Time", "width": 104, "display": "mono" },
  { "key": "level", "label": "Level", "width": 68, "display": "badge" },
  { "key": "logger", "label": "Logger", "width": 120 },
  { "key": "message", "label": "Message", "display": "mono", "maxLines": 3, "primary": true }
]"""

private const val TABLE = """
  { "type": "tableHeader", "constants": { "columns": $LOG_COLUMNS } },
  {
    "type": "tableRow",
    "constants": { "columns": $LOG_COLUMNS, "dense": true },
    "expandWithProcessors": [{ "type": "path", "elements": ["lines"] }],
    "processors": {
      "values": { "type": "path" },
      "tone": [{ "type": "path", "elements": ["severity"] }, { "type": "map", "values": { "info": "neutral" } }]
    },
    "emptyElement": { "type": "listItem", "constants": { "text": "The log is empty", "icon": "info", "dense": true } }
  }
"""

private const val LOG_DATA = """{ "lines": [
  { "time": "14:02:14.501", "level": "WARN", "logger": "RateLimiter", "severity": "warning",
    "message": "Banned 203.0.113.9 until 14:17:14 after 23 failed pairing attempts in 300 s" },
  { "time": "14:02:10.877", "level": "INFO", "logger": "BlobRoutes", "severity": "info",
    "message": "Notification uploaded: id=6f0c1a9e from device 5d17c0aa, 1.4 kB, 3 recipients" },
  { "time": "13:40:02.733", "level": "ERROR", "logger": "PushService", "severity": "error",
    "message": "Push to 0b9e44d1 failed: FCM UNAVAILABLE (HTTP 503)" }
] }"""

@Composable
private fun JsonPreview(
    elements: String,
    data: String = "{}",
    dark: Boolean = false,
) {
    val content = remember(elements, data) {
        KodicesParser().parseJSONToContent("""{ "elements": [$elements] }""", data) ?: Content(emptyList())
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface {
            PageUI(content = content)
        }
    }
}

@Preview(widthDp = 420, heightDp = 620)
@Composable
private fun TonesPreview() {
    JsonPreview("$TABS, $TONES")
}

@Preview(widthDp = 420, heightDp = 620)
@Composable
private fun TonesDarkPreview() {
    JsonPreview("$TABS, $TONES", dark = true)
}

@Preview(widthDp = 900, heightDp = 220)
@Composable
private fun TableWidePreview() {
    JsonPreview(TABLE, LOG_DATA)
}

/** Under `collapseBelow` (600 dp) the header goes and each row labels its own values. */
@Preview(widthDp = 380, heightDp = 320)
@Composable
private fun TableCollapsedPreview() {
    JsonPreview(TABLE, LOG_DATA)
}

@Preview(widthDp = 900, heightDp = 220)
@Composable
private fun TableWideDarkPreview() {
    JsonPreview(TABLE, LOG_DATA, dark = true)
}

@Preview(widthDp = 380, heightDp = 120)
@Composable
private fun TableEmptyPreview() {
    JsonPreview(TABLE, """{ "lines": [] }""")
}
