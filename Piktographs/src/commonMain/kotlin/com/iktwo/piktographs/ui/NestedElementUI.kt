package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ElementUI

@Composable
fun NestedElementUI(nestedElement: ProcessedElement) {
    ElementUI(
        element = nestedElement,
    )
}
