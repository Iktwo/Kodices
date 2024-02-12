package com.iktwo.piktographs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.UnknownElementUI

@Composable
fun ElementUI(
    element: ProcessedElement,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
    inputHandler: InputHandler,
    textInputData: SnapshotStateMap<String, String>
) {
    when {
        element.type == ROW_ELEMENT_TYPE -> {
            RowUI(element)
        }

        element.type == INPUT_ELEMENT_TEXT_INPUT && element is InputElement -> {
            TextInputUI(element, inputHandler, textInputData[element.inputKey])
        }

        element.type == INPUT_ELEMENT_TEXT_AREA && element is InputElement -> {
            // TODO: implement this
        }

        element.type == INPUT_ELEMENT_CHECKBOX && element is InputElement -> {
            CheckboxUI(element)
        }

        element.type == SEPARATOR_ELEMENT_TYPE -> {
            SeperatorUI(element)
        }

        else -> {
            if (!elementOverrides(element)) {
                UnknownElementUI(element)
            }
        }
    }
}
