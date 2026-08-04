package com.iktwo.piktographs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.components.CollapsingTopBar
import com.iktwo.piktographs.components.TopBarStyle
import com.iktwo.piktographs.ui.Constants.TOP_BAR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.DefaultTheme
import com.iktwo.piktographs.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun PageUI(
    content: Content,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean = { false },
    theme: Theme = Theme(),
    textInputData: SnapshotStateMap<String, String?> = rememberSaveable(saver = mapSaver()) {
        mutableStateMapOf()
    },
    booleanInputData: SnapshotStateMap<String, Boolean> = rememberSaveable(saver = mapSaver()) {
        mutableStateMapOf()
    },
    validityMap: SnapshotStateMap<String, Boolean> = rememberSaveable(saver = mapSaver()) {
        mutableStateMapOf()
    },
    onInputIdsPopulated: () -> Unit = { },
    onInputUpdated: () -> Unit = { },
    actionPerformer: ActionPerformer = ActionPerformer { },
) {
    val topAppBarState = rememberTopAppBarState()

    CompositionLocalProvider(DefaultTheme provides theme) {
        // Seeding runs in an effect, not in the composition body: writing to these maps on every
        // recomposition would overwrite whatever the user has typed since. Existing keys are left
        // alone for the same reason - the maps are the source of truth once the user has touched them.
        LaunchedEffect(content) {
            content.elements.forEach { element ->
                if (element is InputElement) {
                    if (!validityMap.containsKey(element.id)) {
                        validityMap[element.id] = element.isValid
                    }

                    if (!textInputData.containsKey(element.id)) {
                        textInputData[element.id] = element.text
                    }
                }
            }

            onInputIdsPopulated()
        }

        val inputHandler = object : InputHandler {
            override fun onTextInput(
                element: ProcessedElement,
                value: String,
            ) {
                textInputData[element.id] = value

                if (element is InputElement) {
                    validityMap[element.id] = element.isValid
                }

                onInputUpdated()
            }

            override fun onBooleanInput(
                element: ProcessedElement,
                value: Boolean,
            ) {
                booleanInputData[element.id] = value
            }
        }

        val topBarElement = content.elements.find { it.type == TOP_BAR_ELEMENT_TYPE }
        val barStyle = TopBarStyle.fromText(topBarElement?.style)
        val scrollBehavior = barStyle.scrollBehavior(topAppBarState)

        CompositionLocalProvider(
            LocalElementOverrides provides elementOverrides,
            LocalInputHandler provides inputHandler,
            LocalTextInputData provides textInputData,
            LocalBooleanInputData provides booleanInputData,
            LocalValidityMap provides validityMap,
            LocalActionPerformer provides actionPerformer,
        ) {
            Scaffold(
                modifier = modifier,
                topBar = {
                    topBarElement?.let {
                        CollapsingTopBar(
                            title = topBarElement.text,
                            scrollBehavior = scrollBehavior,
                            style = barStyle,
                        )
                    }
                },
            ) { innerPadding ->
                when (pageStyle) {
                    HorizontalListPageStyle -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                        ) {
                            renderElements(
                                content.elements,
                            )
                        }
                    }

                    VerticalListPageStyle -> {
                        // Modifier, not `modifier`: the caller's modifier is already applied to the
                        // Scaffold above, applying it again here doubles padding and sizing.
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                        ) {
                            renderElements(
                                content.elements,
                            )
                        }
                    }
                }
            }
        }
    }
}

public fun LazyListScope.renderElements(
    elements: List<ProcessedElement>,
) {
    elements.forEach { element ->
        item(key = element.id) {
            ElementUI(element)
        }
    }
}

/**
 * [Saver] for the input-state maps [PageUI] takes, so they survive configuration changes.
 *
 * Named the same as Compose's own `mapSaver`; import explicitly to avoid ambiguity.
 */
public fun <K, V> mapSaver(): Saver<SnapshotStateMap<K, V>, Any> {
    return Saver(
        save = { originalMap -> originalMap.toList() },
        restore = { savedList ->
            @Suppress("UNCHECKED_CAST")
            (savedList as? List<Pair<K, V>>)?.toTypedArray()?.let {
                mutableStateMapOf(*it)
            } ?: mutableStateMapOf()
        },
    )
}
