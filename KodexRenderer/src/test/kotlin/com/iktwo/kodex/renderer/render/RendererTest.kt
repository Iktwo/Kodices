package com.iktwo.kodex.renderer.render

import com.iktwo.kodex.renderer.SCREENS_DIR
import com.iktwo.kodex.renderer.darkPixels
import com.iktwo.kodex.renderer.decodePng
import com.iktwo.kodex.renderer.testRenderer
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.net.InetSocketAddress
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class RendererTest {
    private val renderer = testRenderer()
    private val dashboard = SCREENS_DIR.resolve("dashboard.json").readText()
    private var server: HttpServer? = null

    @AfterTest
    fun stopServer() {
        server?.stop(0)
    }

    @Test
    fun `sample dashboard renders as an 8-bit grayscale PNG at the device size`() =
        runBlocking {
            val result = renderer.render(dashboard)
            val image = decodePng(result.png)

            assertEquals(RenderSpec.DEFAULT_WIDTH, image.width)
            assertEquals(RenderSpec.DEFAULT_HEIGHT, image.height)
            assertEquals(BufferedImage.TYPE_BYTE_GRAY, image.type)
            assertTrue(darkPixels(image) > 10_000, "Expected visible content, the page looks blank")

            val levels = image.raster.getPixels(0, 0, image.width, image.height, null as IntArray?).toSet()
            assertTrue(levels.all { it % 17 == 0 }, "Every pixel must sit on one of the 16 grey levels")
        }

    @Test
    fun `rendering twice gives identical bytes and ETag`() =
        runBlocking {
            val first = renderer.render(dashboard)
            val second = testRenderer().render(dashboard)

            assertContentEquals(first.png, second.png)
            assertEquals(first.etag, second.etag)
        }

    @Test
    fun `bw mode is pure black and white`() =
        runBlocking {
            val image = decodePng(renderer.render(dashboard, RenderSpec(mode = RenderMode.BlackWhite)).png)

            assertEquals(BufferedImage.TYPE_BYTE_GRAY, image.type)
            val levels = image.raster.getPixels(0, 0, image.width, image.height, null as IntArray?).toSet()
            assertEquals(setOf(0, 255), levels)
        }

    @Test
    fun `color mode is RGB and landscape sizes are honoured`() =
        runBlocking {
            val image = decodePng(renderer.render(dashboard, RenderSpec(width = 1696, height = 954, mode = RenderMode.Color)).png)

            assertEquals(1696, image.width)
            assertEquals(954, image.height)
            assertEquals(3, image.colorModel.numComponents)
        }

    @Test
    fun `the capture waits for slow images instead of showing a placeholder`() =
        runBlocking {
            val png = SCREENS_DIR.resolve("assets/kodices.png").readBytes()
            val url = serve(png, delayMillis = 1_500)

            val result = renderer.render(imageScreen(url))
            saveForInspection("slow-image.png", result.png)
            val image = decodePng(result.png)

            // The image fills a 400 dp (800 px) tall box at the top; the logo is mostly dark grey.
            assertTrue(darkPixels(image, y1 = 800) > 50_000, "The slow image is missing from the capture")
        }

    @Test
    fun `a failed image load renders without it instead of timing out`() =
        runBlocking {
            val image = decodePng(renderer.render(imageScreen("http://127.0.0.1:1/missing.png")).png)

            assertEquals(0, darkPixels(image, y1 = 800))
        }

    @Test
    fun `an image slower than the settle timeout fails the render`() {
        val url = serve(ByteArray(0), delayMillis = 2_000)
        val impatient = Renderer(settleTimeout = 300.milliseconds)

        assertFailsWith<RenderTimeoutException> {
            runBlocking { impatient.render(imageScreen(url)) }
        }
    }

    @Test
    fun `invalid JSON is reported as a parse error`() {
        assertFailsWith<ScreenParseException> {
            runBlocking { renderer.render("{ not json") }
        }
    }

    /** Kept under build/ so a failing capture can be looked at. */
    private fun saveForInspection(
        name: String,
        png: ByteArray,
    ) {
        Path("build/test-renders").createDirectories().resolve(name).writeBytes(png)
    }

    private fun imageScreen(url: String) =
        """
        {
          "elements": [
            {
              "type": "image",
              "constants": { "url": "$url", "width": "fill", "height": 400, "contentScale": "crop" }
            }
          ]
        }
        """.trimIndent()

    private fun serve(
        body: ByteArray,
        delayMillis: Long,
    ): String {
        val http = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        http.createContext("/image.png") { exchange ->
            Thread.sleep(delayMillis)
            exchange.responseHeaders.add("Content-Type", "image/png")
            exchange.sendResponseHeaders(200, body.size.toLong().takeIf { it > 0 } ?: -1)
            exchange.responseBody.use { it.write(body) }
        }
        http.executor = null
        http.start()
        server = http
        return "http://127.0.0.1:${http.address.port}/image.png"
    }
}
