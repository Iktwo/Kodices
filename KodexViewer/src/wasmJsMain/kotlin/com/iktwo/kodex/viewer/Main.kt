package com.iktwo.kodex.viewer

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * The viewer in a browser. Served from the same origin as the API (a dashboard server serving this
 * bundle at `/`), it needs no address: it talks to where it was loaded from, and asks for the token.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    keepBrowserContextMenu()
    val remembered = BrowserSession.load()

    // A page served over http(s) came from the dashboard server, and that server's
    // Content-Security-Policy lets the page talk to nothing else: its origin is the only address
    // that can work, so the form does not ask for one and a remembered address is not used.
    val origin = servedFrom()
    val config = ViewerConfig(
        serverUrl = origin ?: remembered?.baseUrl ?: pageOrigin(),
        token = remembered?.token.orEmpty(),
        // Only a connection this tab already made is resumed without asking.
        autoConnect = remembered != null,
        fixedServer = origin != null,
    )

    ComposeViewport {
        ViewerApp(
            config = config,
            onConnected = BrowserSession::save,
            onDisconnected = BrowserSession::clear,
        )
    }
}
