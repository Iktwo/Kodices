package com.iktwo.piktographs

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.elements.InputElement
import com.iktwo.piktographs.ui.CheckboxUI
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.UnknownElementUI

@Composable
fun ElementUI(
    element: ProcessedElement,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
) {
    when {
        element.type == ROW_ELEMENT_TYPE -> {
            RowUI(element)
        }

        element.type == INPUT_ELEMENT_TEXT_INPUT && element is InputElement -> {
            TextInputUI(element)
        }

        element.type == INPUT_ELEMENT_CHECKBOX && element is InputElement -> {
            CheckboxUI(element)
        }

        else -> {
            if (!elementOverrides(element)) {
                UnknownElementUI(element)
            }
        }
    }
}
