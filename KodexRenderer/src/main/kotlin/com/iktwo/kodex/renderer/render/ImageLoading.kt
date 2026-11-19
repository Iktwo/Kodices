package com.iktwo.kodex.renderer.render

import coil3.EventListener
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.Uri
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.toUri
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.absolute
import kotlin.io.path.isRegularFile
import kotlin.time.Duration

/**
 * Counts Coil requests so the renderer can tell when every image on the page has either loaded or
 * failed. [version] moves on every start and every finish, so "unchanged across a few frames" means
 * nothing new started and nothing finished whose result has not been composed yet.
 */
internal class ImageLoadTracker : EventListener() {
    private val inFlight = AtomicInteger()
    private val version = AtomicLong()
    private val failures = AtomicInteger()

    val inFlightCount: Int get() = inFlight.get()
    val currentVersion: Long get() = version.get()
    val failureCount: Int get() = failures.get()

    fun reset() {
        failures.set(0)
    }

    override fun onStart(request: ImageRequest) {
        inFlight.incrementAndGet()
        version.incrementAndGet()
    }

    override fun onSuccess(
        request: ImageRequest,
        result: SuccessResult,
    ) = finished()

    override fun onError(
        request: ImageRequest,
        result: ErrorResult,
    ) {
        failures.incrementAndGet()
        finished()
    }

    override fun onCancel(request: ImageRequest) = finished()

    private fun finished() {
        inFlight.decrementAndGet()
        version.incrementAndGet()
    }
}

/**
 * Resolves `asset:<path>` image URLs to files under the screens' assets directory, so screens can
 * ship images that render without network access. Paths escaping the directory resolve to nothing.
 */
internal class AssetMapper(
    private val assetsDir: Path,
) : Mapper<String, Uri> {
    override fun map(
        data: String,
        options: Options,
    ): Uri? {
        if (!data.startsWith(ASSET_SCHEME)) return null
        val root = assetsDir.absolute().normalize()
        val file = root.resolve(data.removePrefix(ASSET_SCHEME)).normalize()
        if (!file.startsWith(root) || !file.isRegularFile()) return null
        return file.toUri().toString().toUri()
    }

    companion object {
        const val ASSET_SCHEME = "asset:"
    }
}

internal fun buildImageLoader(
    assetsDir: Path?,
    tracker: ImageLoadTracker,
    networkTimeout: Duration,
): ImageLoader {
    return ImageLoader
        .Builder(PlatformContext.INSTANCE)
        .components {
            assetsDir?.let { add(AssetMapper(it)) }
            add(
                KtorNetworkFetcherFactory(
                    httpClient = {
                        HttpClient(CIO) {
                            install(HttpTimeout) {
                                requestTimeoutMillis = networkTimeout.inWholeMilliseconds
                                connectTimeoutMillis = networkTimeout.inWholeMilliseconds
                            }
                        }
                    },
                ),
            )
        }.eventListener(tracker)
        .memoryCache {
            MemoryCache
                .Builder()
                .maxSizeBytes(MEMORY_CACHE_BYTES)
                .build()
        }
        // No disk cache: a cached image could outlive a change at its source, and the ETag would
        // then describe stale content.
        .diskCache(null)
        .crossfade(false)
        .build()
}

private const val MEMORY_CACHE_BYTES = 32L * 1024 * 1024
