package com.iktwo.piktographs.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.RowUI

@Preview
@Composable
fun RowUIPreview() {
    RowUI(
        ProcessedElement(
            type = "row",
            id = "row",
            text = "Row title",
            textSecondary = "Row subtitle",
        ),
    )
}
