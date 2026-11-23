package com.iktwo.piktographs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_SWITCH
import com.iktwo.kodices.elements.INPUT_ELEMENT_TOGGLE
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.piktographs.components.CollapsingTopBar
import com.iktwo.piktographs.components.TopBarStyle
import com.iktwo.piktographs.ui.Constants
import com.iktwo.piktographs.ui.Constants.TOP_BAR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.LocalTheme
import com.iktwo.piktographs.ui.TABS_ELEMENT_TYPE
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

    CompositionLocalProvider(LocalTheme provides theme) {
        val logger = LocalLogger.current

        // Seeding runs in an effect, not in the composition body: writing to these maps on every
        // recomposition would overwrite whatever the user has typed since. Existing keys are left
        // alone for the same reason - the maps are the source of truth once the user has touched them.
        // Every input is seeded, wherever it is nested: a host reading the maps (to restore or
        // submit a form) sees the whole form, not just the inputs drawn so far.
        LaunchedEffect(content) {
            val inputs = content.elements.inputs()

            inputs.forEach { element ->
                if (!validityMap.containsKey(element.id)) {
                    validityMap[element.id] = element.isValid
                }

                if (element.type in BOOLEAN_INPUT_TYPES) {
                    if (!booleanInputData.containsKey(element.id)) {
                        booleanInputData[element.id] = element.jsonValues[Constants.ACTIVE_KEY]?.asBooleanOrNull() ?: false
                    }
                } else if (!textInputData.containsKey(element.id)) {
                    textInputData[element.id] = element.text
                }
            }

            // The maps are keyed by id, so inputs that share one share their value too.
            duplicateInputIds(inputs).forEach { id ->
                logger.warn("Input id \"$id\" is used by more than one input on this page; they will share one value.")
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
                    // Validate the typed value, not element.isValid (which checks the initial text).
                    validityMap[element.id] = element.isValid(value)
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
        // The first top-level tabs element stays put above the list, like the top bar: navigation
        // that scrolls away with the content is navigation the user has to scroll back to.
        val tabsElement = content.elements.firstOrNull { it.type == TABS_ELEMENT_TYPE }
        val listElements = remember(content.elements) {
            content.elements.filter { it.type != TOP_BAR_ELEMENT_TYPE && it !== tabsElement }
        }

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
                    Column {
                        topBarElement?.let {
                            CollapsingTopBar(
                                title = topBarElement.text,
                                scrollBehavior = scrollBehavior,
                                style = barStyle,
                            )
                        }

                        tabsElement?.let {
                            Surface { ElementUI(it) }
                        }
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
                                listElements,
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
                                listElements,
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

private val BOOLEAN_INPUT_TYPES = setOf(INPUT_ELEMENT_CHECKBOX, INPUT_ELEMENT_SWITCH, INPUT_ELEMENT_TOGGLE)

/** Every input in these elements, however deeply nested, in document order. */
internal fun List<ProcessedElement>.inputs(): List<InputElement> =
    flatMap { element ->
        listOfNotNull(element as? InputElement) + element.nestedElements.inputs()
    }

/** The ids more than one of [inputs] has, each listed once. */
internal fun duplicateInputIds(inputs: List<InputElement>): List<String> =
    inputs
        .groupingBy { it.id }
        .eachCount()
        .filterValues { it > 1 }
        .keys
        .toList()
