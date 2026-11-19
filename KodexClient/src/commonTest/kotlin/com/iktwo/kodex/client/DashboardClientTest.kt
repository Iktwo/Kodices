package com.iktwo.kodex.client

import com.iktwo.kodices.KodicesParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardClientTest {
    private val connection = DashboardConnection("http://dash.local:8090", token = "s3cret")
    private val requests = mutableListOf<HttpRequestData>()
    private val auth = DashboardAuth()

    private val envelope = """
        {
          "content": { "elements": [
            { "type": "stat", "id": "inside", "constants": { "text": "Living room" },
              "processors": { "value": [{ "type": "path", "elements": ["home", "inside"] }] } }
          ] },
          "data": { "home": { "inside": 21.5 } },
          "refresh_seconds": 120
        }
    """.trimIndent()

    private fun http(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
        HttpClient(
            MockEngine { request ->
                requests += request
                handler(request)
            },
        ) {
            dashboardDefaults(auth)
        }

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): DashboardClient = DashboardClient(http(handler), KodicesParser())

    @Test
    fun fetchParsesTheEnvelopeResolvingProcessorsAgainstTheServersData() =
        runTest {
            val client = client { respond(envelope, headers = headersOf(HttpHeaders.ETag, "\"abc\"")) }

            val result = assertIs<DashboardResult.Loaded>(client.fetch(connection, "dashboard"))

            assertEquals("http://dash.local:8090/content/dashboard.json", requests.single().url.toString())
            assertEquals("\"abc\"", result.etag)
            assertEquals(120, result.refreshSeconds)
            val stat = result.content.elements.single()
            assertEquals("Living room", stat.text)
            assertEquals("21.5", stat.jsonValues["value"].toString())
        }

    @Test
    fun fetchRevalidatesWithTheETagAndReportsAnUnchangedScreen() =
        runTest {
            val client = client { respond("", HttpStatusCode.NotModified) }

            assertEquals(DashboardResult.NotModified, client.fetch(connection, "dashboard", etag = "\"abc\""))
            assertEquals("\"abc\"", requests.single().headers[HttpHeaders.IfNoneMatch])
        }

    @Test
    fun aMissingOrTinyRefreshIntervalCannotMakeTheAppPollInATightLoop() =
        runTest {
            val noInterval = client { respond("""{ "content": { "elements": [] } }""") }
            assertEquals(
                DashboardClient.DEFAULT_REFRESH_SECONDS,
                assertIs<DashboardResult.Loaded>(noInterval.fetch(connection, "dashboard")).refreshSeconds,
            )

            val zero = client { respond("""{ "content": { "elements": [] }, "refresh_seconds": 0 }""") }
            assertEquals(
                DashboardClient.MIN_REFRESH_SECONDS,
                assertIs<DashboardResult.Loaded>(zero.fetch(connection, "dashboard")).refreshSeconds,
            )
        }

    @Test
    fun failuresComeBackAsMessagesAPersonCanActOnAndAKindTheUiCanActOn() =
        runTest {
            suspend fun failure(
                status: HttpStatusCode,
                body: String = "",
            ) = assertIs<DashboardResult.Failed>(client { respondError(status, body) }.fetch(connection, "nope"))

            failure(HttpStatusCode.Unauthorized).let {
                assertTrue("token" in it.message)
                assertEquals(DashboardFailure.Unauthorized, it.failure)
            }
            assertEquals(DashboardFailure.Unauthorized, failure(HttpStatusCode.Forbidden).failure)
            failure(HttpStatusCode.NotFound).let {
                assertTrue("'nope'" in it.message)
                assertEquals(DashboardFailure.NotFound, it.failure)
            }
            failure(HttpStatusCode.UnprocessableEntity, "bad element").let {
                assertTrue("not valid" in it.message && "bad element" in it.message)
                assertEquals(DashboardFailure.InvalidScreen, it.failure)
            }
            failure(HttpStatusCode.ServiceUnavailable).let {
                assertTrue("503" in it.message)
                assertEquals(DashboardFailure.UnexpectedAnswer, it.failure)
            }

            val unparseable = assertIs<DashboardResult.Failed>(client { respond("<html>router login</html>") }.fetch(connection, "dashboard"))
            assertTrue("could not parse" in unparseable.message)
            assertEquals(DashboardFailure.UnexpectedAnswer, unparseable.failure)

            val unreachable = assertIs<DashboardResult.Failed>(
                client { throw IllegalStateException("Connection refused") }.fetch(connection, "dashboard"),
            )
            assertTrue("Connection refused" in unreachable.message)
            assertEquals(DashboardFailure.Unreachable, unreachable.failure)

            val badAddress = client { respond(envelope) }.fetch(DashboardConnection("http://bad host:x"), "dashboard")
            assertIs<DashboardResult.Failed>(badAddress)
        }

    @Test
    fun screensReadsTheListAndTheDefault() =
        runTest {
            val client = client { respond("""{ "default": "dashboard", "screens": ["dashboard", "home"] }""") }

            assertEquals(DashboardScreens("dashboard", listOf("dashboard", "home")), client.screens(connection))
            assertEquals("http://dash.local:8090/screens", requests.single().url.toString())
        }

    @Test
    fun screensFailsClearlyWhenTheAddressIsNotADashboardServer() =
        runTest {
            assertFailsWith<DashboardException> { client { respond("<html></html>") }.screens(connection) }
            assertFailsWith<DashboardException> { client { respond("""{ "screens": [] }""") }.screens(connection) }

            val unauthorized = assertFailsWith<DashboardException> { client { respondError(HttpStatusCode.Unauthorized) }.screens(connection) }
            assertEquals(DashboardFailure.Unauthorized, unauthorized.failure)

            val down = assertFailsWith<DashboardException> { client { throw IllegalStateException("refused") }.screens(connection) }
            assertEquals(DashboardFailure.Unreachable, down.failure)
        }

    @Test
    fun theTokenGoesToTheDashboardServerAndNowhereElse() =
        runTest {
            auth.connection = connection
            val http = http { respond("") }

            http.get("http://dash.local:8090/assets/logo.png")
            http.get("https://images.example.com/photo.jpg")
            http.get("http://dash.local:9999/other-service")
            http.get("https://dash.local:8090/other-scheme")

            assertEquals("Bearer s3cret", requests[0].headers[HttpHeaders.Authorization])
            assertNull(requests[1].headers[HttpHeaders.Authorization], "another host")
            assertNull(requests[2].headers[HttpHeaders.Authorization], "another port")
            assertNull(requests[3].headers[HttpHeaders.Authorization], "another scheme")

            auth.connection = null
            http.get("http://dash.local:8090/assets/logo.png")
            assertNull(requests[4].headers[HttpHeaders.Authorization], "disconnected")
        }

    @Test
    fun aRedirectFromTheDashboardServerIsNotFollowed() =
        runTest {
            auth.connection = connection
            val client = client { request ->
                if (request.url.host == "dash.local") {
                    respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://evil.example/content/dashboard.json"))
                } else {
                    respond(envelope)
                }
            }

            val result = assertIs<DashboardResult.Failed>(client.fetch(connection, "dashboard"))

            assertTrue("302" in result.message)
            assertEquals(listOf("dash.local"), requests.map { it.url.host }, "nothing was asked of the other host")
        }

    @Test
    fun twoDashboardsDoNotShareAToken() =
        runTest {
            val other = DashboardAuth()
            auth.connection = connection
            val otherRequests = mutableListOf<HttpRequestData>()
            val otherHttp = HttpClient(
                MockEngine { request ->
                    otherRequests += request
                    respond("")
                },
            ) { dashboardDefaults(other) }

            otherHttp.get("http://dash.local:8090/screens")

            assertNull(otherRequests.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun aConnectionOnlyOwnsUrlsOnItsOwnOrigin() {
        assertTrue(connection.owns(Url("http://dash.local:8090/content/x.json")))
        assertTrue(!connection.owns(Url("http://evil.example/dash.local:8090")))
        assertTrue(!DashboardConnection("not a url at all").owns(Url("http://dash.local:8090/")))
    }

    @Test
    fun aConnectionIsTidiedFromWhatWasTypedAndNeverPrintsItsToken() {
        val typed = DashboardConnection.of("  http://dash.local:8090/ ", "  s3cret ")

        assertEquals(connection, typed)
        assertNull(DashboardConnection.of("http://dash.local:8090", "   ").token)
        assertTrue("s3cret" !in typed.toString())
        assertTrue("s3cret" !in DashboardUiState(token = "s3cret").toString())
    }
}
