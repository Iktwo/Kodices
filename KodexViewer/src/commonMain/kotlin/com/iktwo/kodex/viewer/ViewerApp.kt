package com.iktwo.kodex.viewer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iktwo.kodex.client.Dashboard
import com.iktwo.kodex.client.DashboardAuth
import com.iktwo.kodex.client.DashboardClient
import com.iktwo.kodex.client.DashboardConnection
import com.iktwo.kodex.client.DashboardController
import com.iktwo.kodex.client.DashboardIntro
import com.iktwo.kodex.client.dashboardHttpClient
import com.iktwo.kodex.client.dashboardParser
import com.iktwo.kodex.client.installDashboardImageLoader
import com.iktwo.piktographs.ui.Colors
import com.iktwo.piktographs.ui.Theme

/**
 * What the viewer starts with.
 *
 * @property serverUrl the address in the connect form.
 * @property token the token in the connect form. It is only ever held in memory here; where it
 *   came from (an argument, an environment variable, the browser tab's session) is the host's.
 * @property autoConnect connect right away instead of showing the form first.
 */
data class ViewerConfig(
    val serverUrl: String,
    val token: String = "",
    val autoConnect: Boolean = false,
    /** The address is not the user's to choose: the form asks for the token only. */
    val fixedServer: Boolean = false,
) {
    /** The token is a secret: keep it out of logs. */
    override fun toString(): String = "ViewerConfig(serverUrl=$serverUrl, token=${if (token.isEmpty()) "none" else "***"}, autoConnect=$autoConnect)"
}

/**
 * The whole viewer: a connect form, then the server's screens.
 *
 * @param onConnected called once a connection is made: where a host may remember it.
 * @param onDisconnected called when the user disconnects: where a host forgets it.
 */
@Composable
fun ViewerApp(
    config: ViewerConfig,
    modifier: Modifier = Modifier,
    onConnected: (DashboardConnection) -> Unit = {},
    onDisconnected: () -> Unit = {},
) {
    val auth = remember { DashboardAuth() }
    val httpClient = remember {
        // Before anything shows an image: Coil keeps the first loader it is given.
        dashboardHttpClient(auth).also(::installDashboardImageLoader)
    }
    DisposableEffect(httpClient) {
        onDispose { httpClient.close() }
    }

    val controller = remember(config) {
        DashboardController(
            client = DashboardClient(httpClient, dashboardParser()),
            auth = auth,
            serverUrl = config.serverUrl,
            token = config.token,
        )
    }

    ViewerTheme {
        Dashboard(
            controller = controller,
            modifier = modifier.fillMaxSize().safeDrawingPadding(),
            intro = if (config.fixedServer) FixedServerIntro else ViewerIntro,
            autoConnect = config.autoConnect,
            theme = viewerPageTheme(),
            onConnected = onConnected,
            onDisconnected = onDisconnected,
        )
    }
}

internal val ViewerIntro = DashboardIntro(
    title = "Kodex Viewer",
    text = "Connect to a server that serves Kodices screens (GET /screens and GET /content/<screen>.json), " +
        "such as KodexRenderer. The token is sent to that server only.",
)

/** For a viewer served by the dashboard server itself: there is one server, the one the page came from. */
internal val FixedServerIntro = DashboardIntro(
    title = "Kodex Viewer",
    text = "Enter this server's dashboard token. It is sent to this server only.",
    askForServer = false,
)

/** Material 3, light or dark with the system. */
@Composable
fun ViewerTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface(content = content)
    }
}

/**
 * Piktographs' defaults, except the separator: its default is a fixed near-black, which is a slab
 * on a light page and invisible on a dark one.
 */
@Composable
fun viewerPageTheme(): Theme = Theme(colors = Colors(separatorColor = MaterialTheme.colorScheme.outlineVariant))
