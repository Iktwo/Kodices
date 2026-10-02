package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.kodices.utils.asDoubleOrNull
import com.iktwo.kodices.utils.asFloatOrNull
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val CONTAINER_ELEMENT_TYPE: String = "container"
public const val HROW_ELEMENT_TYPE: String = "hrow"
public const val HORIZONTAL_LAYOUT_ELEMENT_TYPE: String = "horizontal_layout"
public const val VROW_ELEMENT_TYPE: String = "vrow"
public const val VERTICAL_LAYOUT_ELEMENT_TYPE: String = "vertical_layout"

private const val ORIENTATION_KEY = "orientation"
private const val DIRECTION_KEY = "direction"
private const val FILL_MAX_WIDTH_KEY = "fillMaxWidth"
private const val FILL_MAX_HEIGHT_KEY = "fillMaxHeight"
private const val PADDING_KEY = "padding"
private const val PADDING_HORIZONTAL_KEY = "paddingHorizontal"
private const val HORIZONTAL_PADDING_KEY = "horizontalPadding"
private const val PADDING_VERTICAL_KEY = "paddingVertical"
private const val VERTICAL_PADDING_KEY = "verticalPadding"
private const val SPACING_KEY = "spacing"
private const val ARRANGEMENT_KEY = "arrangement"
private const val HORIZONTAL_ARRANGEMENT_KEY = "horizontalArrangement"
private const val VERTICAL_ARRANGEMENT_KEY = "verticalArrangement"
private const val ALIGNMENT_KEY = "alignment"
private const val HORIZONTAL_ALIGNMENT_KEY = "horizontalAlignment"
private const val VERTICAL_ALIGNMENT_KEY = "verticalAlignment"
private const val WEIGHT_KEY = "weight"
private const val ALIGN_KEY = "align"

