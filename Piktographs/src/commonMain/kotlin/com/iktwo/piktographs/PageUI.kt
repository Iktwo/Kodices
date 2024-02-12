package com.iktwo.piktographs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.DefaultTheme
import com.iktwo.piktographs.ui.Theme

@Composable
fun PageUI(
    content: Content,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
    theme: Theme = Theme(),
) {
    CompositionLocalProvider(DefaultTheme provides theme) {
        val textInputData = remember {
            mutableStateMapOf<String, String>()
        }

        val booleanInputData = remember {
            mutableStateMapOf<String, Boolean>()
        }

        val inputHandler = object : InputHandler {
            override fun onTextInput(key: String, value: String) {
                textInputData[key] = value
            }

            override fun onBooleanInput(key: String, value: Boolean) {
                booleanInputData[key] = value
            }
        }

        when (pageStyle) {
            HorizontalListPageStyle -> {
                LazyRow(modifier = modifier) {
                    renderElements(
                        content.elements,
                        elementOverrides,
                        inputHandler,
                        textInputData,
                        booleanInputData
                    )
                }
            }

            VerticalListPageStyle -> {
                LazyColumn(modifier = modifier) {
                    renderElements(
                        content.elements,
                        elementOverrides,
                        inputHandler,
                        textInputData,
                        booleanInputData
                    )
                }
            }
        }
    }
}

fun LazyListScope.renderElements(
    elements: List<ProcessedElement>,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
    inputHandler: InputHandler,
    textInputData: SnapshotStateMap<String, String>,
    booleanInputData: SnapshotStateMap<String, Boolean>,
) {
    elements.forEach { element ->
        item {
            ElementUI(element, elementOverrides, inputHandler, textInputData, booleanInputData)
        }
    }
}
