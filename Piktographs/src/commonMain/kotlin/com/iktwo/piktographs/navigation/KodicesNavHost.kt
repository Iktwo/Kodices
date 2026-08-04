package com.iktwo.piktographs.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.actions.InterimAction
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ActionPerformer
import com.iktwo.piktographs.PageStyle
import com.iktwo.piktographs.PageUI
import com.iktwo.piktographs.VerticalListPageStyle
import com.iktwo.piktographs.ui.Theme

/**
 * State holder for server-driven navigation stack management in Piktographs.
 *
 * Maintains a stack of route strings. Use with [KodicesNavHost] to manage multi-page SDUI navigation.
 */
public class KodicesNavController internal constructor(
    initialRoute: String,
) {
    internal val backStack = mutableStateListOf(initialRoute)

    /**
     * The current active route at the top of the backstack.
     */
    public val currentRoute: String
        get() = backStack.lastOrNull() ?: ""

    /**
     * Pushes a new route onto the backstack.
     */
    public fun navigate(route: String) {
        if (route.isNotBlank()) {
            backStack.add(route)
        }
    }

    /**
     * Pops the current top route off the backstack if more than one route remains.
     * @return `true` if a route was popped, `false` otherwise.
     */
    public fun popBackStack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }
}

/**
 * Remembers a [KodicesNavController] state across recompositions for a given [initialRoute].
 */
@Composable
public fun rememberKodicesNavController(initialRoute: String): KodicesNavController {
    return remember(initialRoute) { KodicesNavController(initialRoute) }
}

/**
 * Represents the asynchronous loading state for a server-driven UI route.
 */
public sealed interface NavState {
    public data object Loading : NavState

    public data class Success(
        val content: Content,
    ) : NavState

    public data class Error(
        val throwable: Throwable,
    ) : NavState
}

/**
 * Server-Driven UI Navigation Container for Compose Multiplatform.
 *
 * Handles asynchronous route payload fetching, navigation stack management, loading indicators,
 * and error retry screens. Automatically intercepts [NavigateAction] ("navigate") and "back" actions.
 *
 * @param navController controller managing the navigation backstack.
 * @param fetchContent suspend function that fetches the [Content] model for a given route.
 * @param modifier Modifier applied to the root host container.
 * @param pageStyle page layout style (vertical or horizontal list).
 * @param elementOverrides custom composable override handler for specific element types.
 * @param theme styling theme passed to child composables.
 * @param actionPerformer handler for non-navigation UI actions.
 * @param loadingContent composable rendered while fetching route JSON.
 * @param errorContent composable rendered when fetching route JSON fails.
 */
@Composable
public fun KodicesNavHost(
    navController: KodicesNavController,
    fetchContent: suspend (route: String) -> Content?,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean = { false },
    theme: Theme = Theme(),
    actionPerformer: ActionPerformer = ActionPerformer { },
    loadingContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    },
    errorContent: @Composable (route: String, error: Throwable, onRetry: () -> Unit) -> Unit = { route, _, onRetry ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Failed to load route: $route",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    },
) {
    val currentRoute = navController.currentRoute
    var state by remember(currentRoute) { mutableStateOf<NavState>(NavState.Loading) }
    var reloadToken by remember { mutableStateOf(0) }

    LaunchedEffect(currentRoute, reloadToken) {
        state = NavState.Loading
        try {
            val content = fetchContent(currentRoute)
            if (content != null) {
                state = NavState.Success(content)
            } else {
                state = NavState.Error(IllegalStateException("No content returned for route $currentRoute"))
            }
        } catch (e: Throwable) {
            state = NavState.Error(e)
        }
    }

    val navActionPerformer = remember(navController, actionPerformer) {
        ActionPerformer { action ->
            when {
                action is NavigateAction -> {
                    navController.navigate(action.targetRoute)
                }

                action.type.equals("navigate", ignoreCase = true) -> {
                    val target = action.getRouteTarget()
                    if (!target.isNullOrBlank()) {
                        navController.navigate(target)
                    } else {
                        actionPerformer.onAction(action)
                    }
                }

                action.type.equals("back", ignoreCase = true) -> {
                    if (!navController.popBackStack()) {
                        actionPerformer.onAction(action)
                    }
                }

                else -> actionPerformer.onAction(action)
            }
        }
    }

    Box(modifier = modifier) {
        when (val currentState = state) {
            is NavState.Loading -> loadingContent()
            is NavState.Error -> errorContent(currentRoute, currentState.throwable) {
                reloadToken += 1
            }

            is NavState.Success -> {
                PageUI(
                    content = currentState.content,
                    pageStyle = pageStyle,
                    elementOverrides = elementOverrides,
                    theme = theme,
                    actionPerformer = navActionPerformer,
                )
            }
        }
    }
}

private fun Action.getRouteTarget(): String? {
    return if (this is InterimAction) {
        // If an interim action hasn't been processed by a descriptor
        null
    } else {
        null
    }
}
