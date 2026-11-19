package com.iktwo.piktographs.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/*
 * How the elements read their values out of ProcessedElement.jsonValues. Servers are not consistent
 * about quoting ("21.5" and 21.5 both show up, especially out of data processors), so everything
 * that is shown as text or used as a number accepts either form.
 */

internal fun ProcessedElement.stringValue(key: String): String? = jsonValues[key]?.asStringOrNull()

internal fun ProcessedElement.intValue(key: String): Int? = jsonValues[key]?.asIntOrNull() ?: stringValue(key)?.trim()?.toIntOrNull()

/** A number, written either as a JSON number or as a numeric string. */
internal fun ProcessedElement.numberValue(key: String): Float? = (jsonValues[key] as? JsonPrimitive)?.toFloatOrNull()

/** A value meant to be shown: a string as it is, a number or boolean as written in the JSON. */
internal fun ProcessedElement.displayValue(key: String): String? = (jsonValues[key] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content

/** `true`/`false`, as a JSON boolean or as a string. */
internal fun ProcessedElement.booleanValue(key: String): Boolean? = (jsonValues[key] as? JsonPrimitive)?.content?.trim()?.lowercase()?.toBooleanStrictOrNull()

/** `monospace` (or `mono`), `serif`, `sans`; anything else is the theme's font. */
internal fun parseFontFamily(name: String?): FontFamily? =
    when (name?.lowercase()?.replace("-", "")?.replace("_", "")?.trim()) {
        "monospace", "mono", "code" -> FontFamily.Monospace
        "serif" -> FontFamily.Serif
        "sans", "sansserif" -> FontFamily.SansSerif
        else -> null
    }

/** The finite numbers of an array value; anything else in the array is skipped. */
internal fun ProcessedElement.numberListValue(key: String): List<Float> =
    (jsonValues[key] as? JsonArray)
        ?.mapNotNull { (it as? JsonPrimitive)?.toFloatOrNull() }
        .orEmpty()

internal fun ProcessedElement.stringListValue(key: String): List<String> =
    (jsonValues[key] as? JsonArray)
        ?.mapNotNull { (it as? JsonPrimitive)?.content }
        .orEmpty()

private fun JsonPrimitive.toFloatOrNull(): Float? {
    val value = doubleOrNull ?: content.trim().toDoubleOrNull()
    return value?.toFloat()?.takeIf { it.isFinite() }
}

/** `#RRGGBB` or `#AARRGGBB`; anything else is null. */
internal fun parseColor(colorString: String?): Color? {
    val hex = colorString?.trim()?.takeIf { it.startsWith("#") }?.removePrefix("#") ?: return null
    val value = hex.toLongOrNull(16) ?: return null
    return when (hex.length) {
        6 -> Color(value or 0xFF000000)
        8 -> Color(value)
        else -> null
    }
}

/**
 * A Material type scale style by name: `headlineMedium`, `title_large`, `body-small`... The size can
 * be left out (`headline`, `title`, `body`, `label`, `display`), which means medium.
 */
internal fun Typography.styleNamed(name: String?): TextStyle? =
    when (name?.lowercase()?.replace("-", "")?.replace("_", "")?.trim()) {
        "displaylarge" -> displayLarge
        "display", "displaymedium" -> displayMedium
        "displaysmall" -> displaySmall
        "headlinelarge" -> headlineLarge
        "headline", "headlinemedium" -> headlineMedium
        "headlinesmall" -> headlineSmall
        "titlelarge" -> titleLarge
        "title", "titlemedium" -> titleMedium
        "titlesmall" -> titleSmall
        "bodylarge" -> bodyLarge
        "body", "bodymedium" -> bodyMedium
        "bodysmall", "caption" -> bodySmall
        "labellarge" -> labelLarge
        "label", "labelmedium" -> labelMedium
        "labelsmall" -> labelSmall
        else -> null
    }

/** `normal`, `medium`, `semibold`, `bold`, or a weight from 1 to 1000. */
internal fun parseFontWeight(weight: String?): FontWeight? =
    when (val normalized = weight?.lowercase()?.trim()) {
        null -> null
        "light" -> FontWeight.Light
        "normal", "regular" -> FontWeight.Normal
        "medium" -> FontWeight.Medium
        "semibold", "semi_bold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        else -> normalized.toIntOrNull()?.takeIf { it in 1..1000 }?.let { FontWeight(it) }
    }

internal fun parseTextAlign(align: String?): TextAlign? =
    when (align?.lowercase()?.trim()) {
        "start", "left" -> TextAlign.Start
        "center" -> TextAlign.Center
        "end", "right" -> TextAlign.End
        else -> null
    }
