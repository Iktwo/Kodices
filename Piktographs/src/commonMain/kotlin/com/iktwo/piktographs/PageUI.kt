package com.iktwo.piktographs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.iktwo.kodices.content.Content
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ui.DefaultTheme
import com.iktwo.piktographs.ui.Theme

@Composable
fun PageUI(
    content: Content,
    modifier: Modifier = Modifier,
    pageStyle: PageStyle = VerticalListPageStyle,
    elementOverrides: @Composable (ProcessedElement) -> Boolean,
    theme: Theme = Theme()
) {
    CompositionLocalProvider(DefaultTheme provides theme) {
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
