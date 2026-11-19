package com.iktwo.kodex.renderer

import com.iktwo.kodex.renderer.render.RenderResult
import com.iktwo.kodex.renderer.render.RenderSpec
import com.iktwo.kodex.renderer.render.Renderer
import com.iktwo.kodex.renderer.render.ResolvedScreen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Caches renders by their inputs, so `/display` (which needs the ETag) followed by `/render`, or a
 * device revalidating with `If-None-Match`, does not render the same picture twice.
 *
 * The key is everything that feeds the render: the screen JSON, the [RenderSpec] and the minute
 * that `now` describes. The cached ETag is the hash of the PNG bytes themselves, so it changes
 * whenever anything else that feeds the picture (a remote image) changes too, once the entry expires.
 */
public class RenderService(
    private val repository: ScreenRepository,
    private val renderer: Renderer,
    private val maxEntries: Int = 16,
    private val ttlMillis: Long = 60_000,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    private data class Key(
        val screen: String,
        val jsonHash: String,
        val spec: RenderSpec,
        val dataKey: String,
    )

    private class Entry(
        val result: RenderResult,
        val createdAt: Long,
    )

    private val mutex = Mutex()
    private val cache = object : LinkedHashMap<Key, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, Entry>): Boolean = size > maxEntries
    }

    /** The screen's source JSON, or null if it does not exist. */
    public fun source(screen: String): String? = repository.read(screen)

    /** Names of the screens that can be rendered. */
    public fun screens(): List<String> = repository.list()

    /** The file an `asset:<path>` URL refers to, or null. */
    public fun asset(path: String): Path? = repository.asset(path)

    /**
     * [screen] for a client that renders it itself, or null if the screen does not exist.
     *
     * @throws com.iktwo.kodex.renderer.render.ScreenParseException if it is not valid Kodices content.
     */
    public fun resolve(screen: String): ResolvedScreen? = repository.read(screen)?.let(renderer::resolve)

    /** Renders [screen] at [spec], or returns null if the screen does not exist. */
    public suspend fun render(
        screen: String,
        spec: RenderSpec,
    ): RenderResult? {
        val json = repository.read(screen) ?: return null
        val key = Key(screen, sha256(json), spec, renderer.dataKey())

        return mutex.withLock {
            val now = currentTimeMillis()
            cache[key]?.takeIf { now - it.createdAt < ttlMillis }?.let { entry ->
                return@withLock with(entry.result) { RenderResult(png, etag, renderMillis, cached = true) }
            }

            renderer.render(json, spec).also { cache[key] = Entry(it, now) }
        }
    }

    private fun sha256(text: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