@Composable
public fun ContainerUI(
    element: ProcessedElement,
    modifier: Modifier = Modifier,
) {
    val rawOrientation = element.jsonValues[ORIENTATION_KEY]?.asStringOrNull()
        ?: element.jsonValues[DIRECTION_KEY]?.asStringOrNull()

    val isVertical = when {
        element.type == VROW_ELEMENT_TYPE || element.type == VERTICAL_LAYOUT_ELEMENT_TYPE -> true
        element.type == HROW_ELEMENT_TYPE || element.type == HORIZONTAL_LAYOUT_ELEMENT_TYPE -> false
        rawOrientation != null -> {
            val normalized = rawOrientation.lowercase()
            normalized == "vertical" || normalized == "column" || normalized == "vrow"
        }
        else -> false
    }

    val fillMaxWidth = element.jsonValues[FILL_MAX_WIDTH_KEY]?.asBooleanOrNull() ?: true
    val fillMaxHeight = element.jsonValues[FILL_MAX_HEIGHT_KEY]?.asBooleanOrNull() ?: false

    val paddingVal = element.jsonValues[PADDING_KEY]?.asIntOrNull()?.dp
    val hPadding = element.jsonValues[HORIZONTAL_PADDING_KEY]?.asIntOrNull()?.dp
        ?: element.jsonValues[PADDING_HORIZONTAL_KEY]?.asIntOrNull()?.dp
        ?: paddingVal
    val vPadding = element.jsonValues[VERTICAL_PADDING_KEY]?.asIntOrNull()?.dp
        ?: element.jsonValues[PADDING_VERTICAL_KEY]?.asIntOrNull()?.dp
        ?: paddingVal

    val defaultSpacing = if (isVertical) {
        DefaultTheme.current.dimensions.verticalSpacing
    } else {
        DefaultTheme.current.dimensions.horizontalSpacing
    }
    val spacing = element.jsonValues[SPACING_KEY]?.asIntOrNull()?.dp ?: defaultSpacing

    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current && element.enabled

    var containerModifier = modifier
    if (fillMaxWidth) {
        containerModifier = containerModifier.fillMaxWidth()
    }
    if (fillMaxHeight) {
        containerModifier = containerModifier.fillMaxHeight()
    }
    if (hPadding != null || vPadding != null) {
        containerModifier = containerModifier.padding(
            horizontal = hPadding ?: 0.dp,
            vertical = vPadding ?: 0.dp,
        )
    }
    if (element.actions.isNotEmpty()) {
        containerModifier = containerModifier.clickable(enabled = isEnabled) {
            element.actions.forEach { action ->
                actionPerformer.onAction(action)
            }
        }
    }

    if (isVertical) {
        val rawArrangement = element.jsonValues[VERTICAL_ARRANGEMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ARRANGEMENT_KEY]?.asStringOrNull()
        val verticalArrangement = resolveVerticalArrangement(rawArrangement, spacing)

        val rawAlignment = element.jsonValues[HORIZONTAL_ALIGNMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()
        val horizontalAlignment = resolveHorizontalAlignment(rawAlignment)

        Column(
            modifier = containerModifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            for (child in element.nestedElements) {
                val weight = child.jsonValues[WEIGHT_KEY]?.asFloatOrNull()
                    ?: child.jsonValues[WEIGHT_KEY]?.asDoubleOrNull()?.toFloat()
                    ?: child.jsonValues[WEIGHT_KEY]?.asStringOrNull()?.toFloatOrNull()
                val childAlign = child.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()
                    ?: child.jsonValues[ALIGN_KEY]?.asStringOrNull()

                var childModifier: Modifier = Modifier
                if (weight != null && weight > 0f) {
                    childModifier = childModifier.weight(weight)
                }
                if (childAlign != null) {
                    val childHorizontal = resolveHorizontalAlignment(childAlign)
                    childModifier = childModifier.align(childHorizontal)
                }
                NestedElementUI(child, childModifier)
            }
        }
    } else {
        val rawArrangement = element.jsonValues[HORIZONTAL_ARRANGEMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ARRANGEMENT_KEY]?.asStringOrNull()
        val horizontalArrangement = resolveHorizontalArrangement(rawArrangement, spacing)

        val rawAlignment = element.jsonValues[VERTICAL_ALIGNMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()
        val verticalAlignment = resolveVerticalAlignment(rawAlignment)

        Row(
            modifier = containerModifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
        ) {
            for (child in element.nestedElements) {
                val weight = child.jsonValues[WEIGHT_KEY]?.asFloatOrNull()
                    ?: child.jsonValues[WEIGHT_KEY]?.asDoubleOrNull()?.toFloat()
                    ?: child.jsonValues[WEIGHT_KEY]?.asStringOrNull()?.toFloatOrNull()
                val childAlign = child.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()
                    ?: child.jsonValues[ALIGN_KEY]?.asStringOrNull()

                var childModifier: Modifier = Modifier
                if (weight != null && weight > 0f) {
                    childModifier = childModifier.weight(weight)
                }
                if (childAlign != null) {
                    val childVertical = resolveVerticalAlignment(childAlign)
                    childModifier = childModifier.align(childVertical)
                }
                NestedElementUI(child, childModifier)
            }
        }
    }
}

private fun resolveHorizontalArrangement(
    raw: String?,
    spacing: androidx.compose.ui.unit.Dp,
): Arrangement.Horizontal {
    if (raw == null) return Arrangement.spacedBy(spacing)
    return when (raw.lowercase().replace("-", "_").trim()) {
        "start", "left" -> Arrangement.spacedBy(spacing, Alignment.Start)
        "end", "right" -> Arrangement.spacedBy(spacing, Alignment.End)
        "center" -> Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
        "space_between", "spacebetween" -> Arrangement.SpaceBetween
        "space_around", "spacearound" -> Arrangement.SpaceAround
        "space_evenly", "spaceevenly" -> Arrangement.SpaceEvenly
        else -> Arrangement.spacedBy(spacing)
    }
}

private fun resolveVerticalArrangement(
    raw: String?,
    spacing: androidx.compose.ui.unit.Dp,
): Arrangement.Vertical {
    if (raw == null) return Arrangement.spacedBy(spacing)
    return when (raw.lowercase().replace("-", "_").trim()) {
        "top" -> Arrangement.spacedBy(spacing, Alignment.Top)
        "bottom" -> Arrangement.spacedBy(spacing, Alignment.Bottom)
        "center" -> Arrangement.spacedBy(spacing, Alignment.CenterVertically)
        "space_between", "spacebetween" -> Arrangement.SpaceBetween
        "space_around", "spacearound" -> Arrangement.SpaceAround
        "space_evenly", "spaceevenly" -> Arrangement.SpaceEvenly
        else -> Arrangement.spacedBy(spacing)
    }
}

private fun resolveVerticalAlignment(raw: String?): Alignment.Vertical {
    if (raw == null) return Alignment.CenterVertically
    return when (raw.lowercase().replace("-", "_").trim()) {
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        "center", "center_vertically", "centervertically" -> Alignment.CenterVertically
        else -> Alignment.CenterVertically
    }
}

private fun resolveHorizontalAlignment(raw: String?): Alignment.Horizontal {
    if (raw == null) return Alignment.Start
    return when (raw.lowercase().replace("-", "_").trim()) {
        "start", "left" -> Alignment.Start
        "end", "right" -> Alignment.End
        "center", "center_horizontally", "centerhorizontally" -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }
}
