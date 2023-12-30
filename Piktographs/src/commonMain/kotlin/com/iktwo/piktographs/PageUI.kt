package com.iktwo.piktographs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement

@Composable
fun PageUI(
    content: Content,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
) {
    when (pageStyle) {
        HorizontalListPageStyle -> {
            LazyRow(modifier = modifier) {
                renderElements(content.elements, elementOverrides)
            }
        }

        VerticalListPageStyle -> {
            LazyColumn(modifier = modifier) {
                renderElements(content.elements, elementOverrides)
            }
        }
    }
}

fun LazyListScope.renderElements(
    elements: List<ProcessedElement>,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
) {
    elements.forEach { element ->
        item {
            ElementUI(element, elementOverrides)
        }
    }
}
