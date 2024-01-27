package com.iktwo.piktographs

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
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

        else -> {
            if (!elementOverrides(element)) {
                UnknownElementUI(element)
            }
        }
    }
}
