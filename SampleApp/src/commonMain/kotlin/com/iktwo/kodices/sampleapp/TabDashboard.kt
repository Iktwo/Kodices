package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.iktwo.kodex.client.Dashboard
import com.iktwo.kodex.client.DashboardAuth
import com.iktwo.kodex.client.DashboardClient
import com.iktwo.kodex.client.DashboardController
import com.iktwo.kodex.client.DashboardIntro
import com.iktwo.kodex.client.dashboardHttpClient
import com.iktwo.kodex.client.installDashboardImageLoader
import com.iktwo.kodices.sampleapp.ui.elementOverride

/*
 * The Dashboard tab is KodexClient's dashboard (the same one KodexViewer is built around) with the
 * sample's own parser and element overrides, so its custom elements work in live screens too.
 */

private val dashboardAuth = DashboardAuth()

/**
 * One HTTP client for the dashboard's JSON and for the images its screens refer to, so both carry
 * the server's token (and only to that server, see [DashboardAuth]).
 */
private val dashboardHttpClient by lazy { dashboardHttpClient(dashboardAuth) }

/**
 * Makes Coil (which Piktographs' `image` element uses) load through the dashboard's HTTP client.
 * Call it before the first image is shown: Coil keeps the first loader it is given.
 */
fun installDashboardImageLoader() {
    installDashboardImageLoader(dashboardHttpClient)
}

/**
 * A live dashboard: connects to a KodexRenderer server, fetches a screen's JSON from
 * `/content/<screen>.json`, renders it with Piktographs and asks again when the server says to.
 * It is the app counterpart of the e-ink device, which shows the same screen as a picture.
 */
@Composable
fun TabDashboard() {
    // Kept for as long as the tab is shown; leaving the tab forgets the connection, as before.
    val controller = remember {
        DashboardController(
            client = DashboardClient(dashboardHttpClient, kodicesParser),
            auth = dashboardAuth,
            serverUrl = DEFAULT_SERVER_URL,
        )
    }

    Dashboard(
        controller = controller,
        intro = DashboardTabIntro,
        elementOverrides = { elementOverride(it) },
    )
}

internal val DashboardTabIntro = DashboardIntro(
    title = "Live dashboard",
    text = "Connect to a KodexRenderer server (./gradlew :KodexRenderer:run) to render its screens " +
        "in the app, from the same JSON the server turns into pictures for e-ink devices.",
)

/** Android emulators reach the host machine at 10.0.2.2; everything else at localhost. */
internal const val DEFAULT_SERVER_URL = "http://localhost:8090"
