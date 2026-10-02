package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ElementUI

@Composable
public fun NestedElementUI(
    nestedElement: ProcessedElement,
    modifier: Modifier = Modifier,
) {
    ElementUI(
        element = nestedElement,
        modifier = modifier,
    )
}
