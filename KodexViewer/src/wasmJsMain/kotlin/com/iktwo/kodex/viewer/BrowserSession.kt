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

/**
 * Leaves the browser's own context menu alone. Compose cancels `contextmenu` on its canvas, which
 * takes away "Inspect", "Reload", "Save page" and everything an extension adds, and puts nothing
 * in their place here: the viewer has no context menu of its own. A capturing listener on the
 * window runs before Compose's and stops the event from reaching it, so nothing cancels it.
 */
internal fun keepBrowserContextMenu(): Unit = js("window.addEventListener('contextmenu', function (event) { event.stopImmediatePropagation(); }, true)")

/** `scheme://host[:port]` of the page: the API it was served next to. */
internal fun pageOrigin(): String = locationOrigin().takeIf { it.startsWith("http") } ?: FALLBACK_URL

/** The origin of a page served over http(s); null for one opened from disk. */
internal fun servedFrom(): String? = locationOrigin().takeIf { it.startsWith("http") }

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
