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
fun PageUI(
    content: Content,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
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
) {
    val topAppBarState = rememberTopAppBarState()

    CompositionLocalProvider(DefaultTheme provides theme) {
        content.elements.forEach { element ->
            if (element is InputElement) {
                validityMap[element.id] = element.isValid

                textInputData[element.id] = element.text
            }
        }

        LaunchedEffect(true) {
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
                        LazyColumn(
                            modifier = modifier
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

fun LazyListScope.renderElements(
    elements: List<ProcessedElement>,
) {
    elements.forEach { element ->
        item(key = element.id) {
            ElementUI(element)
        }
    }
}

fun <K, V> mapSaver(): Saver<SnapshotStateMap<K, V>, Any> {
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
