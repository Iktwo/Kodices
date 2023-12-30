package com.iktwo.piktographs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.UnknownElementUI

@Composable
fun ElementUI(
    element: ProcessedElement,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
) {
    when {
        element.type == "row" -> {
            Text(element.toString())
        }

        else -> {
            if (!elementOverrides(element)) {
                UnknownElementUI(element)
            }
        }
    }
}
