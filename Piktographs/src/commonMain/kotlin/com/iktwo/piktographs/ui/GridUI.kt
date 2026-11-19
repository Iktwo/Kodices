package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement

public const val GRID_ELEMENT_TYPE: String = "grid"

private const val KEY_COLUMNS = "columns"
private const val KEY_MIN_COLUMN_WIDTH = "minColumnWidth"
private const val KEY_SPACING = "spacing"
private const val KEY_PADDING = "padding"

private const val DEFAULT_COLUMNS = 2
private const val MAX_COLUMNS = 12

/**
 * Lays its nested elements out in equal-width columns, filling row by row.
 *
 * - `columns`: how many per row (default 2, at most 12).
 * - `minColumnWidth`: in dp. With it the grid fits as many columns as are at least this wide, so
 *   the same screen is one column on a phone and several in a desktop window; `columns` is then the
 *   most it will use.
 * - `spacing`: gap between cells, in dp.
 * - `padding`: padding around the grid, in dp.
 *
 * A last row with fewer elements keeps the column width instead of stretching them. It is not lazy:
 * it is meant for the handful of tiles on a dashboard, not for a long list.
 */
@Composable
public fun GridUI(element: ProcessedElement) {
    val dimensions = LocalTheme.current.dimensions
    val declaredColumns = element.intValue(KEY_COLUMNS)?.coerceIn(1, MAX_COLUMNS)
    val minColumnWidth = element.intValue(KEY_MIN_COLUMN_WIDTH)?.takeIf { it > 0 }?.dp
    val spacing = element.intValue(KEY_SPACING)?.coerceAtLeast(0)?.dp ?: dimensions.horizontalSpacing
    val padding = element.intValue(KEY_PADDING)?.coerceAtLeast(0)?.dp ?: 0.dp

    if (minColumnWidth == null) {
        GridRows(element, declaredColumns ?: DEFAULT_COLUMNS, spacing, Modifier.fillMaxWidth().padding(padding))
    } else {
        // Measured here and not from the window: a grid inside a card or a column is narrower.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(padding)) {
            GridRows(
                element = element,
                columns = adaptiveColumns(maxWidth, minColumnWidth, spacing, declaredColumns ?: MAX_COLUMNS),
                spacing = spacing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** How many columns at least [minColumnWidth] wide fit in [available], from 1 to [maxColumns]. */
internal fun adaptiveColumns(
    available: Dp,
    minColumnWidth: Dp,
    spacing: Dp,
    maxColumns: Int,
): Int {
    // n columns need n * min + (n - 1) * spacing.
    val fitting = ((available + spacing) / (minColumnWidth + spacing)).toInt()
    return fitting.coerceIn(1, maxColumns.coerceAtLeast(1))
}

@Composable
private fun GridRows(
    element: ProcessedElement,
    columns: Int,
    spacing: Dp,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        element.nestedElements.chunked(columns).forEach { rowElements ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                rowElements.forEach { child ->
                    NestedElementUI(child, Modifier.weight(1f))
                }

                repeat(columns - rowElements.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
