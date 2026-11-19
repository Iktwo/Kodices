package com.iktwo.kodex.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import kotlinx.serialization.json.JsonPrimitive

private val previewElements = listOf(
    ProcessedElement(type = "text", id = "title", text = "Thursday, November 19", style = "headlineSmall"),
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
)

private val previewTabs = ProcessedElement(
    type = "tabs",
    id = "tabs",
    nestedElements = listOf(
        ProcessedElement(type = "tab", id = "home", text = "Home", jsonValues = mapOf("icon" to JsonPrimitive("home"), "selected" to JsonPrimitive(true))),
        ProcessedElement(type = "tab", id = "servers", text = "Servers", jsonValues = mapOf("icon" to JsonPrimitive("list"))),
    ),
)

private val connected = DashboardUiState(
    serverUrl = "http://localhost:8090",
    connected = true,
    screens = listOf("dashboard", "home", "status"),
    screen = "dashboard",
    content = Content(previewElements),
    status = "Updated 10:54:02 · every 15 minutes",
)

@Composable
private fun DashboardPreview(
    state: DashboardUiState,
    dark: Boolean = false,
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        DashboardScreen(state, {}, {}, {}, {}, {})
    }
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ConnectFormPreview() {
    DashboardPreview(DashboardUiState(serverUrl = "http://localhost:8090"))
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ConnectFormWrongTokenPreview() {
    DashboardPreview(
        DashboardUiState(
            serverUrl = "http://localhost:8090",
            token = "nope",
            error = "The server needs a token, or the token is wrong.",
            failure = DashboardFailure.Unauthorized,
        ),
    )
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ConnectedPreview() {
    DashboardPreview(connected)
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun ConnectedWithOwnTabsPreview() {
    DashboardPreview(connected.copy(content = Content(listOf(previewTabs) + previewElements), loading = true))
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun StaleScreenDarkPreview() {
    DashboardPreview(
        connected.copy(
            error = "Could not reach http://localhost:8090: Connection refused",
            failure = DashboardFailure.Unreachable,
        ),
        dark = true,
    )
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun BrokenScreenPreview() {
    DashboardPreview(
        connected.copy(
            content = null,
            error = "The screen's JSON is not valid: Unexpected JSON token at offset 211",
            failure = DashboardFailure.InvalidScreen,
        ),
    )
}

@Preview(widthDp = 400, heightDp = 480)
@Composable
private fun LoadingPreview() {
    DashboardPreview(connected.copy(content = null, status = null, loading = true))
}
