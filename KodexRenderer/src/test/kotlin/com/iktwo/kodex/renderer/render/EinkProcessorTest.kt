package com.iktwo.kodex.renderer.render

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EinkProcessorTest {
    @Test
    fun `luminance composites transparent pixels over white`() {
        val rgba = byteArrayOf(
            0,
            0,
            0,
            0xFF.toByte(), // opaque black
            0,
            0,
            0,
            0, // fully transparent
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(), // opaque white
        )

        assertEquals(listOf(0, 255, 255), EinkProcessor.luminance(rgba).toList())
    }

    @Test
    fun `quantize snaps to sixteen evenly spaced levels`() {
        val out = EinkProcessor.quantize(IntArray(256) { it }).map { it.toInt() and 0xFF }

        assertEquals(16, out.toSet().size)
        assertTrue(out.all { it % 17 == 0 })
        assertEquals(0, out.first())
        assertEquals(255, out.last())
        // Nearest level, not floor: 9 is closer to 17 than to 0.
        assertEquals(17, out[9])
    }

    @Test
    fun `floyd steinberg outputs only black and white and preserves mean brightness`() {
        val width = 64
        val height = 64
        val out = EinkProcessor.floydSteinberg(IntArray(width * height) { 128 }, width, height)
        val values = out.map { it.toInt() and 0xFF }

        assertEquals(setOf(0, 255), values.toSet())
        val whiteShare = values.count { it == 255 }.toDouble() / values.size
        assertTrue(whiteShare in 0.45..0.55, "Expected about half white, was $whiteShare")
    }
}
