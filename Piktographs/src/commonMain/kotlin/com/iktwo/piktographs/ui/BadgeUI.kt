package com.iktwo.piktographs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement

public const val BADGE_ELEMENT_TYPE: String = "badge"

private const val KEY_VARIANT = "variant"

/**
 * A short status label in a pill ("Online", "3 new").
 *
 * - `text`: the label. Without one (or with a blank one) nothing is drawn, so a badge bound to a
 *   value that is often empty needs no special case.
 * - `variant`: `filled` (default), `outlined` or `tonal` (a faint fill, for a list full of them).
 * - `tone`: `neutral` (default), `info`, `success`, `warning` or `error`.
 */
@Composable
public fun BadgeUI(element: ProcessedElement) {
    val text = element.text?.takeIf { it.isNotBlank() } ?: return

    Box(modifier = Modifier.padding(LocalTheme.current.dimensions.padding)) {
        Pill(text = text, tone = element.tone(), variant = parsePillVariant(element.stringValue(KEY_VARIANT)))
    }
}

internal enum class PillVariant { Filled, Outlined, Tonal }

internal fun parsePillVariant(variant: String?): PillVariant =
    when (variant?.lowercase()?.trim()) {
        "outlined", "outline" -> PillVariant.Outlined
        "tonal", "soft" -> PillVariant.Tonal
        else -> PillVariant.Filled
    }

/** The pill itself, shared by [BadgeUI], [ListItemUI] and the cells of [TableRowUI]. */
@Composable
internal fun Pill(
    text: String,
    tone: Tone,
    variant: PillVariant,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val scheme = MaterialTheme.colorScheme
    val toneColor = tone.colorOrNull()

    val (shape, textColor) = when (variant) {
        PillVariant.Outlined -> {
            val color = toneColor ?: scheme.onSurface
            Modifier.border(BADGE_BORDER, color, BADGE_SHAPE) to color
        }

        PillVariant.Tonal -> {
            val fill = tone.containerColorOrNull(TONAL_ALPHA) ?: scheme.surfaceContainerHighest
            Modifier.background(fill, BADGE_SHAPE) to (toneColor ?: scheme.onSurfaceVariant)
        }

        PillVariant.Filled -> {
            val fill = toneColor ?: scheme.primary
            Modifier.background(fill, BADGE_SHAPE) to if (toneColor == null) scheme.onPrimary else contentColorOn(fill)
        }
    }

    Text(
        text = text,
        modifier = modifier.then(shape).padding(horizontal = 10.dp, vertical = 4.dp),
        style = style,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private val BADGE_SHAPE = RoundedCornerShape(50)
private val BADGE_BORDER = 1.dp
private const val TONAL_ALPHA = 0.16f
