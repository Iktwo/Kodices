package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val STAT_ELEMENT_TYPE: String = "stat"

private const val KEY_VALUE = "value"
private const val KEY_UNIT = "unit"
private const val KEY_ICON = "icon"
private const val KEY_TREND = "trend"
private const val KEY_ALIGN = "align"
private const val KEY_SIZE = "size"

/**
 * A single figure with its label: the tile a dashboard is made of.
 *
 * - `text`: the label above the figure ("Living room").
 * - `value`: the figure, a string or a number ("21.5").
 * - `unit`: shown after the figure, smaller ("°C").
 * - `textSecondary`: a caption under it ("2° warmer than yesterday").
 * - `trend`: `up` or `down` puts an arrow before the caption.
 * - `icon`: an icon name (see [IconUI]) shown before the label.
 * - `align`: `start` (default), `center` or `end`.
 * - `size`: of the figure: `large` (default), `medium` or `small`, for a row of many.
 * - `tone`: `info`, `success`, `warning` or `error` colors the figure.
 *
 * With actions the whole tile is clickable.
 */
@Composable
public fun StatUI(element: ProcessedElement) {
    val dimensions = LocalTheme.current.dimensions
    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current
    val valueStyle = when (element.stringValue(KEY_SIZE)?.lowercase()?.trim()) {
        "small" -> MaterialTheme.typography.titleLarge
        "medium" -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.displaySmall
    }
    val valueColor = element.tone().colorOrNull() ?: MaterialTheme.colorScheme.onSurface
    val value = element.displayValue(KEY_VALUE)
    val unit = element.displayValue(KEY_UNIT)
    val icon = element.stringValue(KEY_ICON)?.let(::resolveImageVector)
    val trend = when (element.stringValue(KEY_TREND)?.lowercase()?.trim()) {
        "up" -> Icons.Default.KeyboardArrowUp
        "down" -> Icons.Default.KeyboardArrowDown
        else -> null
    }
    val alignment = when (parseTextAlign(element.stringValue(KEY_ALIGN))) {
        TextAlign.Center -> Alignment.CenterHorizontally
        TextAlign.End -> Alignment.End
        else -> Alignment.Start
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (element.actions.isNotEmpty()) {
                    Modifier.clickable(enabled = isEnabled) {
                        element.actions.forEach { actionPerformer.onAction(it) }
                    }
                } else {
                    Modifier
                },
            ).padding(dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(dimensions.verticalSpacing),
        horizontalAlignment = alignment,
    ) {
        if (element.text != null || icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.horizontalSpacing),
            ) {
                icon?.let {
                    Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(LABEL_ICON_SIZE))
                }
                element.text?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (value != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.horizontalSpacing)) {
                Text(
                    text = value,
                    modifier = Modifier.alignByBaseline(),
                    style = valueStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                unit?.let {
                    Text(
                        text = it,
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        element.textSecondary?.let { caption ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                trend?.let {
                    // Decorative: the caption says what the arrow shows.
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(TREND_ICON_SIZE).clearAndSetSemantics { },
                    )
                }
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val LABEL_ICON_SIZE = 18.dp
private val TREND_ICON_SIZE = 18.dp
