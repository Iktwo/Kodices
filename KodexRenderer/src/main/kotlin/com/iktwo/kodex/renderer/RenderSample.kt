package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.RenderMode
import com.iktwo.kodex.renderer.render.RenderSpec
import com.iktwo.kodex.renderer.render.Renderer
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes
import kotlin.system.exitProcess

/**
 * Renders one screen to PNG files without starting the server: `<out>/<screen>.png` (gray16, the
 * device default) plus `-bw` and `-color` variants and a landscape version, which uses
 * `<screen>-landscape.json` if it exists.
 *
 * Usage: `RenderSampleKt <screensDir> <screen> <outputDir>`, or `./gradlew :KodexRenderer:renderSample`.
 */
public fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")

    if (args.size != 3) {
        System.err.println("Usage: RenderSampleKt <screensDir> <screen> <outputDir>")
        exitProcess(2)
    }
    val (screensDir, screen, outputDir) = args
    val repository = ScreenRepository(Path(screensDir))
    val json = repository.read(screen) ?: run {
        System.err.println("No screen '$screen' in ${Path(screensDir).toAbsolutePath()}")
        exitProcess(1)
    }

    val renderer = Renderer(assetsDir = repository.screensDir.resolve(ASSETS_DIR))
    val out = Path(outputDir).createDirectories()

    // A layout is designed for one orientation, so the landscape image uses `<screen>-landscape`
    // when the screens directory has one.
    val landscapeJson = repository.read("$screen-landscape") ?: json

    val variants = listOf(
        Triple("$screen.png", json, RenderSpec()),
        Triple("$screen-bw.png", json, RenderSpec(mode = RenderMode.BlackWhite)),
        Triple("$screen-color.png", json, RenderSpec(mode = RenderMode.Color)),
        Triple(
            "$screen-landscape.png",
            landscapeJson,
            RenderSpec(width = RenderSpec.DEFAULT_HEIGHT, height = RenderSpec.DEFAULT_WIDTH),
        ),
    )

    runBlocking {
        for ((name, screenJson, spec) in variants) {
            val result = renderer.render(screenJson, spec)
            val file = out.resolve(name)
            file.writeBytes(result.png)
            println("${file.toAbsolutePath()}  ${spec.width}x${spec.height} ${spec.mode.id}  ${result.png.size} bytes  ${result.renderMillis} ms  ETag ${result.etag}")
        }
    }
    exitProcess(0)
}
