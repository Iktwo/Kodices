package com.iktwo.piktographs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.InputElement
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
        val textInputData = rememberSaveable {
            mutableStateMapOf<String, String>()
        }

        val booleanInputData = rememberSaveable {
            mutableStateMapOf<String, Boolean>()
        }

        val validityMap = rememberSaveable {
            mutableStateMapOf<String, Boolean>()
        }

        content.elements.forEach { element ->
            if (element is InputElement) {
                validityMap[element.id] = element.isValid
            }
        }

        val inputHandler = object : InputHandler {
            override fun onTextInput(element: ProcessedElement, value: String) {
                textInputData[element.id] = value

                if (element is InputElement) {
                    validityMap[element.id] = element.isValid
                }
            }

            override fun onBooleanInput(element: ProcessedElement, value: Boolean) {
                booleanInputData[element.id] = value
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
                        booleanInputData,
                        validityMap
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
                        booleanInputData,
                        validityMap
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
    validityMap: SnapshotStateMap<String, Boolean>,
) {
    elements.forEach { element ->
        item(key = element.id) {
            ElementUI(element, elementOverrides, inputHandler, textInputData, booleanInputData, validityMap)
        }
    }
}
