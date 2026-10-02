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
import com.iktwo.kodices.actions.SimpleAction
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asJSONObjectOrNull
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.ActionPerformer
import com.iktwo.piktographs.PageStyle
import com.iktwo.piktographs.PageUI
import com.iktwo.piktographs.VerticalListPageStyle
import com.iktwo.piktographs.ui.ContentBottomSheet
import com.iktwo.piktographs.ui.ContentDialog
import com.iktwo.piktographs.ui.Theme

/**
 * Representation of an active modal destination presented over the current route.
 */
public data class ModalDestination(
    public val route: String,
    public val presentation: String = "dialog",
    public val title: String? = null,
)

/**
 * State holder for server-driven navigation stack management in Piktographs.
 *
 * Maintains a stack of route strings and supports modal dialogs / bottom sheets.
 * Use with [KodicesNavHost] to manage multi-page SDUI navigation.
 */
public class KodicesNavController internal constructor(
    initialRoute: String,
) {
    internal val backStack = mutableStateListOf(initialRoute)

    /**
     * Active modal destination, or null if no modal is currently presented.
     */
    public var activeModal: ModalDestination? by mutableStateOf(null)
        internal set

    /**
     * The current active route at the top of the backstack.
     */
    public val currentRoute: String
        get() = backStack.lastOrNull() ?: ""

    /**
     * Pushes a new route onto the backstack.
     */
    public fun navigate(route: String) {
        navigate(route, presentation = "push", title = null)
    }

    /**
     * Navigates to [route] using the given [presentation] mode ("push", "dialog", "bottom_sheet")
     * and optional [title].
     */
    public fun navigate(
        route: String,
        presentation: String = "push",
        title: String? = null,
    ) {
        if (route.isBlank()) return
        when (presentation.lowercase()) {
            "dialog", "modal" -> {
                activeModal = ModalDestination(
                    route = route,
                    presentation = "dialog",
                    title = title,
                )
            }

            "bottom_sheet", "bottomsheet", "sheet" -> {
                activeModal = ModalDestination(
                    route = route,
                    presentation = "bottom_sheet",
                    title = title,
                )
            }

            else -> {
                backStack.add(route)
            }
        }
    }

    /**
     * Shows a modal dialog or bottom sheet for [route].
     */
    public fun showModal(
        route: String,
        presentation: String = "dialog",
        title: String? = null,
    ) {
        navigate(route, presentation, title)
    }

    /**
     * Dismisses the active modal presentation if one is showing.
     * @return `true` if a modal was dismissed, `false` otherwise.
     */
    public fun dismissModal(): Boolean {
        if (activeModal != null) {
            activeModal = null
            return true
        }
        return false
    }

    /**
     * Pops the top modal if one is open, or pops the backstack if more than one route remains.
     * @return `true` if a route or modal was popped, `false` otherwise.
     */
    public fun popBackStack(): Boolean {
        if (dismissModal()) {
            return true
        }
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
 * error retry screens, and modal dialog / bottom sheet presentation.
 * Automatically intercepts [NavigateAction], [ModalAction], [DismissModalAction], and "back" actions.
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
                    navController.navigate(action.targetRoute, action.presentation, action.title)
                }

                action is ModalAction -> {
                    navController.showModal(action.targetRoute, action.presentation, action.title)
                }

                action is DismissModalAction ||
                    action.type.equals("dismiss_modal", ignoreCase = true) ||
                    action.type.equals("close_modal", ignoreCase = true) -> {
                    navController.dismissModal()
                }

                action.type.equals("modal", ignoreCase = true) ||
                    action.type.equals("dialog", ignoreCase = true) ||
                    action.type.equals("bottom_sheet", ignoreCase = true) ||
                    action.type.equals("sheet", ignoreCase = true) -> {
                    val target = action.getRouteTarget()
                    if (!target.isNullOrBlank()) {
                        val pres = action.getPresentation()
                            ?: if (action.type.contains("sheet", ignoreCase = true)) "bottom_sheet" else "dialog"
                        navController.showModal(target, pres, action.getModalTitle())
                    } else {
                        actionPerformer.onAction(action)
                    }
                }

                action.type.equals("navigate", ignoreCase = true) -> {
                    val target = action.getRouteTarget()
                    if (!target.isNullOrBlank()) {
                        val pres = action.getPresentation() ?: "push"
                        navController.navigate(target, pres, action.getModalTitle())
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

        val activeModal = navController.activeModal
        if (activeModal != null) {
            var modalState by remember(activeModal.route) {
                mutableStateOf<NavState>(NavState.Loading)
            }
            var modalReloadToken by remember(activeModal.route) { mutableStateOf(0) }

            LaunchedEffect(activeModal.route, modalReloadToken) {
                modalState = NavState.Loading
                try {
                    val content = fetchContent(activeModal.route)
                    if (content != null) {
                        modalState = NavState.Success(content)
                    } else {
                        modalState = NavState.Error(
                            IllegalStateException("No content returned for modal route ${activeModal.route}"),
                        )
                    }
                } catch (e: Throwable) {
                    modalState = NavState.Error(e)
                }
            }

            val modalActionPerformer = remember(navController, navActionPerformer) {
                ActionPerformer { action ->
                    when {
                        action is DismissModalAction ||
                            action.type.equals("dismiss_modal", ignoreCase = true) ||
                            action.type.equals("close_modal", ignoreCase = true) ||
                            action.type.equals("close", ignoreCase = true) ||
                            action.type.equals("dismiss", ignoreCase = true) -> {
                            navController.dismissModal()
                        }

                        action.type.equals("back", ignoreCase = true) -> {
                            navController.dismissModal()
                        }

                        action is NavigateAction && action.presentation == "push" -> {
                            navController.dismissModal()
                            navController.navigate(action.targetRoute)
                        }

                        action.type.equals("navigate", ignoreCase = true) &&
                            (action.getPresentation() == null || action.getPresentation() == "push") -> {
                            val target = action.getRouteTarget()
                            if (!target.isNullOrBlank()) {
                                navController.dismissModal()
                                navController.navigate(target)
                            } else {
                                navActionPerformer.onAction(action)
                            }
                        }

                        else -> navActionPerformer.onAction(action)
                    }
                }
            }

            val modalContent: @Composable () -> Unit = {
                when (val currentModalState = modalState) {
                    is NavState.Loading -> loadingContent()
                    is NavState.Error -> errorContent(activeModal.route, currentModalState.throwable) {
                        modalReloadToken += 1
                    }

                    is NavState.Success -> {
                        PageUI(
                            content = currentModalState.content,
                            pageStyle = pageStyle,
                            elementOverrides = elementOverrides,
                            theme = theme,
                            actionPerformer = modalActionPerformer,
                        )
                    }
                }
            }

            if (activeModal.presentation == "bottom_sheet") {
                ContentBottomSheet(
                    title = activeModal.title,
                    onDismissRequest = { navController.dismissModal() },
                    content = modalContent,
                )
            } else {
                ContentDialog(
                    title = activeModal.title,
                    onCloseRequest = { navController.dismissModal() },
                    content = modalContent,
                )
            }
        }
    }
}

private fun Action.getRouteTarget(): String? {
    if (this is NavigateAction) return targetRoute
    if (this is ModalAction) return targetRoute
    if (this is SimpleAction) {
        val constants = jsonValues[Constants.CONSTANTS]?.asJSONObjectOrNull()
        return constants?.get("targetRoute")?.asStringOrNull()
            ?: jsonValues["targetRoute"]?.asStringOrNull()
            ?: jsonValues["target"]?.asStringOrNull()
            ?: constants?.get("target")?.asStringOrNull()
            ?: jsonValues["route"]?.asStringOrNull()
            ?: constants?.get("route")?.asStringOrNull()
    }
    return null
}

private fun Action.getPresentation(): String? {
    if (this is NavigateAction) return presentation
    if (this is ModalAction) return presentation
    if (this is SimpleAction) {
        if (type.equals("dialog", ignoreCase = true)) return "dialog"
        if (type.equals("bottom_sheet", ignoreCase = true) || type.equals("sheet", ignoreCase = true)) return "bottom_sheet"
        val constants = jsonValues[Constants.CONSTANTS]?.asJSONObjectOrNull()
        return constants?.get("presentation")?.asStringOrNull()
            ?: jsonValues["presentation"]?.asStringOrNull()
    }
    return null
}

private fun Action.getModalTitle(): String? {
    if (this is NavigateAction) return title
    if (this is ModalAction) return title
    if (this is SimpleAction) {
        val constants = jsonValues[Constants.CONSTANTS]?.asJSONObjectOrNull()
        return constants?.get("title")?.asStringOrNull()
            ?: jsonValues["title"]?.asStringOrNull()
    }
    return null
}
