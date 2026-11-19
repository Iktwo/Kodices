package com.iktwo.kodex.viewer

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * The viewer in a browser. Served from the same origin as the API (a dashboard server serving this
 * bundle at `/`), it needs no address: it talks to where it was loaded from, and asks for the token.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val remembered = BrowserSession.load()

    val config = ViewerConfig(
        serverUrl = remembered?.baseUrl ?: pageOrigin(),
        token = remembered?.token.orEmpty(),
        // Only a connection this tab already made is resumed without asking.
        autoConnect = remembered != null,
    )

    ComposeViewport {
        ViewerApp(
            config = config,
            onConnected = BrowserSession::save,
            onDisconnected = BrowserSession::clear,
        )
    }
}
