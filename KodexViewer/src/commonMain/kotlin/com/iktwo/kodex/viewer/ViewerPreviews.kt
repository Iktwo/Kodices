package com.iktwo.kodex.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodex.client.DashboardFailure
import com.iktwo.kodex.client.DashboardScreen
import com.iktwo.kodex.client.DashboardUiState
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import kotlinx.serialization.json.JsonPrimitive

/*
 * What ViewerApp shows, without its HTTP client: the same DashboardScreen, in the viewer's theme
 * and with the viewer's page theme and intro.
 */

private val previewContent = Content(
    listOf(
        ProcessedElement(type = "text", id = "title", text = "Thursday, November 19", style = "headlineSmall"),
        ProcessedElement(type = "separator", id = "separator"),
        ProcessedElement(
            type = "stat",
            id = "inside",
            text = "Living room",
            textSecondary = "2° warmer than yesterday",
            jsonValues = mapOf("value" to JsonPrimitive(21.5), "unit" to JsonPrimitive("°C"), "trend" to JsonPrimitive("up")),
        ),
        ProcessedElement(
            type = "listItem",
            id = "standup",
            text = "Stand-up",
            textSecondary = "Kitchen table",
            jsonValues = mapOf("icon" to JsonPrimitive("calendar"), "value" to JsonPrimitive("09:30")),
        ),
    ),
)

private val connected = DashboardUiState(
    serverUrl = "http://localhost:8090",
    connected = true,
    screens = listOf("dashboard", "home", "status"),
    screen = "dashboard",
    content = previewContent,
    status = "Updated 10:54:02 · every 15 minutes",
)

@Composable
private fun ViewerPreview(
    state: DashboardUiState,
    dark: Boolean = false,
) {
    ViewerTheme(dark = dark) {
        DashboardScreen(state, {}, {}, {}, {}, {}, intro = ViewerIntro, theme = viewerPageTheme())
    }
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ViewerConnectPreview() {
    ViewerPreview(DashboardUiState(serverUrl = "http://localhost:8090"))
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ViewerConnectDarkPreview() {
    ViewerPreview(
        DashboardUiState(
            serverUrl = "http://localhost:8090",
            token = "not-the-token",
            error = "The server did not accept the token.",
            failure = DashboardFailure.Unauthorized,
        ),
        dark = true,
    )
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ViewerScreenPreview() {
    ViewerPreview(connected)
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ViewerScreenDarkPreview() {
    ViewerPreview(connected, dark = true)
}
