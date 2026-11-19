package com.iktwo.kodex.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.iktwo.kodices.KodicesParser
import com.iktwo.piktographs.navigation.NavigateAction
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class DashboardScreenTest {
    private val parser = KodicesParser(actions = listOf(NavigateAction.descriptor))

    private fun content(json: String) = assertNotNull(parser.parseJSONToContent(json))

    private val plain = content("""{ "elements": [{ "type": "text", "text": "Plain screen" }] }""")

    private val withTabs = content(
        """
        { "elements": [
          { "type": "tabs", "nestedElements": [
            { "type": "tab", "constants": { "text": "Overview", "selected": true } },
            { "type": "tab", "constants": { "text": "Logs" }, "action": { "type": "navigate", "constants": { "targetRoute": "logs" } } }
          ] },
          { "type": "text", "constants": { "text": "Screen with tabs" } }
        ] }
        """.trimIndent(),
    )

    private val connected = DashboardUiState(
        serverUrl = "http://127.0.0.1:8089",
        connected = true,
        screens = listOf("overview", "logs"),
        screen = "overview",
        content = plain,
        status = "Updated 14:02:11 · every 10 seconds",
    )

    @Test
    fun aScreenWithoutTabsGetsTheScreenListAndAScreenWithTabsDoesNot() =
        runComposeUiTest {
            var state by mutableStateOf(connected)
            val selected = mutableListOf<String>()

            setContent { DashboardScreen(state, {}, {}, {}, {}, { selected += it }) }

            onNodeWithText("Plain screen").assertIsDisplayed()
            onNodeWithText("overview").assertIsDisplayed()
            onNodeWithText("logs").performClick()
            assertEquals(listOf("logs"), selected)

            state = connected.copy(content = withTabs)
            waitForIdle()

            onNodeWithText("Screen with tabs").assertIsDisplayed()
            onAllNodesWithText("overview").assertCountEquals(0)
            onNodeWithText("127.0.0.1:8089 · Updated 14:02:11 · every 10 seconds").assertIsDisplayed()

            // The screen's own tab navigates through its action.
            onNodeWithText("Logs").performClick()
            assertEquals(listOf("logs", "logs"), selected)
        }

    @Test
    fun aFailureOverAScreenIsABannerAndWithoutOneIsThePage() =
        runComposeUiTest {
            var state by mutableStateOf(
                connected.copy(error = "Could not reach http://127.0.0.1:8089: Connection refused", failure = DashboardFailure.Unreachable),
            )
            var refreshes = 0
            var disconnects = 0

            setContent { DashboardScreen(state, {}, {}, {}, { disconnects++ }, {}, onRefresh = { refreshes++ }) }

            onNodeWithText("Plain screen").assertIsDisplayed()
            onNodeWithText("Could not reach http://127.0.0.1:8089: Connection refused").assertIsDisplayed()
            onNodeWithText("Retry").performClick()
            assertEquals(1, refreshes)

            state = connected.copy(content = null, error = "The server needs a token, or the token is wrong.", failure = DashboardFailure.Unauthorized)
            waitForIdle()

            onNodeWithText("Not authorized").assertIsDisplayed()
            onNodeWithText("Change token").performClick()
            assertEquals(1, disconnects)

            state = connected.copy(content = null, error = "The screen's JSON is not valid: oops", failure = DashboardFailure.InvalidScreen)
            waitForIdle()

            onNodeWithText("This screen is broken").assertIsDisplayed()
            onNodeWithText("The screen's JSON is not valid: oops").assertIsDisplayed()
        }

    @Test
    fun theFormConnectsShowsTheFirstScreenAndDisconnectingForgetsTheToken() =
        runComposeUiTest {
            val auth = DashboardAuth()
            val authorizations = mutableListOf<String?>()
            val http = HttpClient(
                MockEngine { request ->
                    authorizations += request.headers[HttpHeaders.Authorization]
                    when {
                        request.headers[HttpHeaders.Authorization] != "Bearer s3cret" -> respondError(HttpStatusCode.Unauthorized)
                        request.url.encodedPath == "/screens" -> respond("""{ "default": "overview", "screens": ["overview"] }""")
                        else -> respond("""{ "content": { "elements": [{ "type": "text", "text": "Live screen" }] }, "refresh_seconds": 10 }""")
                    }
                },
            ) { dashboardDefaults(auth) }
            val controller = DashboardController(DashboardClient(http, parser), auth, serverUrl = "http://127.0.0.1:8089")
            val remembered = mutableListOf<DashboardConnection?>()

            setContent {
                Dashboard(controller, onConnected = { remembered += it }, onDisconnected = { remembered += null })
            }

            onNodeWithText("Connect").performClick()
            waitUntil { controller.state.error != null }
            onNodeWithText("The server needs a token, or the token is wrong.").assertIsDisplayed()
            assertNull(authorizations.single(), "no token typed, none sent")

            onNodeWithTag(TAG_TOKEN).performTextInput("s3cret")
            onNodeWithText("Connect").performClick()
            waitUntil { controller.state.content != null }

            onNodeWithText("Live screen").assertIsDisplayed()
            assertEquals(DashboardConnection("http://127.0.0.1:8089", "s3cret"), remembered.single())

            onNodeWithText("Disconnect").performClick()
            waitForIdle()

            onNodeWithText("Connect").assertIsDisplayed()
            assertEquals("", controller.state.token)
            assertNull(auth.connection)
            assertNull(remembered.last())
        }

    @Test
    fun autoConnectSkipsTheForm() =
        runComposeUiTest {
            val auth = DashboardAuth()
            val http = HttpClient(
                MockEngine { request ->
                    if (request.url.encodedPath == "/screens") {
                        respond("""{ "default": "overview", "screens": ["overview"] }""")
                    } else {
                        respond("""{ "content": { "elements": [{ "type": "text", "text": "Live screen" }] }, "refresh_seconds": 10 }""")
                    }
                },
            ) { dashboardDefaults(auth) }
            val controller = DashboardController(DashboardClient(http, parser), auth, serverUrl = "http://127.0.0.1:8089", token = "s3cret")

            setContent { Dashboard(controller, autoConnect = true) }

            waitUntil { controller.state.content != null }
            onNodeWithText("Live screen").assertIsDisplayed()
        }
}
