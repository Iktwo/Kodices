package com.iktwo.kodex.client

import com.iktwo.kodices.KodicesParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardControllerTest {
    private val requests = mutableListOf<HttpRequestData>()
    private val auth = DashboardAuth()

    private val screens = """{ "default": "overview", "screens": ["overview", "logs"] }"""

    private fun screen(text: String) = """{ "content": { "elements": [{ "type": "text", "text": "$text" }] }, "data": {}, "refresh_seconds": 10 }"""

    /**
     * The engine answers on the test's scheduler: on its default dispatcher a request would finish
     * on another thread, some time after `runCurrent()` had returned.
     */
    private fun TestScope.controller(
        token: String = "s3cret",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): DashboardController {
        val engine = MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                requests += request
                handler(request)
            }
        }
        val http = HttpClient(engine) {
            dashboardDefaults(auth)
        }

        return DashboardController(
            client = DashboardClient(http, KodicesParser()),
            auth = auth,
            serverUrl = " http://127.0.0.1:8089/ ",
            token = token,
            clock = { "14:02:11" },
        )
    }

    private fun paths() = requests.map { it.url.encodedPath }

    @Test
    fun connectingListsTheScreensSelectsTheDefaultAndSendsTheToken() =
        runTest {
            val controller = controller { respond(screens) }

            assertTrue(controller.connect())

            assertEquals("http://127.0.0.1:8089/screens", requests.single().url.toString())
            assertEquals("Bearer s3cret", requests.single().headers[HttpHeaders.Authorization])
            assertEquals(DashboardConnection("http://127.0.0.1:8089", "s3cret"), controller.connection)
            with(controller.state) {
                assertTrue(connected)
                assertEquals(listOf("overview", "logs"), this.screens)
                assertEquals("overview", screen)
                assertNull(error)
            }
        }

    @Test
    fun aWrongTokenKeepsTheFormAndSaysSo() =
        runTest {
            val controller = controller(token = "wrong") { respondError(HttpStatusCode.Unauthorized) }

            assertFalse(controller.connect())

            assertNull(controller.connection)
            assertNull(auth.connection, "a token the server refused is not kept for later requests")
            with(controller.state) {
                assertFalse(connected)
                assertFalse(loading)
                assertEquals(DashboardFailure.Unauthorized, failure)
                assertTrue("token" in error.orEmpty())
                assertEquals("wrong", token, "still in the field, to be corrected")
            }
        }

    @Test
    fun anUnreachableServerKeepsTheFormAndSaysSo() =
        runTest {
            val controller = controller { throw IllegalStateException("Connection refused") }

            assertFalse(controller.connect())

            assertEquals(DashboardFailure.Unreachable, controller.state.failure)
            assertTrue("Connection refused" in controller.state.error.orEmpty())
        }

    @Test
    fun pollingAsksAgainWhenTheServerSaysToAndRevalidatesWithTheETag() =
        runTest {
            var version = 1
            val controller = controller { request ->
                when {
                    request.url.encodedPath == "/screens" -> respond(screens)
                    request.headers[HttpHeaders.IfNoneMatch] == "\"v$version\"" -> respond("", HttpStatusCode.NotModified)
                    else -> respond(screen("Version $version"), headers = headersOf(HttpHeaders.ETag, "\"v$version\""))
                }
            }
            controller.connect()
            val connection = assertNotNull(controller.connection)

            val polling = backgroundScope.launch { controller.poll(connection, "overview") }
            runCurrent()

            assertEquals(listOf("/screens", "/content/overview.json"), paths())
            assertEquals("Version 1", controller.state.content?.elements?.single()?.text)
            assertEquals("Updated 14:02:11 · every 10 seconds", controller.state.status)
            assertFalse(controller.state.loading)

            // Nothing before the interval the server asked for.
            advanceTimeBy(9_000)
            runCurrent()
            assertEquals(2, requests.size)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(3, requests.size)
            assertEquals("\"v1\"", requests.last().headers[HttpHeaders.IfNoneMatch])
            assertEquals("Unchanged at 14:02:11 · every 10 seconds", controller.state.status)
            assertEquals("Version 1", controller.state.content?.elements?.single()?.text)

            version = 2
            advanceTimeBy(10_000)
            runCurrent()
            assertEquals("Version 2", controller.state.content?.elements?.single()?.text)

            polling.cancel()
        }

    @Test
    fun aFailedPollKeepsTheScreenShowsTheErrorAndRecovers() =
        runTest {
            var down = false
            val controller = controller { request ->
                when {
                    request.url.encodedPath == "/screens" -> respond(screens)
                    down -> respondError(HttpStatusCode.UnprocessableEntity, "Unexpected JSON token")
                    else -> respond(screen("Fine"))
                }
            }
            controller.connect()
            val polling = backgroundScope.launch { controller.poll(assertNotNull(controller.connection), "overview") }
            runCurrent()

            down = true
            advanceTimeBy(10_000)
            runCurrent()

            with(controller.state) {
                assertEquals(DashboardFailure.InvalidScreen, failure)
                assertTrue("Unexpected JSON token" in error.orEmpty())
                assertEquals("Fine", content?.elements?.single()?.text, "a stale screen beats an empty one")
            }

            down = false
            advanceTimeBy(10_000)
            runCurrent()

            assertNull(controller.state.error)
            assertNull(controller.state.failure)

            polling.cancel()
        }

    @Test
    fun selectingAScreenClearsTheOldOneAndRefreshAsksForARestart() =
        runTest {
            val controller = controller { request ->
                if (request.url.encodedPath == "/screens") respond(screens) else respond(screen("Overview"))
            }
            controller.connect()
            val polling = backgroundScope.launch { controller.poll(assertNotNull(controller.connection), "overview") }
            runCurrent()
            polling.cancel()

            controller.selectScreen("overview")
            assertNotNull(controller.state.content, "selecting the current screen changes nothing")

            controller.selectScreen("logs")
            assertEquals("logs", controller.state.screen)
            assertNull(controller.state.content)

            val before = controller.refreshRequests
            controller.refresh()
            assertEquals(before + 1, controller.refreshRequests)
        }

    @Test
    fun disconnectingForgetsTheTokenEverywhere() =
        runTest {
            val controller = controller { respond(screens) }
            controller.connect()

            controller.disconnect()

            assertNull(controller.connection)
            assertNull(auth.connection)
            assertEquals(DashboardUiState(serverUrl = " http://127.0.0.1:8089/ "), controller.state)
        }

    @Test
    fun intervalsAreSaidInWords() {
        assertEquals("10 seconds", describeInterval(10))
        assertEquals("second", describeInterval(1))
        assertEquals("minute", describeInterval(60))
        assertEquals("15 minutes", describeInterval(900))
        assertEquals("90 seconds", describeInterval(90))
        assertEquals("127.0.0.1:8089", displayHost("http://127.0.0.1:8089/"))
        assertEquals("dash.local", displayHost("dash.local"))
    }
}
