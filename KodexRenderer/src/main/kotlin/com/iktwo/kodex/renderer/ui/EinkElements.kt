package com.iktwo.kodex.renderer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.ui.CARD_ELEMENT_TYPE
import com.iktwo.piktographs.ui.LocalTheme
import com.iktwo.piktographs.ui.NestedElementUI
import com.iktwo.piktographs.ui.PROGRESS_ELEMENT_TYPE
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/*
 * E-ink replacements for Piktographs elements whose Material rendering does not survive the panel:
 * an elevated card's shadow becomes a gray smear, and a progress indicator's track is a tonal
 * container color, which the e-ink scheme collapses to white (invisible) and which ends in a stop
 * dot that reads as noise. Both are redrawn here with solid black strokes and fills of at least 2 dp.
 * They read the same JSON keys as Piktographs' CardUI and ProgressUI.
 */

private const val KEY_VARIANT = "variant"
private const val KEY_PROGRESS = "progress"

private val STROKE = 2.dp
private val CARD_SHAPE = RoundedCornerShape(12.dp)
private val BAR_HEIGHT = 20.dp
private val BAR_INSET = 4.dp
private val RING_SIZE = 48.dp
private val RING_WIDTH = 8.dp

/** Element overrides for `PageUI`: renders cards and progress for e-ink, everything else as usual. */
@Composable
internal fun einkElementOverride(element: ProcessedElement): Boolean =
    when (element.type) {
        CARD_ELEMENT_TYPE -> {
            EinkCard(element)
            true
        }

        PROGRESS_ELEMENT_TYPE -> {
            EinkProgress(element)
            true
        }

        else -> false
    }

/** Every card variant as an outlined card: no shadow (elevated) and no tonal fill (filled). */
@Composable
internal fun EinkCard(element: ProcessedElement) {
    val padding = LocalTheme.current.dimensions.padding

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .border(STROKE, Color.Black, CARD_SHAPE)
            .padding(padding),
    ) {
        element.text?.let { title ->
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        }

        element.textSecondary?.let { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(top = LocalTheme.current.dimensions.verticalSpacing),
            )
        }

        element.nestedElements.forEach { child ->
            NestedElementUI(child)
        }
    }
}

/**
 * A progress indicator as an outlined track with a solid fill. Without a `progress` value
 * (indeterminate) only the empty track is drawn: an animation has no meaning in a still image.
 */
@Composable
internal fun EinkProgress(element: ProcessedElement) {
    val variant = element.jsonValues[KEY_VARIANT]?.asStringOrNull()?.lowercase() ?: "linear"
    val primitive = element.jsonValues[KEY_PROGRESS] as? JsonPrimitive
    val progress = (primitive?.doubleOrNull?.toFloat() ?: primitive?.content?.toFloatOrNull())?.coerceIn(0f, 1f)
    val dimensions = LocalTheme.current.dimensions

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(dimensions.verticalSpacing),
    ) {
        element.text?.let { title ->
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }

        if (variant == "circular") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.horizontalSpacing),
            ) {
                EinkProgressRing(progress)
                element.textSecondary?.let { subtitle ->
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }
            }
        } else {
            EinkProgressBar(progress)
            element.textSecondary?.let { subtitle ->
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Black)
            }
        }
    }
}

@Composable
private fun EinkProgressBar(progress: Float?) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT)
            .border(STROKE, Color.Black, shape)
            .padding(BAR_INSET),
    ) {
        if (progress != null && progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Color.Black, shape),
            )
        }
    }
}

@Composable
private fun EinkProgressRing(progress: Float?) {
    Canvas(modifier = Modifier.size(RING_SIZE)) {
        val ring = RING_WIDTH.toPx()
        val inset = ring / 2

        drawCircle(
            color = Color.Black,
            radius = size.minDimension / 2 - STROKE.toPx() / 2,
            style = Stroke(width = STROKE.toPx()),
        )

        if (progress != null && progress > 0f) {
            drawArc(
                color = Color.Black,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - ring, size.height - ring),
                style = Stroke(width = ring, cap = StrokeCap.Butt),
            )
        }
    }
}
