@file:OptIn(ExperimentalTime::class)

package com.iktwo.kodex.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.iktwo.kodices.content.Content
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/** What a dashboard shows. */
data class DashboardUiState(
    val serverUrl: String = "",
    val token: String = "",
    val connected: Boolean = false,
    val loading: Boolean = false,
    val screens: List<String> = emptyList(),
    val screen: String? = null,
    val content: Content? = null,
    val status: String? = null,
    val error: String? = null,
    /** What kind of failure [error] describes; null when there is none. */
    val failure: DashboardFailure? = null,
) {
    /** The token is a secret: keep it out of logs and crash reports. */
    override fun toString(): String =
        "DashboardUiState(serverUrl=$serverUrl, connected=$connected, loading=$loading, screens=$screens, screen=$screen, " +
            "status=$status, error=$error, failure=$failure)"
}

/**
 * The state of one dashboard and everything that changes it: connecting, choosing a screen and
 * polling it. It holds no coroutine scope of its own; [Dashboard] runs [connect] and [poll] in the
 * composition's, so nothing keeps polling once the dashboard is off screen.
 *
 * @param auth the instance installed on [client]'s HTTP client. This sets its connection.
 * @param clock the time shown in the status line, as text.
 */
class DashboardController(
    private val client: DashboardClient,
    private val auth: DashboardAuth,
    serverUrl: String = "",
    token: String = "",
    private val clock: () -> String = ::currentTime,
) {
    var state: DashboardUiState by mutableStateOf(DashboardUiState(serverUrl = serverUrl, token = token))
        private set

    /** The server being shown; null until [connect] succeeds and after [disconnect]. */
    var connection: DashboardConnection? by mutableStateOf(null)
        private set

    /** Changes when the user asks for the screen again, so the caller restarts [poll]. */
    var refreshRequests: Int by mutableIntStateOf(0)
        private set

    fun onServerUrlChanged(serverUrl: String) {
        state = state.copy(serverUrl = serverUrl)
    }

    fun onTokenChanged(token: String) {
        state = state.copy(token = token)
    }

    /** Asks the server for its screens. On failure the state says why and nothing else changes. */
    suspend fun connect(): Boolean {
        val target = DashboardConnection.of(state.serverUrl, state.token)
        state = state.copy(loading = true, error = null, failure = null)
        auth.connection = target

        return try {
            val available = client.screens(target)
            connection = target
            state = state.copy(
                connected = true,
                screens = available.screens,
                screen = available.defaultScreen,
                content = null,
                status = null,
            )
            true
        } catch (e: DashboardException) {
            auth.connection = null
            state = state.copy(loading = false, error = e.message, failure = e.failure)
            false
        }
    }

    /** Forgets the connection and what it showed. The address stays; the token only if [keepToken]. */
    fun disconnect(keepToken: Boolean = false) {
        connection = null
        auth.connection = null
        state = DashboardUiState(serverUrl = state.serverUrl, token = if (keepToken) state.token else "")
    }

    fun selectScreen(screen: String) {
        if (screen == state.screen) return
        state = state.copy(screen = screen, content = null, status = null, error = null, failure = null)
    }

    /** Fetches the current screen now instead of at the next interval. */
    fun refresh() {
        refreshRequests++
    }

    /**
     * Fetches [screen] and keeps fetching it as often as the server asks, until cancelled. Run it
     * again when the connection, the screen or [refreshRequests] changes.
     */
    suspend fun poll(
        connection: DashboardConnection,
        screen: String,
    ): Nothing {
        var etag: String? = null
        var refreshSeconds = DashboardClient.DEFAULT_REFRESH_SECONDS

        while (true) {
            state = state.copy(loading = true)

            when (val result = client.fetch(connection, screen, etag)) {
                is DashboardResult.Loaded -> {
                    etag = result.etag
                    refreshSeconds = result.refreshSeconds
                    state = state.copy(
                        loading = false,
                        content = result.content,
                        error = null,
                        failure = null,
                        status = "Updated ${clock()} · every ${describeInterval(refreshSeconds)}",
                    )
                }

                DashboardResult.NotModified -> {
                    state = state.copy(
                        loading = false,
                        error = null,
                        failure = null,
                        status = "Unchanged at ${clock()} · every ${describeInterval(refreshSeconds)}",
                    )
                }

                is DashboardResult.Failed -> {
                    // What is on screen stays there: a stale dashboard beats an empty one.
                    state = state.copy(loading = false, error = result.message, failure = result.failure)
                    refreshSeconds = minOf(refreshSeconds, RETRY_SECONDS)
                }
            }

            delay(refreshSeconds.seconds)
        }
    }

    companion object {
        /** After a failure the screen is asked for again at least this often. */
        const val RETRY_SECONDS: Int = 30
    }
}

internal fun currentTime(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}:${now.second.toString().padStart(2, '0')}"
}

internal fun describeInterval(seconds: Int): String =
    when {
        seconds % SECONDS_PER_MINUTE == 0 && seconds >= SECONDS_PER_MINUTE -> {
            val minutes = seconds / SECONDS_PER_MINUTE
            if (minutes == 1) "minute" else "$minutes minutes"
        }

        seconds == 1 -> "second"

        else -> "$seconds seconds"
    }

private const val SECONDS_PER_MINUTE = 60
