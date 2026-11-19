package com.iktwo.kodex.renderer

import com.iktwo.kodices.KodicesParser
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentRoutesTest {
    private val config = RendererConfig(screensDir = SCREENS_DIR)

    @Test
    fun `screens lists every screen and names the default`() =
        renderer {
            val screens = Json.decodeFromString(ScreensResponse.serializer(), client.get("/screens").bodyAsText())

            assertEquals("dashboard", screens.defaultScreen)
            assertEquals(SCREENS_DIR.listDirectoryEntries("*.json").map { it.nameWithoutExtension }.sorted(), screens.screens)
            assertTrue("dashboard" in screens.screens)
        }

    @Test
    fun `content is what a client parser needs, with now merged into the data`() =
        renderer {
            val response = client.get("/content/dashboard.json")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

            val body = response.bodyAsText()
            val envelope = Json.parseToJsonElement(body).jsonObject
            assertEquals(900, envelope.getValue("refresh_seconds").jsonPrimitive.int)
            assertEquals(
                "Wednesday, November 18, 2026",
                envelope.getValue("data").jsonObject.getValue("now").jsonObject.getValue("date").jsonPrimitive.content,
            )

            // The point of the endpoint: a stock parser turns the body into the same screen the
            // renderer draws, processors resolved against the server's data.
            val content = assertNotNull(KodicesParser().parseJSONWithDataToContent(body))
            val today = content.elements.flatMap { it.nestedElements + it }.first { it.id == "today" }
            assertEquals("Wednesday, November 18, 2026", today.text)
            assertEquals("Updated at 09:41", today.textSecondary)
            assertEquals(4, content.elements.count { it.type == "listItem" })
        }

    @Test
    fun `asset urls are rewritten to this server and the asset is served`() =
        renderer {
            val body = client.get("/content/dashboard.json") { header(HttpHeaders.Host, "dash.local:8090") }.bodyAsText()

            assertTrue("asset:" !in body, "asset: URLs only resolve on the server")
            assertTrue("\"http://dash.local:8090/assets/kodices.png\"" in body, body)

            val asset = client.get("/assets/kodices.png")
            assertEquals(HttpStatusCode.OK, asset.status)
            assertEquals(ContentType.Image.PNG, asset.contentType())
            assertContentEquals(SCREENS_DIR.resolve("assets/kodices.png").readBytes(), asset.bodyAsBytes())
        }

    @Test
    fun `a configured public url wins over the request host`() =
        renderer(config.copy(publicUrl = "https://dash.example.com")) {
            val body = client.get("/content/dashboard.json").bodyAsText()

            assertTrue("\"https://dash.example.com/assets/kodices.png\"" in body, body)
        }

    @Test
    fun `content revalidates with its ETag`() =
        renderer {
            val first = client.get("/content/dashboard.json")
            val etag = assertNotNull(first.headers[HttpHeaders.ETag])
            assertEquals("no-cache", first.headers[HttpHeaders.CacheControl])

            val second = client.get("/content/dashboard.json") { header(HttpHeaders.IfNoneMatch, etag) }
            assertEquals(HttpStatusCode.NotModified, second.status)
            assertEquals(0, second.bodyAsBytes().size)
        }

    @Test
    fun `unknown screens and assets outside the assets directory are 404`() =
        renderer {
            assertEquals(HttpStatusCode.NotFound, client.get("/content/nope.json").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/content/..%2Fbuild.gradle.json").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/assets/nope.png").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/assets/..%2Fdashboard.json").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/assets/%2E%2E/%2E%2E/build.gradle.kts").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/assets/").status)
        }

    @Test
    fun `every bundled screen is valid content`() =
        renderer {
            for (screen in SCREENS_DIR.listDirectoryEntries("*.json").map { it.nameWithoutExtension }) {
                assertEquals(HttpStatusCode.OK, client.get("/content/$screen.json").status, screen)
                assertEquals(HttpStatusCode.OK, client.get("/render/$screen.png").status, screen)
            }
        }

    @Test
    fun `with a token, screens, content and assets require it`() =
        renderer(config.copy(token = "s3cret")) {
            for (path in listOf("/screens", "/content/dashboard.json", "/assets/kodices.png")) {
                assertEquals(HttpStatusCode.Unauthorized, client.get(path).status, path)
                assertEquals(HttpStatusCode.OK, client.get(path) { bearerAuth("s3cret") }.status, path)
            }
        }

    private fun renderer(
        config: RendererConfig = this.config,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        environment { this.config = MapApplicationConfig() }
        application { rendererModule(config, testRenderer()) }
        block()
    }
}
