package com.iktwo.kodex.viewer

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.iktwo.kodex.client.DashboardScreen
import com.iktwo.kodex.client.DashboardUiState
import com.iktwo.kodex.client.dashboardParser
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.system.exitProcess

/**
 * Renders a screen file to a PNG the way the viewer draws it, without a server or a window:
 * `./gradlew :KodexViewer:renderScreen -Pfile=<screen.json>`.
 *
 * The file is what `GET /content/{screen}.json` returns, `{"content": ..., "data": ...}`. The page
 * is as tall as asked for, so a long screen needs a tall picture to be seen whole.
 *
 * Usage: `RenderScreenKt <screen.json> <out.png> [<width>x<height>] [--dark]`, sizes in dp.
 */
fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")

    val dark = "--dark" in args
    val positional = args.filter { it != "--dark" }
    if (positional.size !in 2..3) {
        System.err.println("Usage: RenderScreenKt <screen.json> <out.png> [<width>x<height>] [--dark]")
        exitProcess(2)
    }

    val source = File(positional[0])
    val target = File(positional[1])
    val size = parseSize(positional.getOrNull(2) ?: "${DEFAULT_WIDTH}x$DEFAULT_HEIGHT") ?: run {
        System.err.println("The size is <width>x<height> in dp, e.g. 1280x900")
        exitProcess(2)
    }

    val content = dashboardParser().parseJSONWithDataToContent(source.readText()) ?: run {
        System.err.println("$source is not a {\"content\": ..., \"data\": ...} screen")
        exitProcess(1)
    }

    val state = DashboardUiState(
        serverUrl = "http://127.0.0.1:8089",
        connected = true,
        screens = listOf(source.nameWithoutExtension),
        screen = source.nameWithoutExtension,
        content = content,
        status = "Updated 14:02:11 · every 10 seconds",
    )

    val scene = ImageComposeScene(
        width = size.first * RENDER_SCALE,
        height = size.second * RENDER_SCALE,
        density = Density(RENDER_SCALE.toFloat()),
    ) {
        ViewerTheme(dark = dark) {
            DashboardScreen(state, {}, {}, {}, {}, {}, theme = viewerPageTheme())
        }
    }

    try {
        // Lazy lists and subcomposed layouts settle over a few frames.
        repeat(SETTLE_FRAMES) { scene.render(it * FRAME_NANOS) }
        val image = scene.render(SETTLE_FRAMES * FRAME_NANOS)
        val png = checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) { "Could not encode the picture" }

        target.absoluteFile.parentFile?.mkdirs()
        target.writeBytes(png.bytes)
        println("${target.absolutePath}  ${size.first}x${size.second} dp${if (dark) " dark" else ""}")
    } finally {
        scene.close()
    }

    exitProcess(0)
}

internal fun parseSize(size: String): Pair<Int, Int>? {
    val parts = size.lowercase().split('x').mapNotNull { it.trim().toIntOrNull() }
    return parts.takeIf { it.size == 2 && it.all { side -> side in SIZE_RANGE } }?.let { it[0] to it[1] }
}

private const val DEFAULT_WIDTH = 1280
private const val DEFAULT_HEIGHT = 900
private const val RENDER_SCALE = 1
private const val SETTLE_FRAMES = 5
private const val FRAME_NANOS = 16_000_000L
private val SIZE_RANGE = 100..8000
