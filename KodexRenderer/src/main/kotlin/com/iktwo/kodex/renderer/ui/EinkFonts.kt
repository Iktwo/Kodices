package com.iktwo.kodex.renderer.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/**
 * Fonts bundled with the renderer.
 *
 * Text must never fall back to system fonts: the set installed differs between a developer machine
 * and the container, so the same screen would lay out differently and the ETag would stop meaning
 * "same picture". Noto Sans is licensed under the SIL Open Font License 1.1, see
 * `resources/fonts/NotoSans-OFL.txt`.
 */
internal object EinkFonts {
    val notoSans: FontFamily by lazy {
        FontFamily(
            font("NotoSans-Regular", FontWeight.Normal),
            font("NotoSans-Medium", FontWeight.Medium),
            font("NotoSans-Bold", FontWeight.Bold),
        )
    }

    private fun font(
        name: String,
        weight: FontWeight,
    ) = Font(
        identity = name,
        data = readResource("/fonts/$name.ttf"),
        weight = weight,
    )

    private fun readResource(path: String): ByteArray {
        val stream = EinkFonts::class.java.getResourceAsStream(path)
            ?: error("Bundled font $path is missing from the classpath")
        return stream.use { it.readBytes() }
    }
}
