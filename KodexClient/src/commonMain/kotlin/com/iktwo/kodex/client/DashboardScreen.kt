package com.iktwo.kodex.client

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.PageUI
import com.iktwo.piktographs.navigation.NavigateAction
import com.iktwo.piktographs.ui.TABS_ELEMENT_TYPE
import com.iktwo.piktographs.ui.Theme
import kotlinx.coroutines.launch

/** What the connect form says above its fields. */
data class DashboardIntro(
    val title: String = "Live dashboard",
    val text: String? = null,
)

/**
 * A live dashboard: connects to a server, fetches a screen's JSON from `/content/<screen>.json`,
 * renders it with Piktographs and asks again when the server says to.
 *
 * @param autoConnect connect right away with the address and token [controller] starts with,
 *   instead of waiting for the form.
 * @param onConnected called once a connection is made: where a host may remember it.
 * @param onDisconnected called when the user disconnects: where a host forgets it.
 */
@Composable
fun Dashboard(
    controller: DashboardController,
    modifier: Modifier = Modifier,
    intro: DashboardIntro = DashboardIntro(),
    autoConnect: Boolean = false,
    theme: Theme = Theme(),
    elementOverrides: @Composable (ProcessedElement) -> Boolean = { false },
    onConnected: (DashboardConnection) -> Unit = {},
    onDisconnected: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val state = controller.state

    LaunchedEffect(controller) {
        if (autoConnect && controller.connection == null) controller.connect()
    }

    val connection = controller.connection
    LaunchedEffect(connection) { connection?.let(onConnected) }

    // The polling loop. It restarts when the server or the screen changes or the user asks for a
    // refresh, and is cancelled when the dashboard leaves the composition, so nothing keeps
    // polling in the background.
    val screen = state.screen
    LaunchedEffect(connection, screen, controller.refreshRequests) {
        if (connection != null && screen != null) controller.poll(connection, screen)
    }

    DashboardScreen(
        state = state,
        onServerUrlChanged = controller::onServerUrlChanged,
        onTokenChanged = controller::onTokenChanged,
        onConnect = { scope.launch { controller.connect() } },
        onDisconnect = {
            controller.disconnect()
            onDisconnected()
        },
        onScreenSelected = controller::selectScreen,
        onRefresh = controller::refresh,
        modifier = modifier,
        intro = intro,
        theme = theme,
        elementOverrides = elementOverrides,
    )
}

