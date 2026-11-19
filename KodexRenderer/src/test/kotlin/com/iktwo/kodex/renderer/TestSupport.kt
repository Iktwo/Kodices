package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.Renderer
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.imageio.ImageIO
import kotlin.io.path.Path

/** Tests run with the module directory as working directory. */
internal val SCREENS_DIR = Path("screens")

internal val FIXED_CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-18T09:41:00Z"), ZoneOffset.UTC)

internal fun testRenderer(): Renderer =
    Renderer(
        clock = FIXED_CLOCK,
        zoneId = ZoneOffset.UTC,
        assetsDir = SCREENS_DIR.resolve(ASSETS_DIR),
    )

internal fun decodePng(bytes: ByteArray): BufferedImage = requireNotNull(ImageIO.read(ByteArrayInputStream(bytes))) { "Not a PNG" }

/**
 * Pixels in [image] darker than mid grey, within the given rectangle (whole image by default).
 *
 * Reads raw raster samples: `getRGB` on a `TYPE_BYTE_GRAY` image treats the samples as linear grey
 * and gamma-converts them, which would report 0x66 as roughly 0xAA.
 */
internal fun darkPixels(
    image: BufferedImage,
    x0: Int = 0,
    y0: Int = 0,
    x1: Int = image.width,
    y1: Int = image.height,
): Int {
    val raster = image.raster
    val bands = raster.numBands
    var count = 0
    for (y in y0 until y1) {
        for (x in x0 until x1) {
            val luma = (0 until minOf(bands, 3)).sumOf { raster.getSample(x, y, it) } / minOf(bands, 3)
            if (luma < 128) count++
        }
    }
    return count
}
