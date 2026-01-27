package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.iktwo.kodices.elements.ProcessedElement

const val ROW_ELEMENT_TYPE = "row"

@Composable
fun RowUI(element: ProcessedElement) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        element.text?.let {
            Box(modifier = Modifier.padding(horizontal = DefaultTheme.current.dimensions.padding)) {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        fontSize = DefaultTheme.current.fonts.primary,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Proportional,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                    ),
                )
            }
        }

        element.textSecondary?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth().padding(horizontal = DefaultTheme.current.dimensions.padding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(
                    fontSize = DefaultTheme.current.fonts.secondary,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Proportional,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
            )
        }

        for (nestedElement in element.nestedElements) {
            NestedElementUI(nestedElement)
        }
    }
}
