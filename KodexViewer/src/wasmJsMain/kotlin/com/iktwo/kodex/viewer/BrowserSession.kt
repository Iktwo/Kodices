// js(...) is how this file reaches window.location and sessionStorage.
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.iktwo.kodex.viewer

import com.iktwo.kodex.client.DashboardConnection
import kotlin.js.ExperimentalWasmJsInterop

/**
 * Remembers the connection for as long as the browser tab lives, so a reload does not ask for the
 * token again.
 *
 * `sessionStorage` and nothing more durable: it is per tab and gone when the tab closes. The token
 * is never put in the URL (it would end up in history and server logs) or in `localStorage` (it
 * would outlive the session on a shared machine). A browser that refuses storage just asks again.
 */
internal object BrowserSession {
    private const val KEY_URL = "kodex.viewer.url"
    private const val KEY_TOKEN = "kodex.viewer.token"

    fun load(): DashboardConnection? {
        val url = sessionGet(KEY_URL)?.takeIf { it.isNotBlank() } ?: return null
        return DashboardConnection(url, sessionGet(KEY_TOKEN)?.takeIf { it.isNotBlank() })
    }

    fun save(connection: DashboardConnection) {
        sessionSet(KEY_URL, connection.baseUrl)
        sessionSet(KEY_TOKEN, connection.token.orEmpty())
    }

    fun clear() {
        sessionRemove(KEY_URL)
        sessionRemove(KEY_TOKEN)
    }
}

/** `scheme://host[:port]` of the page: the API it was served next to. */
internal fun pageOrigin(): String = locationOrigin().takeIf { it.startsWith("http") } ?: FALLBACK_URL

/** A page opened from disk has no origin worth connecting to. */
private const val FALLBACK_URL = "http://localhost:8090"

private fun locationOrigin(): String = js("window.location.origin")

// Storage access throws when the browser blocks it (private modes, some embedded views).
private fun sessionGet(key: String): String? = js("{ try { return window.sessionStorage.getItem(key); } catch (e) { return null; } }")

private fun sessionSet(
    key: String,
    value: String,
) {
    js("{ try { window.sessionStorage.setItem(key, value); } catch (e) { } }")
}

private fun sessionRemove(key: String) {
    js("{ try { window.sessionStorage.removeItem(key); } catch (e) { } }")
}
