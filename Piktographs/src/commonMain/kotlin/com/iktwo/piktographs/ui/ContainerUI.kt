package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.iktwo.piktographs.LocalLogger

/** A row or a column of elements: `{"type": "container", "orientation": "vertical"}`. */
public const val CONTAINER_ELEMENT_TYPE: String = "container"

private const val TYPE_ALIAS_MESSAGE = "Use CONTAINER_ELEMENT_TYPE with an \"orientation\"; this type name is removed in 1.0."

@Deprecated(TYPE_ALIAS_MESSAGE, ReplaceWith("CONTAINER_ELEMENT_TYPE"))
public const val HROW_ELEMENT_TYPE: String = "hrow"

@Deprecated(TYPE_ALIAS_MESSAGE, ReplaceWith("CONTAINER_ELEMENT_TYPE"))
public const val HORIZONTAL_LAYOUT_ELEMENT_TYPE: String = "horizontal_layout"

@Deprecated(TYPE_ALIAS_MESSAGE, ReplaceWith("CONTAINER_ELEMENT_TYPE"))
public const val VROW_ELEMENT_TYPE: String = "vrow"

@Deprecated(TYPE_ALIAS_MESSAGE, ReplaceWith("CONTAINER_ELEMENT_TYPE"))
public const val VERTICAL_LAYOUT_ELEMENT_TYPE: String = "vertical_layout"

private const val ORIENTATION_KEY = "orientation"
private const val ORIENTATION_VERTICAL = "vertical"
private const val ORIENTATION_HORIZONTAL = "horizontal"
private const val FILL_MAX_WIDTH_KEY = "fillMaxWidth"
private const val FILL_MAX_HEIGHT_KEY = "fillMaxHeight"
private const val PADDING_KEY = "padding"
private const val PADDING_HORIZONTAL_KEY = "paddingHorizontal"
private const val PADDING_VERTICAL_KEY = "paddingVertical"
private const val SPACING_KEY = "spacing"
private const val ARRANGEMENT_KEY = "arrangement"
private const val HORIZONTAL_ARRANGEMENT_KEY = "horizontalArrangement"
private const val VERTICAL_ARRANGEMENT_KEY = "verticalArrangement"
private const val ALIGNMENT_KEY = "alignment"
private const val HORIZONTAL_ALIGNMENT_KEY = "horizontalAlignment"
private const val VERTICAL_ALIGNMENT_KEY = "verticalAlignment"
private const val WEIGHT_KEY = "weight"
private const val ALIGN_KEY = "align"

/*
 * Names that still work but are not the documented spelling: each concept has one name, and these
 * are the others. Using one is reported through LocalLogger (see containerDeprecations); they are
 * removed in 1.0.
 */

/** Deprecated type names, and the orientation each one stands for. */
internal val CONTAINER_TYPE_ALIASES: Map<String, String> = mapOf(
    "hrow" to ORIENTATION_HORIZONTAL,
    "horizontal_layout" to ORIENTATION_HORIZONTAL,
    "vrow" to ORIENTATION_VERTICAL,
    "vertical_layout" to ORIENTATION_VERTICAL,
)

/** Deprecated keys, and the key to use instead. */
private val KEY_ALIASES = mapOf(
    "direction" to ORIENTATION_KEY,
    "horizontalPadding" to PADDING_HORIZONTAL_KEY,
    "verticalPadding" to PADDING_VERTICAL_KEY,
)

/** Deprecated values of "orientation", and the value to use instead. */
private val ORIENTATION_VALUE_ALIASES = mapOf(
    "column" to ORIENTATION_VERTICAL,
    "vrow" to ORIENTATION_VERTICAL,
    "row" to ORIENTATION_HORIZONTAL,
    "hrow" to ORIENTATION_HORIZONTAL,
)

/** Deprecated values of the arrangement and alignment keys, and the value to use instead. */
private val POSITION_VALUE_ALIASES = mapOf(
    "left" to "start",
    "right" to "end",
    "spacebetween" to "space_between",
    "spacearound" to "space_around",
    "spaceevenly" to "space_evenly",
    "centervertically" to "center",
    "center_vertically" to "center",
    "centerhorizontally" to "center",
    "center_horizontally" to "center",
)

private val POSITION_KEYS = listOf(
    ARRANGEMENT_KEY,
    HORIZONTAL_ARRANGEMENT_KEY,
    VERTICAL_ARRANGEMENT_KEY,
    ALIGNMENT_KEY,
    HORIZONTAL_ALIGNMENT_KEY,
    VERTICAL_ALIGNMENT_KEY,
)

private fun normalized(raw: String): String = raw.lowercase().replace("-", "_").trim()

/** Reads [key], or the deprecated name it replaced. */
private fun ProcessedElement.value(key: String) = jsonValues[key] ?: KEY_ALIASES.entries.firstOrNull { it.value == key }?.let { jsonValues[it.key] }

/**
 * One message for each deprecated name [element] uses, saying what to write instead. Empty for a
 * container written the documented way.
 */
