package com.iktwo.kodex.renderer.render

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.iktwo.kodex.renderer.ui.EinkPageTheme
import com.iktwo.kodex.renderer.ui.EinkTheme
import com.iktwo.kodex.renderer.ui.einkElementOverride
import com.iktwo.kodices.content.Content
import com.iktwo.piktographs.PageUI
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Clock
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Renders Kodices screens headlessly, through Piktographs, to e-ink ready PNGs.
 *
 * Renders are serialized on one thread shared by every instance: Coil's image loader is a process
 * singleton (Piktographs' `ImageUI` uses `AsyncImage` without an explicit loader), so two renders
 * running at once would see each other's image requests. One render at a time also bounds memory.
 *
 * @param clock source of `now` for screens. Inject a fixed clock for reproducible output.
 * @param assetsDir where `asset:` image URLs resolve; null disables them.
 * @param settleTimeout how long to wait for images before giving up with [RenderTimeoutException].
 */
public class Renderer(
    clock: Clock = Clock.systemDefaultZone(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.US,
    assetsDir: Path? = null,
    private val settleTimeout: Duration = 10.seconds,
    networkTimeout: Duration = 5.seconds,
) {
    private val screenData = ScreenData(clock, zoneId, locale)
    private val tracker = ImageLoadTracker()
    private val imageLoader: ImageLoader = buildImageLoader(assetsDir, tracker, networkTimeout)

    private val parser = ScreenParser(screenData)

    /** Key for the minute the screen data currently describes; changes when `now` does. */
    public fun dataKey(): String = screenData.minuteKey()

    /**
     * Renders [screenJson] at [spec].
     *
     * See [ScreenParser] for the accepted JSON shapes.
     *
     * @throws ScreenParseException if the JSON is not valid Kodices content.
     * @throws RenderTimeoutException if images are still loading after [settleTimeout].
     */
    public suspend fun render(
        screenJson: String,
        spec: RenderSpec = RenderSpec(),
    ): RenderResult {
        val started = TimeSource.Monotonic.markNow()
        val content = parse(screenJson)

        val rgba = withContext(renderDispatcher) { renderPixels(content, spec) }
        val png = EinkProcessor.encode(rgba, spec.width, spec.height, spec.mode)

        return RenderResult(
            png = png,
            etag = strongEtag(png),
            renderMillis = started.elapsedNow().inWholeMilliseconds,
        )
    }

    internal fun parse(screenJson: String): Content = parser.parse(screenJson)

    /**
     * The screen as a client that renders it itself needs it: its content, and its data with `now`
     * merged in, exactly as [render] would see them. The content is checked to be valid Kodices
     * content first, so a broken screen fails here and not on every client.
     *
     * @throws ScreenParseException if the JSON is not valid Kodices content.
     */
    public fun resolve(screenJson: String): ResolvedScreen {
        parser.parse(screenJson)
        return parser.resolve(screenJson)
    }

    // setUnsafe on purpose: the renderer owns the process, and every render must load images
    // through this instance's loader, whatever was set before.
    @OptIn(DelicateCoilApi::class)
    private suspend fun renderPixels(
        content: Content,
        spec: RenderSpec,
    ): ByteArray {
        SingletonImageLoader.setUnsafe(imageLoader)
        tracker.reset()

        val scene = ImageComposeScene(
            width = spec.width,
            height = spec.height,
            density = Density(spec.density),
        ) {
            Page(content)
        }

        try {
            settle(scene)

            // Animations started while settling all began at frame time 0. Jumping the clock a fixed
            // distance finishes every finite animation and leaves infinite ones (indeterminate
            // progress) at the same phase every time. The second frame picks up anything the first
            // one invalidated.
            scene.render(FAST_FORWARD_NANOS)
            val image = scene.render(FAST_FORWARD_NANOS)

            val bitmap = Bitmap()
            try {
                bitmap.allocPixels(
                    ImageInfo(spec.width, spec.height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL),
                )
                check(image.readPixels(bitmap, 0, 0)) { "Could not read rendered pixels" }
                return checkNotNull(bitmap.readPixels()) { "Could not read rendered pixels" }
            } finally {
                bitmap.close()
                image.close()
            }
        } finally {
            scene.close()
        }
    }

    /**
     * Renders frames at a frozen frame time until every image request has finished and nothing has
     * started or finished for [STABLE_FRAMES] consecutive frames.
     *
     * Frame time stays at 0 so that how long the network takes cannot change how far animations
     * have advanced in the captured frame.
     */
    private suspend fun settle(scene: ImageComposeScene) {
        val deadline = TimeSource.Monotonic.markNow() + settleTimeout
        var stableFrames = 0
        var lastVersion = -1L

        while (stableFrames < STABLE_FRAMES) {
            scene.render(0L)

            val version = tracker.currentVersion
            stableFrames = if (tracker.inFlightCount == 0 && version == lastVersion) stableFrames + 1 else 0
            lastVersion = version

            if (deadline.hasPassedNow()) {
                throw RenderTimeoutException(
                    "${tracker.inFlightCount} image(s) still loading after $settleTimeout",
                )
            }

            // Gives Coil's fetch/decode (on other threads) a chance to finish between frames.
            delay(FRAME_GAP_MILLIS)
        }

        if (tracker.failureCount > 0) {
            log.warn("{} image(s) failed to load and are rendered empty", tracker.failureCount)
        }
    }

    public companion object {
        private const val STABLE_FRAMES = 3
        private const val FRAME_GAP_MILLIS = 5L
        private const val FAST_FORWARD_NANOS = 10_000_000_000L // 10 s

        private val log = LoggerFactory.getLogger(Renderer::class.java)

        private val renderDispatcher = Executors
            .newSingleThreadExecutor { runnable ->
                Thread(runnable, "kodex-render").apply { isDaemon = true }
            }.asCoroutineDispatcher()

        /** Quoted, strong ETag of the exact bytes served. */
        public fun strongEtag(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return "\"" + digest.joinToString("") { "%02x".format(it) }.take(32) + "\""
        }
    }
}

@Composable
internal fun Page(content: Content) {
    EinkTheme {
        PageUI(
            content = content,
            modifier = Modifier.fillMaxSize(),
            elementOverrides = { einkElementOverride(it) },
            theme = EinkPageTheme,
        )
    }
}
