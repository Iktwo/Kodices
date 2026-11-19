package com.iktwo.piktographs.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import kotlin.math.abs

public const val CHART_ELEMENT_TYPE: String = "chart"

private const val KEY_VALUES = "values"
private const val KEY_VARIANT = "variant"
private const val KEY_MIN = "min"
private const val KEY_MAX = "max"
private const val KEY_LABELS = "labels"

private const val VARIANT_BAR = "bar"

/**
 * A small chart of one series: a line (sparkline) or bars. It has no axes or grid; it shows the
 * shape of the numbers next to a [StatUI] or under a title.
 *
 * - `values`: array of numbers. Usually bound with a processor.
 * - `variant`: `line` (default) or `bar`.
 * - `height`: of the plot, in dp (default 64).
 * - `min`, `max`: the value range. By default a line spans its own range and bars start at zero.
 * - `labels`: array of strings shown under the plot, spread evenly (one per bar, or just the ends
 *   of a line). Fewer labels than bars mark equal stretches of them: 4 under 24 hourly bars sit
 *   under bars 0, 6, 12 and 18.
 * - `text`, `textSecondary`: a title above and a caption below.
 * - `tone`: `info`, `success`, `warning` or `error` draws the series in that color instead of the
 *   theme's primary.
 */
@Composable
public fun ChartUI(element: ProcessedElement) {
    val dimensions = LocalTheme.current.dimensions
    val values = element.numberListValue(KEY_VALUES)
    val isBar = element.stringValue(KEY_VARIANT)?.lowercase() == VARIANT_BAR
    val height = (element.intValue(KEY_HEIGHT) ?: DEFAULT_HEIGHT_DP).coerceIn(MIN_HEIGHT_DP, MAX_HEIGHT_DP).dp
    val labels = element.stringListValue(KEY_LABELS)
    val range = chartRange(values, isBar, element.numberValue(KEY_MIN), element.numberValue(KEY_MAX))
    val color = element.tone().colorOrNull() ?: MaterialTheme.colorScheme.primary

    // A chart is a picture: say what it shows to anyone who cannot see it.
    val description = listOfNotNull(element.text, values.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.toString() })
        .joinToString(": ")

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(dimensions.verticalSpacing),
    ) {
        element.text?.let {
            Text(text = it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .semantics { contentDescription = description },
        ) {
            if (values.isEmpty()) return@Canvas

            if (isBar) {
                drawBars(values, range, color)
            } else {
                drawLine(values, range, color)
            }
        }

        if (labels.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        // Bars get a label each, centered under its bar; a line only labels points.
                        // Fewer labels than bars (4 for 24 hours) each mark where their stretch of
                        // bars begins, so they start there instead of sitting in its middle.
                        modifier = if (isBar) Modifier.weight(1f) else Modifier,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = when {
                            !isBar -> null
                            labels.size < values.size -> TextAlign.Start
                            else -> TextAlign.Center
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        element.textSecondary?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * The value range the plot covers. Bars include zero so their length is proportional to the value;
 * a line uses the data's own range so small changes stay visible. A flat series gets a range around
 * its value rather than a zero-height one.
 */
internal fun chartRange(
    values: List<Float>,
    isBar: Boolean,
    min: Float?,
    max: Float?,
): ClosedFloatingPointRange<Float> {
    val dataMin = values.minOrNull() ?: 0f
    val dataMax = values.maxOrNull() ?: 0f

    val low = min ?: if (isBar) minOf(0f, dataMin) else dataMin
    val high = max ?: if (isBar) maxOf(0f, dataMax) else dataMax

    return when {
        high > low -> low..high
        isBar -> low..(low + 1f)
        else -> (low - 1f)..(low + 1f)
    }
}

/** Where [value] sits in [range], from 0 (bottom) to 1 (top). */
internal fun ClosedFloatingPointRange<Float>.fraction(value: Float): Float = ((value - start) / (endInclusive - start)).coerceIn(0f, 1f)

private fun DrawScope.drawLine(
    values: List<Float>,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
) {
    val stroke = LINE_WIDTH.toPx()
    // Inset by half the stroke so the line is not clipped at the top and bottom of the range.
    val top = stroke / 2
    val plotHeight = size.height - stroke

    fun y(value: Float) = top + plotHeight * (1f - range.fraction(value))

    if (values.size == 1) {
        drawLine(color, Offset(0f, y(values[0])), Offset(size.width, y(values[0])), stroke, StrokeCap.Round)
        return
    }

    val step = (size.width - stroke) / (values.size - 1)
    val path = Path()
    values.forEachIndexed { index, value ->
        val x = stroke / 2 + step * index
        if (index == 0) path.moveTo(x, y(value)) else path.lineTo(x, y(value))
    }
    drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawBars(
    values: List<Float>,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
) {
    val slot = size.width / values.size
    val gap = minOf(BAR_GAP.toPx(), slot / 2)
    val barWidth = slot - gap
    val baseline = size.height * (1f - range.fraction(0f))
    val radius = CornerRadius(minOf(BAR_RADIUS.toPx(), barWidth / 2))
    val minBar = MIN_BAR_HEIGHT.toPx()

    values.forEachIndexed { index, value ->
        val valueY = size.height * (1f - range.fraction(value))
        // A zero still gets a sliver, so "no value" and "no bar" can be told apart.
        val barHeight = maxOf(abs(baseline - valueY), minBar)
        val barTop = if (value >= 0f) minOf(valueY, baseline - barHeight).coerceAtLeast(0f) else baseline

        drawRoundRect(
            color = color,
            topLeft = Offset(slot * index + gap / 2, barTop),
            size = Size(barWidth, minOf(barHeight, size.height - barTop)),
            cornerRadius = radius,
        )
    }
}

private const val DEFAULT_HEIGHT_DP = 64
private const val MIN_HEIGHT_DP = 16
private const val MAX_HEIGHT_DP = 400

private val LINE_WIDTH = 2.dp
private val BAR_GAP = 4.dp
private val BAR_RADIUS = 2.dp
private val MIN_BAR_HEIGHT = 2.dp
