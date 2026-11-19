package com.iktwo.piktographs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val LIST_ITEM_ELEMENT_TYPE: String = "listItem"

private const val KEY_ICON = "icon"
private const val KEY_VALUE = "value"
private const val KEY_BADGE = "badge"
private const val KEY_TRAILING_ICON = "trailingIcon"
private const val KEY_DENSE = "dense"
private const val KEY_VARIANT = "variant"

private const val VARIANT_TONAL = "tonal"

/**
 * One line of a list: an optional leading icon, a title with an optional subtitle, and an optional
 * value or icon at the end. Unlike `row`, which is text only, this is the shape of an agenda entry,
 * a sensor reading or a settings line.
 *
 * - `text`, `textSecondary`: title and subtitle.
 * - `icon`: leading icon name (see [IconUI]).
 * - `value`: text at the end of the line, a string or a number ("09:30", 42).
 * - `badge`: a short label in a pill before `value` ("New"). A blank one is left out.
 * - `trailingIcon`: icon name at the end, after `value`.
 * - `tone`: `info`, `success`, `warning` or `error` colors the leading icon and the badge.
 * - `variant`: `tonal` puts the line on a rounded fill in its tone, for something that should not
 *   be missed (an alert). The default has no background.
 * - `dense`: `true` for smaller text and less padding, for long lists and key/value lines.
 *
 * With actions the whole line is clickable.
 */
@Composable
public fun ListItemUI(element: ProcessedElement) {
    val dimensions = LocalTheme.current.dimensions
    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current

    val icon = element.stringValue(KEY_ICON)?.let(::resolveImageVector)
    val value = element.displayValue(KEY_VALUE)
    val badge = element.displayValue(KEY_BADGE)?.takeIf { it.isNotBlank() }
    val trailingIcon = element.stringValue(KEY_TRAILING_ICON)?.let(::resolveImageVector)
    val tone = element.tone()
    val toneColor = tone.colorOrNull()
    val dense = element.booleanValue(KEY_DENSE) == true
    val tonal = element.stringValue(KEY_VARIANT)?.lowercase()?.trim() == VARIANT_TONAL

    val typography = MaterialTheme.typography
    val iconSize = if (dense) DENSE_ICON_SIZE else ICON_SIZE
    val verticalPadding = if (dense) dimensions.verticalSpacing else dimensions.padding

    val container = if (tonal) {
        val shape = RoundedCornerShape(TONAL_RADIUS)
        Modifier
            .padding(horizontal = dimensions.padding, vertical = dimensions.verticalSpacing)
            .clip(shape)
            .background(tone.containerColorOrNull() ?: MaterialTheme.colorScheme.surfaceContainerHigh)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(container)
            .then(
                if (element.actions.isNotEmpty()) {
                    Modifier.clickable(enabled = isEnabled) {
                        element.actions.forEach { actionPerformer.onAction(it) }
                    }
                } else {
                    Modifier
                },
            ).padding(horizontal = dimensions.padding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.padding),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = toneColor ?: LocalContentColor.current,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (dense) 0.dp else dimensions.verticalSpacing),
        ) {
            element.text?.let {
                Text(
                    text = it,
                    style = if (dense) typography.bodyMedium else typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            element.textSecondary?.let {
                Text(
                    text = it,
                    style = if (dense) typography.bodySmall else typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        badge?.let {
            Pill(text = it, tone = tone, variant = PillVariant.Tonal, style = typography.labelMedium)
        }

        value?.let {
            Text(
                text = it,
                style = if (dense) typography.labelLarge else typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        trailingIcon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}

private val ICON_SIZE = 24.dp
private val DENSE_ICON_SIZE = 18.dp
private val TONAL_RADIUS = 12.dp
