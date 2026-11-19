package com.iktwo.kodex.renderer

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
import java.awt.image.BufferedImage
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoutesTest {
    private val config = RendererConfig(screensDir = SCREENS_DIR)

    @Test
    fun `health is ok`() =
        renderer {
            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("ok", response.bodyAsText())
        }

    @Test
    fun `display points the device at the default screen`() =
        renderer {
            val response = client.get("/display?device=paper-pro-move")
            assertEquals(HttpStatusCode.OK, response.status)

            val display = Json.decodeFromString(DisplayResponse.serializer(), response.bodyAsText())
            assertEquals("/render/dashboard.png?width=954&height=1696&mode=gray16", display.imageUrl)
            assertEquals(900, display.refreshSeconds)

            // The advertised ETag is the one the image is served with.
            val image = client.get(display.imageUrl)
            assertEquals(display.etag, image.headers[HttpHeaders.ETag])
        }

    @Test
    fun `display uses the snake case contract field names`() =
        renderer {
            val body = client.get("/display?device=x").bodyAsText()

            assertTrue("\"image_url\"" in body && "\"refresh_seconds\"" in body && "\"etag\"" in body, body)
        }

    @Test
    fun `render serves a PNG with a strong ETag, then 304 on revalidation`() =
        renderer {
            val first = client.get("/render/dashboard.png?width=954&height=1696&mode=gray16")
            assertEquals(HttpStatusCode.OK, first.status)
            assertEquals(ContentType.Image.PNG, first.contentType())
            assertEquals("no-cache", first.headers[HttpHeaders.CacheControl])
            assertEquals("miss", first.headers["X-Render-Cache"])

            val etag = assertNotNull(first.headers[HttpHeaders.ETag])
            assertTrue(etag.startsWith("\"") && !etag.startsWith("W/"), "Expected a strong ETag, was $etag")
            assertEquals(BufferedImage.TYPE_BYTE_GRAY, decodePng(first.bodyAsBytes()).type)

            val second = client.get("/render/dashboard.png?width=954&height=1696&mode=gray16") {
                header(HttpHeaders.IfNoneMatch, etag)
            }
            assertEquals(HttpStatusCode.NotModified, second.status)
            assertEquals(etag, second.headers[HttpHeaders.ETag])
            assertEquals("hit", second.headers["X-Render-Cache"])
            assertEquals(0, second.bodyAsBytes().size)
        }

    @Test
    fun `a different mode is a different representation`() =
        renderer {
            val gray = client.get("/render/dashboard.png").headers[HttpHeaders.ETag]
            val bw = client.get("/render/dashboard.png?mode=bw") { header(HttpHeaders.IfNoneMatch, gray) }

            assertEquals(HttpStatusCode.OK, bw.status)
        }

    @Test
    fun `landscape is accepted and oversize requests are rejected`() =
        renderer {
            val landscape = client.get("/render/dashboard.png?width=1696&height=954")
            assertEquals(HttpStatusCode.OK, landscape.status)
            assertEquals(1696, decodePng(landscape.bodyAsBytes()).width)

            assertEquals(HttpStatusCode.BadRequest, client.get("/render/dashboard.png?width=5000").status)
            assertEquals(HttpStatusCode.BadRequest, client.get("/render/dashboard.png?mode=sepia").status)
            assertEquals(HttpStatusCode.BadRequest, client.get("/render/dashboard.png?height=abc").status)
        }

    @Test
    fun `render json returns the screen source`() =
        renderer {
            val response = client.get("/render/dashboard.json")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(SCREENS_DIR.resolve("dashboard.json").readText(), response.bodyAsText())
        }

    @Test
    fun `unknown and malicious screen names are 404`() =
        renderer {
            assertEquals(HttpStatusCode.NotFound, client.get("/render/nope.png").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/render/nope.json").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/render/..%2Fbuild.gradle.json").status)
        }

    @Test
    fun `with a token, display and render require it but health does not`() =
        renderer(config.copy(token = "s3cret")) {
            assertEquals(HttpStatusCode.OK, client.get("/health").status)
            assertEquals(HttpStatusCode.Unauthorized, client.get("/display?device=x").status)
            assertEquals(HttpStatusCode.Unauthorized, client.get("/render/dashboard.png").status)
            assertEquals(HttpStatusCode.Unauthorized, client.get("/render/dashboard.png") { bearerAuth("wrong") }.status)

            assertEquals(HttpStatusCode.OK, client.get("/display?device=x") { bearerAuth("s3cret") }.status)
            assertEquals(HttpStatusCode.OK, client.get("/render/dashboard.png") { bearerAuth("s3cret") }.status)
        }

    @Test
    fun `the module named in application conf boots`() =
        testApplication {
            // Loads application.conf and its module list exactly as EngineMain does, which catches
            // module-resolution problems the other tests bypass by calling rendererModule directly.
            configure("application.conf")
            assertEquals(HttpStatusCode.OK, client.get("/health").status)
        }

    @Test
    fun `If-None-Match parsing follows weak comparison`() {
        assertTrue(matchesIfNoneMatch("\"a\"", "\"a\""))
        assertTrue(matchesIfNoneMatch("W/\"a\"", "\"a\""))
        assertTrue(matchesIfNoneMatch("\"x\", \"a\"", "\"a\""))
        assertTrue(matchesIfNoneMatch("*", "\"a\""))
        assertTrue(!matchesIfNoneMatch("\"b\"", "\"a\""))
        assertTrue(!matchesIfNoneMatch(null, "\"a\""))
    }

    private fun renderer(
        config: RendererConfig = this.config,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        // An empty config keeps testApplication from loading application.conf and starting the
        // production module next to this one.
        environment { this.config = MapApplicationConfig() }
        application { rendererModule(config, testRenderer()) }
        block()
    }
}
