package com.iktwo.piktographs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

public const val TABLE_HEADER_ELEMENT_TYPE: String = "tableHeader"
public const val TABLE_ROW_ELEMENT_TYPE: String = "tableRow"

private const val KEY_COLUMNS = "columns"
private const val KEY_VALUES = "values"
private const val KEY_DENSE = "dense"
private const val KEY_COLLAPSE_BELOW = "collapseBelow"

private const val DEFAULT_COLLAPSE_BELOW_DP = 600

/**
 * The column titles of a table whose rows are [TableRowUI] elements with the same `columns`.
 *
 * - `columns`: see [TableRowUI]. Only `label`, `width`, `weight` and `align` matter here.
 * - `collapseBelow`: the same value as the rows. Narrower than that the rows label their own values,
 *   and the header draws nothing.
 */
@Composable
public fun TableHeaderUI(element: ProcessedElement) {
    val columns = rememberColumns(element)
    if (columns.isEmpty()) return

    val dimensions = LocalTheme.current.dimensions
    val collapseBelow = element.collapseBelow()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < collapseBelow) return@BoxWithConstraints

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.padding, vertical = dimensions.verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
                verticalAlignment = Alignment.Bottom,
            ) {
                columns.forEach { column ->
                    Text(
                        text = column.label.orEmpty(),
                        modifier = cellWidth(column).semantics { heading() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = column.align,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * One row of a table. It is an element of its own, and not part of a `table` holding every row, so
 * that a page can expand it over a long array and still only compose the rows on screen.
 *
 * - `columns`: an array, one object per column:
 *   - `key`: which of the row's values the cell shows.
 *   - `label`: the column title, shown by [TableHeaderUI] and, in a collapsed row, before the value.
 *   - `width` (dp) or `weight` (default 1): a fixed column, or a share of what is left.
 *   - `align`: `start` (default), `center` or `end`.
 *   - `display`: `text` (default), `mono` (monospace: ids, timestamps, log lines), `badge` (a pill)
 *     or `icon` (the value is an icon name).
 *   - `secondaryKey`: a second value under the first, smaller.
 *   - `toneKey`: which of the row's values is this cell's tone. Without it a `badge` or `icon` cell
 *     takes the row's `tone`, and text is not colored.
 *   - `maxLines`: default 2.
 *   - `primary`: `true` on the column that titles a collapsed row (default: the first text column).
 * - The row's values are its own keys (`"processors": { "name": ... }`), or an object under
 *   `values`, which a `{ "type": "path" }` processor fills with a whole array entry at once.
 * - `tone`: `info`, `success`, `warning` or `error`. A warning or error row gets a faint fill.
 * - `dense`: `true` for less padding and smaller text.
 * - `collapseBelow`: in dp (default 600). In less width than this the row stacks: the primary cell
 *   as a title, the other values under it with their labels. `0` never collapses.
 *
 * With actions the whole row is clickable.
 */
@Composable
public fun TableRowUI(element: ProcessedElement) {
    val columns = rememberColumns(element)
    if (columns.isEmpty()) return

    val dimensions = LocalTheme.current.dimensions
    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current
    val tone = element.tone()
    val dense = element.booleanValue(KEY_DENSE) == true
    val collapseBelow = element.collapseBelow()
    val fill = if (tone.needsAttention) tone.containerColorOrNull(ROW_FILL_ALPHA) else null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fill != null) Modifier.background(fill) else Modifier)
            .then(
                if (element.actions.isNotEmpty()) {
                    Modifier.clickable(enabled = isEnabled) {
                        element.actions.forEach { actionPerformer.onAction(it) }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        val collapsed = maxWidth < collapseBelow
        val rowModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.padding,
                vertical = if (dense) dimensions.verticalSpacing else dimensions.padding,
            )

        Column(modifier = Modifier.fillMaxWidth()) {
            if (collapsed) {
                CollapsedRow(element, columns, tone, rowModifier)
            } else {
                Row(
                    modifier = rowModifier,
                    horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    columns.forEach { column ->
                        Box(
                            modifier = cellWidth(column),
                            contentAlignment = when (column.align) {
                                TextAlign.End -> Alignment.CenterEnd
                                TextAlign.Center -> Alignment.Center
                                else -> Alignment.CenterStart
                            },
                        ) {
                            Cell(element, column, tone, dense)
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA))
        }
    }
}

@Composable
private fun Cell(
    element: ProcessedElement,
    column: TableColumn,
    rowTone: Tone,
    dense: Boolean,
) {
    val value = element.cellValue(column.key)
    val secondary = column.secondaryKey?.let { element.cellValue(it) }?.takeIf { it.isNotBlank() }
    val typography = MaterialTheme.typography

    when (column.display) {
        CellDisplay.Badge -> {
            if (!value.isNullOrBlank()) {
                Pill(
                    text = value,
                    tone = element.cellTone(column, rowTone),
                    variant = PillVariant.Tonal,
                    style = typography.labelMedium,
                )
            }
        }

        CellDisplay.Icon -> {
            value?.let(::resolveImageVector)?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(CELL_ICON_SIZE),
                    tint = element.cellTone(column, rowTone).colorOrNull() ?: LocalContentColor.current,
                )
            }
        }

        CellDisplay.Text, CellDisplay.Mono -> {
            val family = if (column.display == CellDisplay.Mono) FontFamily.Monospace else null
            val toneColor = column.toneKey?.let { element.cellTone(column, rowTone).colorOrNull() }

            Column {
                Text(
                    text = value.orEmpty(),
                    style = if (dense) typography.bodySmall else typography.bodyMedium,
                    color = toneColor ?: MaterialTheme.colorScheme.onSurface,
                    fontFamily = family,
                    textAlign = column.align,
                    maxLines = column.maxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                secondary?.let {
                    Text(
                        text = it,
                        style = typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = family,
                        textAlign = column.align,
                        maxLines = column.maxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * The row in a width where its columns do not fit: icons, the primary cell and the badges on one
 * line, then every other value with its label, wrapping as needed.
 */
@Composable
private fun CollapsedRow(
    element: ProcessedElement,
    columns: List<TableColumn>,
    rowTone: Tone,
    modifier: Modifier,
) {
    val typography = MaterialTheme.typography
    val scheme = MaterialTheme.colorScheme
    val primary = columns.firstOrNull { it.primary } ?: columns.firstOrNull { it.display.isText }
    val icons = columns.filter { it.display == CellDisplay.Icon }
    val badges = columns.filter { it.display == CellDisplay.Badge }
    val details = columns.filter { it.display.isText && it !== primary }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(COLLAPSED_LINE_GAP)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icons.forEach { Cell(element, it, rowTone, dense = false) }

            Column(modifier = Modifier.weight(1f)) {
                primary?.let { column ->
                    val family = if (column.display == CellDisplay.Mono) FontFamily.Monospace else null
                    Text(
                        text = element.cellValue(column.key).orEmpty(),
                        style = typography.bodyMedium,
                        fontWeight = if (family == null) FontWeight.Medium else null,
                        fontFamily = family,
                        color = scheme.onSurface,
                        maxLines = COLLAPSED_PRIMARY_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    column.secondaryKey?.let { element.cellValue(it) }?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = typography.bodySmall,
                            fontFamily = family,
                            color = scheme.onSurfaceVariant,
                            maxLines = COLLAPSED_PRIMARY_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            badges.forEach { Cell(element, it, rowTone, dense = false) }
        }

        if (details.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(COLLAPSED_DETAIL_GAP),
                verticalArrangement = Arrangement.spacedBy(COLLAPSED_LINE_GAP),
            ) {
                details.forEach { column ->
                    val value = listOfNotNull(
                        element.cellValue(column.key)?.takeIf { it.isNotBlank() },
                        column.secondaryKey?.let { element.cellValue(it) }?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")

                    if (value.isNotEmpty()) {
                        LabelledValue(
                            label = column.label?.takeIf { it.isNotBlank() },
                            value = value,
                            style = typography.bodySmall,
                            valueColor = column.toneKey?.let { element.cellTone(column, rowTone).colorOrNull() } ?: scheme.onSurface,
                            family = if (column.display == CellDisplay.Mono) FontFamily.Monospace else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelledValue(
    label: String?,
    value: String,
    style: TextStyle,
    valueColor: Color,
    family: FontFamily?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(COLLAPSED_LABEL_GAP)) {
        label?.let {
            Text(text = it, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Text(
            text = value,
            style = style,
            color = valueColor,
            fontFamily = family,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal enum class CellDisplay {
    Text,
    Mono,
    Badge,
    Icon,
    ;

    val isText: Boolean
        get() = this == Text || this == Mono
}

internal class TableColumn(
    val key: String,
    val label: String?,
    val width: Dp?,
    val weight: Float,
    val align: TextAlign,
    val display: CellDisplay,
    val secondaryKey: String?,
    val toneKey: String?,
    val maxLines: Int,
    val primary: Boolean,
)

/** The usable entries of a `columns` value: objects with a `key`. Anything else is skipped. */
internal fun parseTableColumns(columns: JsonElement?): List<TableColumn> =
    (columns as? JsonArray)
        ?.mapNotNull { entry ->
            val column = entry as? JsonObject ?: return@mapNotNull null
            val key = column["key"]?.asStringOrNull()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

            TableColumn(
                key = key,
                label = column["label"]?.asStringOrNull(),
                width = (column["width"] as? JsonPrimitive)?.floatOrNull?.takeIf { it > 0f }?.dp,
                weight = (column["weight"] as? JsonPrimitive)?.floatOrNull?.takeIf { it > 0f } ?: 1f,
                align = parseTextAlign(column["align"]?.asStringOrNull()) ?: TextAlign.Start,
                display = when (column["display"]?.asStringOrNull()?.lowercase()?.trim()) {
                    "mono", "monospace", "code" -> CellDisplay.Mono
                    "badge", "pill" -> CellDisplay.Badge
                    "icon" -> CellDisplay.Icon
                    else -> CellDisplay.Text
                },
                secondaryKey = column["secondaryKey"]?.asStringOrNull(),
                toneKey = column["toneKey"]?.asStringOrNull(),
                maxLines = (column["maxLines"] as? JsonPrimitive)?.intOrNull?.takeIf { it > 0 } ?: DEFAULT_MAX_LINES,
                primary = (column["primary"] as? JsonPrimitive)?.booleanOrNull == true,
            )
        }.orEmpty()

@Composable
private fun rememberColumns(element: ProcessedElement): List<TableColumn> {
    val columns = element.jsonValues[KEY_COLUMNS]
    return remember(columns) { parseTableColumns(columns) }
}

/** A row's value for [key]: its own, or the one in its `values` object. Shown as written. */
internal fun ProcessedElement.cellValue(key: String): String? = displayValue(key) ?: ((jsonValues[KEY_VALUES] as? JsonObject)?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content

private fun ProcessedElement.cellTone(
    column: TableColumn,
    rowTone: Tone,
): Tone = column.toneKey?.let { parseTone(cellValue(it)) } ?: rowTone

private fun ProcessedElement.collapseBelow(): Dp = (intValue(KEY_COLLAPSE_BELOW)?.coerceAtLeast(0) ?: DEFAULT_COLLAPSE_BELOW_DP).dp

private fun RowScope.cellWidth(column: TableColumn): Modifier = if (column.width != null) Modifier.width(column.width) else Modifier.weight(column.weight)

private const val DEFAULT_MAX_LINES = 2
private const val COLLAPSED_PRIMARY_MAX_LINES = 4
private const val ROW_FILL_ALPHA = 0.08f
private const val DIVIDER_ALPHA = 0.6f

private val CELL_GAP = 12.dp
private val CELL_ICON_SIZE = 18.dp
private val COLLAPSED_LINE_GAP = 2.dp
private val COLLAPSED_DETAIL_GAP = 12.dp
private val COLLAPSED_LABEL_GAP = 4.dp
