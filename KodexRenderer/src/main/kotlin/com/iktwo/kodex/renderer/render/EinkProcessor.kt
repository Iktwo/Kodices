package com.iktwo.kodex.renderer.render

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * Turns RGBA pixels into the PNG the panel displays.
 *
 * Everything here is integer or fixed-order float arithmetic, so the same pixels always produce the
 * same bytes. The JDK PNG writer adds no timestamps or other varying chunks.
 */
internal object EinkProcessor {
    private const val GRAY_LEVELS = 16
    private const val LEVEL_STEP = 255 / (GRAY_LEVELS - 1) // 17

    /**
     * @param rgba unpremultiplied RGBA, 4 bytes per pixel, row-major.
     */
    fun encode(
        rgba: ByteArray,
        width: Int,
        height: Int,
        mode: RenderMode,
    ): ByteArray {
        require(rgba.size == width * height * 4) { "Expected ${width * height * 4} bytes, got ${rgba.size}" }

        val image = when (mode) {
            RenderMode.Gray16 -> grayImage(quantize(luminance(rgba)), width, height)
            RenderMode.BlackWhite -> grayImage(floydSteinberg(luminance(rgba), width, height), width, height)
            RenderMode.Color -> rgbImage(rgba, width, height)
        }

        return ByteArrayOutputStream().use { out ->
            check(ImageIO.write(image, "png", out)) { "No PNG writer available" }
            out.toByteArray()
        }
    }

    /** Rec. 601 luma of each pixel, composited over white. */
    internal fun luminance(rgba: ByteArray): IntArray {
        val pixels = rgba.size / 4
        val gray = IntArray(pixels)
        for (i in 0 until pixels) {
            val o = i * 4
            val a = rgba[o + 3].toInt() and 0xFF
            val r = over(rgba[o].toInt() and 0xFF, a)
            val g = over(rgba[o + 1].toInt() and 0xFF, a)
            val b = over(rgba[o + 2].toInt() and 0xFF, a)
            // Fixed-point 299/587/114, rounded.
            gray[i] = (r * 299 + g * 587 + b * 114 + 500) / 1000
        }
        return gray
    }

    private fun over(
        channel: Int,
        alpha: Int,
    ): Int = (channel * alpha + 255 * (255 - alpha) + 127) / 255

    /** Snaps each value to the nearest of 16 evenly spaced levels (0, 17, ..., 255). */
    internal fun quantize(gray: IntArray): ByteArray {
        val out = ByteArray(gray.size)
        for (i in gray.indices) {
            val level = (gray[i] * (GRAY_LEVELS - 1) + 127) / 255
            out[i] = (level * LEVEL_STEP).toByte()
        }
        return out
    }

    /** Classic Floyd–Steinberg error diffusion to 0/255, scanning left to right. */
    internal fun floydSteinberg(
        gray: IntArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val buffer = FloatArray(gray.size) { gray[it].toFloat() }
        val out = ByteArray(gray.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                val old = buffer[i]
                val new = if (old < 128f) 0f else 255f
                out[i] = new.roundToInt().toByte()
                val error = old - new
                if (x + 1 < width) buffer[i + 1] += error * 7f / 16f
                if (y + 1 < height) {
                    if (x > 0) buffer[i + width - 1] += error * 3f / 16f
                    buffer[i + width] += error * 5f / 16f
                    if (x + 1 < width) buffer[i + width + 1] += error * 1f / 16f
                }
            }
        }
        return out
    }

    private fun grayImage(
        gray: ByteArray,
        width: Int,
        height: Int,
    ): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        val target = (image.raster.dataBuffer as DataBufferByte).data
        gray.copyInto(target)
        return image
    }

    private fun rgbImage(
        rgba: ByteArray,
        width: Int,
        height: Int,
    ): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val target = (image.raster.dataBuffer as DataBufferInt).data
        for (i in target.indices) {
            val o = i * 4
            val a = rgba[o + 3].toInt() and 0xFF
            val r = over(rgba[o].toInt() and 0xFF, a)
            val g = over(rgba[o + 1].toInt() and 0xFF, a)
            val b = over(rgba[o + 2].toInt() and 0xFF, a)
            target[i] = (r shl 16) or (g shl 8) or b
        }
        return image
    }
}
