package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.iktwo.kodices.elements.ProcessedElement

public const val TEXT_ELEMENT_TYPE: String = "text"

private const val KEY_ALIGN = "align"
private const val KEY_FONT_WEIGHT = "fontWeight"
private const val KEY_FONT_FAMILY = "fontFamily"
private const val KEY_MAX_LINES = "maxLines"
private const val KEY_COLOR = "color"

/**
 * A run of text in one of the Material type scale styles.
 *
 * - `text`: what to show.
 * - `style`: a type scale name such as `headlineMedium`, `titleLarge` or `bodySmall`
 *   (default `bodyLarge`).
 * - `align`: `start`, `center` or `end`. Setting it makes the text take the full width.
 * - `fontWeight`: `normal`, `medium`, `semibold`, `bold` or a number.
 * - `fontFamily`: `monospace`, `serif` or `sans`. Without it the text uses the theme's font.
 * - `maxLines`: longer text is cut with an ellipsis.
 * - `tone`: `info`, `success`, `warning` or `error` colors the text from the theme.
 * - `color`: `#RRGGBB` or `#AARRGGBB`, which wins over `tone`. Without either the text follows the
 *   theme.
 */
@Composable
public fun TextUI(element: ProcessedElement) {
    val text = element.text ?: return
    val dimensions = LocalTheme.current.dimensions

    val style = MaterialTheme.typography.styleNamed(element.style) ?: MaterialTheme.typography.bodyLarge
    val align = parseTextAlign(element.stringValue(KEY_ALIGN))
    val maxLines = element.intValue(KEY_MAX_LINES)?.takeIf { it > 0 } ?: Int.MAX_VALUE

    Text(
        text = text,
        modifier = Modifier
            .then(if (align != null) Modifier.fillMaxWidth() else Modifier)
            .padding(horizontal = dimensions.padding, vertical = dimensions.verticalSpacing),
        style = style,
        color = parseColor(element.stringValue(KEY_COLOR)) ?: element.tone().colorOrNull() ?: LocalContentColor.current,
        fontWeight = parseFontWeight(element.displayValue(KEY_FONT_WEIGHT)),
        fontFamily = parseFontFamily(element.stringValue(KEY_FONT_FAMILY)),
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
