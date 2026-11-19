package com.iktwo.kodex.client

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.KodicesRegistry
import com.iktwo.kodices.actions.MessageAction
import com.iktwo.piktographs.navigation.DismissModalAction
import com.iktwo.piktographs.navigation.ModalAction
import com.iktwo.piktographs.navigation.NavigateAction
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth
import kotlin.concurrent.Volatile

/**
 * Adds the dashboard server's bearer token to requests going to that server, and to no other host:
 * the same HTTP client also loads the images a screen refers to, which can live anywhere.
 *
 * One instance per HTTP client. It used to be a process-wide object, which made two dashboards in
 * one process share a token.
 */
class DashboardAuth {
    /** The server the app is connected to; null when it is not connected. */
    @Volatile
    var connection: DashboardConnection? = null

    val plugin: ClientPlugin<Unit> = createClientPlugin("DashboardAuth") {
        onRequest { request, _ ->
            val current = connection ?: return@onRequest
            val token = current.token?.takeIf { it.isNotBlank() } ?: return@onRequest

            if (current.owns(request.url.build())) {
                request.bearerAuth(token)
            }
        }
    }
}

/**
 * The settings every dashboard HTTP client needs, whatever its engine: the token for the dashboard
 * server only, a timeout, and no redirects. A redirect is the one way a request that started at
 * the dashboard server could end up somewhere else, and nothing in the protocol needs one.
 */
fun HttpClientConfig<*>.dashboardDefaults(auth: DashboardAuth) {
    followRedirects = false

    install(auth.plugin)
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}

/** An HTTP client for a dashboard, on whichever engine the app has on its classpath. */
fun dashboardHttpClient(auth: DashboardAuth): HttpClient = HttpClient { dashboardDefaults(auth) }

/**
 * Makes Coil (which Piktographs' `image` element uses) load through [httpClient], so images on the
 * dashboard server carry its token. Call it before the first image is shown: Coil keeps the first
 * loader it is given.
 */
fun installDashboardImageLoader(httpClient: HttpClient) {
    SingletonImageLoader.setSafe { context ->
        ImageLoader
            .Builder(context)
            .components { add(KtorNetworkFetcherFactory(httpClient = { httpClient })) }
            .build()
    }
}

/**
 * A parser for screens that use the stock Piktographs elements and actions. An app with its own
 * element or action types passes its own parser to [DashboardClient] instead.
 */
fun dashboardParser(): KodicesParser =
    KodicesParser(
        registry = KodicesRegistry.of(
            actions = listOf(
                MessageAction.descriptor,
                NavigateAction.descriptor,
                ModalAction.descriptor,
                DismissModalAction.descriptor,
            ),
            allowGlobalFallback = false,
        ),
    )

private const val REQUEST_TIMEOUT_MILLIS = 10_000L
