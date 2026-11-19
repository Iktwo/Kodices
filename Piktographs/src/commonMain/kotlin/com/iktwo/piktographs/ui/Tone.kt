package com.iktwo.piktographs.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import com.iktwo.kodices.elements.ProcessedElement

internal const val KEY_TONE = "tone"

/**
 * What an element is saying, as opposed to what it shows: the `tone` key of `text`, `badge`,
 * `stat`, `listItem`, `chart` and `tableRow`. It only picks a color; the colors themselves come
 * from [Colors], so a host decides what "warning" looks like.
 */
internal enum class Tone {
    Neutral,
    Info,
    Success,
    Warning,
    Error,
    ;

    /** Whether a row in this tone should stand out from its neighbours. */
    val needsAttention: Boolean
        get() = this == Warning || this == Error
}

/**
 * `neutral`, `info`, `success`, `warning` or `error`, plus the names servers tend to use for the
 * same things (log levels, `ok`, `danger`...). Anything else, and no value, is [Tone.Neutral].
 */
internal fun parseTone(tone: String?): Tone =
    when (tone?.lowercase()?.trim()) {
        "info", "information", "notice" -> Tone.Info
        "success", "ok", "good", "positive" -> Tone.Success
        "warning", "warn", "caution" -> Tone.Warning
        "error", "err", "danger", "critical", "fatal", "severe", "negative" -> Tone.Error
        else -> Tone.Neutral
    }

internal fun ProcessedElement.tone(key: String = KEY_TONE): Tone = parseTone(displayValue(key))

/** The color for text and icons in this tone; null for [Tone.Neutral], which follows the theme. */
@Composable
@ReadOnlyComposable
internal fun Tone.colorOrNull(): Color? {
    val colors = LocalTheme.current.colors
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < DARK_SURFACE_LUMINANCE

    return when (this) {
        Tone.Neutral -> null
        Tone.Info -> colors.infoColor.takeIf { it.isSpecified } ?: scheme.primary
        Tone.Success -> colors.successColor.takeIf { it.isSpecified } ?: if (dark) SUCCESS_ON_DARK else SUCCESS_ON_LIGHT
        Tone.Warning -> colors.warningColor.takeIf { it.isSpecified } ?: if (dark) WARNING_ON_DARK else WARNING_ON_LIGHT
        Tone.Error -> colors.errorColor.takeIf { it.isSpecified } ?: scheme.error
    }
}

/** A faint fill in this tone, opaque so that it reads the same on any background. */
@Composable
@ReadOnlyComposable
internal fun Tone.containerColorOrNull(alpha: Float = CONTAINER_ALPHA): Color? = colorOrNull()?.copy(alpha = alpha)?.compositeOver(MaterialTheme.colorScheme.surface)

/** Black or white, whichever reads on [background]. */
internal fun contentColorOn(background: Color): Color = if (background.luminance() > LIGHT_BACKGROUND_LUMINANCE) Color.Black else Color.White

private const val DARK_SURFACE_LUMINANCE = 0.5f
private const val LIGHT_BACKGROUND_LUMINANCE = 0.4f
private const val CONTAINER_ALPHA = 0.12f

// Dark enough to be read as text on white, light enough on black: 4.5:1 or better either way.
private val SUCCESS_ON_LIGHT = Color(0xFF1B7F3B)
private val SUCCESS_ON_DARK = Color(0xFF7BD88F)
private val WARNING_ON_LIGHT = Color(0xFF9A5B00)
private val WARNING_ON_DARK = Color(0xFFFFB95C)