/** [Dashboard] without the behaviour: what it draws for a given [state]. */
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onServerUrlChanged: (String) -> Unit,
    onTokenChanged: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
    intro: DashboardIntro = DashboardIntro(),
    theme: Theme = Theme(),
    elementOverrides: @Composable (ProcessedElement) -> Boolean = { false },
) {
    Surface(modifier = modifier.fillMaxSize()) {
        if (!state.connected) {
            DashboardConnectForm(state, intro, onServerUrlChanged, onTokenChanged, onConnect)
            return@Surface
        }

        val content = state.content

        Column(modifier = Modifier.fillMaxSize()) {
            DashboardStatusBar(
                state = state,
                // A screen with its own tabs brings its navigation with it; a second row of the
                // same links above it would only cost height.
                showScreens = content == null || content.elements.none { it.type == TABS_ELEMENT_TYPE },
                onScreenSelected = onScreenSelected,
                onRefresh = onRefresh,
                onDisconnect = onDisconnect,
            )

            // With a screen on display a failure is a banner over it: a stale dashboard beats an
            // empty one. Without one it is the whole page, below.
            if (content != null) {
                state.error?.let { DashboardErrorBanner(it, state.failure, onRefresh, onDisconnect) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when {
                    content != null -> {
                        PageUI(
                            content = content,
                            modifier = Modifier.fillMaxSize(),
                            elementOverrides = elementOverrides,
                            theme = theme,
                            // A screen can link to another one: {"type": "navigate", "targetRoute": "<screen>"}.
                            actionPerformer = object : ActionPerformer {
                                override fun onAction(action: Action) {
                                    if (action is NavigateAction && action.targetRoute.isNotBlank()) {
                                        onScreenSelected(action.targetRoute)
                                    }
                                }
                            },
                        )
                    }

                    state.error != null -> {
                        DashboardErrorPage(state.error, state.failure, onRefresh, onDisconnect)
                    }

                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatusBar(
    state: DashboardUiState,
    showScreens: Boolean,
    onScreenSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showScreens) {
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.screens.forEach { screen ->
                        FilterChip(
                            selected = screen == state.screen,
                            onClick = { onScreenSelected(screen) },
                            label = { Text(screen) },
                        )
                    }
                }
            } else {
                Text(
                    text = listOfNotNull(displayHost(state.serverUrl), state.status).joinToString(" · "),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp).testTag(TAG_LOADING), strokeWidth = 2.dp)
            }

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
            TextButton(onClick = onDisconnect) { Text("Disconnect") }
        }
    }

    if (showScreens) {
        state.status?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun DashboardErrorBanner(
    message: String,
    failure: DashboardFailure?,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            FailureAction(failure, onRefresh, onDisconnect)
        }
    }
}

@Composable
private fun DashboardErrorPage(
    message: String,
    failure: DashboardFailure?,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier.widthIn(max = FORM_MAX_WIDTH).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = when (failure) {
                DashboardFailure.Unauthorized -> "Not authorized"
                DashboardFailure.Unreachable -> "Server unreachable"
                DashboardFailure.InvalidScreen -> "This screen is broken"
                DashboardFailure.NotFound -> "No such screen"
                else -> "Could not load this screen"
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FailureAction(failure, onRefresh, onDisconnect)
    }
}

/** What the user can do about a failure: a wrong token needs a new one, anything else another try. */
@Composable
private fun FailureAction(
    failure: DashboardFailure?,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
) {
    if (failure == DashboardFailure.Unauthorized) {
        TextButton(onClick = onDisconnect) { Text("Change token") }
    } else {
        TextButton(onClick = onRefresh) { Text("Retry") }
    }
}

@Composable
private fun DashboardConnectForm(
    state: DashboardUiState,
    intro: DashboardIntro,
    onServerUrlChanged: (String) -> Unit,
    onTokenChanged: (String) -> Unit,
    onConnect: () -> Unit,
) {
    val canConnect = !state.loading && state.serverUrl.isNotBlank()

    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = FORM_MAX_WIDTH).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(intro.title, style = MaterialTheme.typography.titleLarge)
            intro.text?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = onServerUrlChanged,
                label = { Text("Server address") },
                singleLine = true,
                isError = state.failure == DashboardFailure.InvalidAddress || state.failure == DashboardFailure.Unreachable,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth().testTag(TAG_SERVER_URL),
            )

            TokenField(
                token = state.token,
                onTokenChanged = onTokenChanged,
                isError = state.failure == DashboardFailure.Unauthorized,
                onGo = { if (canConnect) onConnect() },
            )

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = onConnect, enabled = canConnect) {
                Text(if (state.loading) "Connecting…" else "Connect")
            }
        }
    }
}

/**
 * The token, obscured. A secure text field owns its text (a [TextFieldState]) instead of taking a
 * value and a callback, so this keeps that state and the caller's [token] the same: what is typed
 * goes out through [onTokenChanged], and a [token] changed from outside replaces the text.
 */
@Composable
private fun TokenField(
    token: String,
    onTokenChanged: (String) -> Unit,
    isError: Boolean,
    onGo: () -> Unit,
) {
    val fieldState = rememberTextFieldState(token)
    val currentOnTokenChanged by rememberUpdatedState(onTokenChanged)
    val currentOnGo by rememberUpdatedState(onGo)

    LaunchedEffect(fieldState) {
        snapshotFlow { fieldState.text.toString() }.collect { currentOnTokenChanged(it) }
    }
    LaunchedEffect(token) {
        if (fieldState.text.toString() != token) fieldState.setTextAndPlaceCursorAtEnd(token)
    }

    OutlinedSecureTextField(
        state = fieldState,
        label = { Text("Token (if the server has one)") },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = ImeAction.Go),
        onKeyboardAction = { currentOnGo() },
        modifier = Modifier.fillMaxWidth().testTag(TAG_TOKEN),
    )
}

/** `host:port` of an address, or the address itself when it is not a URL. */
internal fun displayHost(serverUrl: String): String =
    serverUrl
        .trim()
        .substringAfter("://")
        .substringBefore('/')
        .ifBlank { serverUrl.trim() }

const val TAG_SERVER_URL: String = "dashboard-server-url"
const val TAG_TOKEN: String = "dashboard-token"
const val TAG_LOADING: String = "dashboard-loading"

private val FORM_MAX_WIDTH = 480.dp