internal fun containerDeprecations(element: ProcessedElement): List<String> {
    val where = "Element \"${element.id}\""
    val messages = mutableListOf<String>()

    CONTAINER_TYPE_ALIASES[element.type]?.let { orientation ->
        val replacement = if (orientation == ORIENTATION_VERTICAL) {
            "\"type\": \"$CONTAINER_ELEMENT_TYPE\" with \"$ORIENTATION_KEY\": \"$ORIENTATION_VERTICAL\""
        } else {
            "\"type\": \"$CONTAINER_ELEMENT_TYPE\""
        }
        messages += "$where: type \"${element.type}\" is deprecated, use $replacement."
    }

    KEY_ALIASES.forEach { (old, new) ->
        if (element.jsonValues[old] != null) messages += "$where: key \"$old\" is deprecated, use \"$new\"."
    }

    element.value(ORIENTATION_KEY)?.asStringOrNull()?.let { raw ->
        ORIENTATION_VALUE_ALIASES[normalized(raw)]?.let { new ->
            messages += "$where: \"$ORIENTATION_KEY\": \"$raw\" is deprecated, use \"$new\"."
        }
    }

    POSITION_KEYS.forEach { key ->
        element.jsonValues[key]?.asStringOrNull()?.let { raw ->
            POSITION_VALUE_ALIASES[normalized(raw)]?.let { new ->
                messages += "$where: \"$key\": \"$raw\" is deprecated, use \"$new\"."
            }
        }
    }

    return messages
}

@Composable
public fun ContainerUI(
    element: ProcessedElement,
    modifier: Modifier = Modifier,
) {
    val logger = LocalLogger.current
    // An effect, so a name is reported when the element appears and not on every recomposition.
    LaunchedEffect(element, logger) {
        containerDeprecations(element).forEach(logger::warn)
    }

    // A deprecated type name says which way it runs; a container says so with "orientation" and
    // is a row without one.
    val orientation = CONTAINER_TYPE_ALIASES[element.type]
        ?: element.value(ORIENTATION_KEY)?.asStringOrNull()?.let { normalized(it) }
    val isVertical = (ORIENTATION_VALUE_ALIASES[orientation] ?: orientation) == ORIENTATION_VERTICAL

    val fillMaxWidth = element.jsonValues[FILL_MAX_WIDTH_KEY]?.asBooleanOrNull() ?: true
    val fillMaxHeight = element.jsonValues[FILL_MAX_HEIGHT_KEY]?.asBooleanOrNull() ?: false

    val paddingVal = element.jsonValues[PADDING_KEY]?.asIntOrNull()?.dp
    val hPadding = element.value(PADDING_HORIZONTAL_KEY)?.asIntOrNull()?.dp ?: paddingVal
    val vPadding = element.value(PADDING_VERTICAL_KEY)?.asIntOrNull()?.dp ?: paddingVal

    val defaultSpacing = if (isVertical) {
        LocalTheme.current.dimensions.verticalSpacing
    } else {
        LocalTheme.current.dimensions.horizontalSpacing
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
        val rawAlignment = element.jsonValues[HORIZONTAL_ALIGNMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()

        Column(
            modifier = containerModifier,
            verticalArrangement = resolveVerticalArrangement(rawArrangement, spacing),
            horizontalAlignment = resolveHorizontalAlignment(rawAlignment),
        ) {
            for (child in element.nestedElements) {
                var childModifier: Modifier = Modifier
                childWeight(child)?.let { childModifier = childModifier.weight(it) }
                childAlignment(child)?.let { childModifier = childModifier.align(resolveHorizontalAlignment(it)) }
                NestedElementUI(child, childModifier)
            }
        }
    } else {
        val rawArrangement = element.jsonValues[HORIZONTAL_ARRANGEMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ARRANGEMENT_KEY]?.asStringOrNull()
        val rawAlignment = element.jsonValues[VERTICAL_ALIGNMENT_KEY]?.asStringOrNull()
            ?: element.jsonValues[ALIGNMENT_KEY]?.asStringOrNull()

        Row(
            modifier = containerModifier,
            horizontalArrangement = resolveHorizontalArrangement(rawArrangement, spacing),
            verticalAlignment = resolveVerticalAlignment(rawAlignment),
        ) {
            for (child in element.nestedElements) {
                var childModifier: Modifier = Modifier
                childWeight(child)?.let { childModifier = childModifier.weight(it) }
                childAlignment(child)?.let { childModifier = childModifier.align(resolveVerticalAlignment(it)) }
                NestedElementUI(child, childModifier)
            }
        }
    }
}

/** The share of the leftover space a child asked for, if any. */
private fun childWeight(child: ProcessedElement): Float? {
    val weight = child.jsonValues[WEIGHT_KEY]?.asFloatOrNull()
        ?: child.jsonValues[WEIGHT_KEY]?.asDoubleOrNull()?.toFloat()
        ?: child.jsonValues[WEIGHT_KEY]?.asStringOrNull()?.toFloatOrNull()
    return weight?.takeIf { it > 0f }
}

/** Where a child asked to sit across its container, if it did. */
private fun childAlignment(child: ProcessedElement): String? = child.jsonValues[ALIGNMENT_KEY]?.asStringOrNull() ?: child.jsonValues[ALIGN_KEY]?.asStringOrNull()

private fun resolveHorizontalArrangement(
    raw: String?,
    spacing: androidx.compose.ui.unit.Dp,
): Arrangement.Horizontal {
    if (raw == null) return Arrangement.spacedBy(spacing)
    return when (normalized(raw)) {
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
    return when (normalized(raw)) {
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
    return when (normalized(raw)) {
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        "center", "center_vertically", "centervertically" -> Alignment.CenterVertically
        else -> Alignment.CenterVertically
    }
}

private fun resolveHorizontalAlignment(raw: String?): Alignment.Horizontal {
    if (raw == null) return Alignment.Start
    return when (normalized(raw)) {
        "start", "left" -> Alignment.Start
        "end", "right" -> Alignment.End
        "center", "center_horizontally", "centerhorizontally" -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }
}
