package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.iktwo.kodices.elements.ProcessedElement

public const val ROW_ELEMENT_TYPE: String = "row"

@Composable
public fun RowUI(element: ProcessedElement) {
    // Only the font family is inherited: the size and line height stay Piktographs' own, but a host
    // that sets a typography (or has to avoid system fonts, like the headless renderer) is respected.
    val fontFamily = LocalTextStyle.current.fontFamily
    val colors = LocalTheme.current.colors

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = LocalTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(LocalTheme.current.dimensions.verticalSpacing),
    ) {
        element.text?.let {
            Box(modifier = Modifier.padding(horizontal = LocalTheme.current.dimensions.padding)) {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.mainTextColor.takeOrElse { MaterialTheme.colorScheme.onSurface },
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = LocalTheme.current.fonts.primary,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = LocalTheme.current.dimensions.padding),
                color = colors.secondaryTextColor.takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant },
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = LocalTheme.current.fonts.secondary,
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
