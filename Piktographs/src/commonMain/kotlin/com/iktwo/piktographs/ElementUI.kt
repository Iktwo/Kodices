package com.iktwo.piktographs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_AREA
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.CheckboxUI
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
import com.iktwo.piktographs.ui.SEPARATOR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.SeperatorUI
import com.iktwo.piktographs.ui.TextAreaUI
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.UnknownElementUI
import com.iktwo.piktographs.utils.debugModifier

@Composable
fun ElementUI(
    element: ProcessedElement,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
    inputHandler: InputHandler,
    textInputData: SnapshotStateMap<String, String?>,
    booleanInputData: SnapshotStateMap<String, Boolean>,
    validityMap: SnapshotStateMap<String, Boolean>,
) {
    Box(
        modifier = Modifier.then(debugModifier()),
    ) {
        var updatedElement = element

        if (element.requiresValidElements.isNotEmpty()) {
            val enabled by remember {
                derivedStateOf {
                    element.requiresValidElements
                        .map { requiredElementId ->
                            validityMap[requiredElementId] ?: false
                        }.firstOrNull { !it } == null
                }
            }
            updatedElement = updatedElement.copy(enabled = enabled)
        }

        when {
            updatedElement.type == ROW_ELEMENT_TYPE -> {
                RowUI(updatedElement)
            }

            updatedElement.type == INPUT_ELEMENT_TEXT_INPUT && updatedElement is InputElement -> {
                val input by remember {
                    derivedStateOf {
                        textInputData[updatedElement.id] ?: element.text
                    }
                }

                val validity by remember {
                    derivedStateOf {
                        validityMap[updatedElement.id]
                    }
                }

                if (input != null) {
                    updatedElement = updatedElement.copy(text = input)
                }

                println("id: ${element.id} input: $input updatedElement: ${updatedElement.text} textInputData: ${textInputData.toMap()}")

                if (validity != updatedElement.isValid) {
                    validityMap[element.id] = updatedElement.isValid
                }

                TextInputUI(updatedElement, inputHandler)
            }

            updatedElement.type == INPUT_ELEMENT_TEXT_AREA && updatedElement is InputElement -> {
                val input by remember {
                    derivedStateOf {
                        textInputData[updatedElement.id] ?: updatedElement.text
                    }
                }
                TextAreaUI(updatedElement, inputHandler, input)
            }

            updatedElement.type == INPUT_ELEMENT_CHECKBOX && updatedElement is InputElement -> {
                val input by remember { derivedStateOf { booleanInputData[updatedElement.id] } }
                CheckboxUI(updatedElement, inputHandler, input)
            }

            updatedElement.type == SEPARATOR_ELEMENT_TYPE -> {
                SeperatorUI(updatedElement)
            }

            else -> {
                if (!elementOverrides(updatedElement)) {
                    UnknownElementUI(updatedElement)
                }
            }
        }
    }
}
