package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asFloatOrNull
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val IMAGE_ELEMENT_TYPE: String = "image"

public const val KEY_URL: String = "url"
public const val KEY_SRC: String = "src"
public const val KEY_CONTENT_DESCRIPTION: String = "contentDescription"
public const val KEY_WIDTH: String = "width"
public const val KEY_HEIGHT: String = "height"
public const val KEY_ASPECT_RATIO: String = "aspectRatio"
public const val KEY_CONTENT_SCALE: String = "contentScale"
public const val KEY_RADIUS: String = "radius"
public const val KEY_CORNER_RADIUS: String = "cornerRadius"
public const val KEY_ALIGNMENT: String = "alignment"
public const val KEY_ALPHA: String = "alpha"

@Composable
public fun ImageUI(element: ProcessedElement) {
    val url = element.jsonValues[KEY_URL]?.asStringOrNull()
        ?: element.jsonValues[KEY_SRC]?.asStringOrNull()
        ?: element.text

    val contentDescription = element.jsonValues[KEY_CONTENT_DESCRIPTION]?.asStringOrNull()

    var modifier: Modifier = Modifier

    val widthValue = element.jsonValues[KEY_WIDTH]?.asIntOrNull()
    val widthString = element.jsonValues[KEY_WIDTH]?.asStringOrNull()
    if (widthValue != null) {
        modifier = modifier.width(widthValue.dp)
    } else if (widthString == "fill" || widthString == "fillMaxWidth" || widthString == "match_parent") {
        modifier = modifier.fillMaxWidth()
    }

    val heightValue = element.jsonValues[KEY_HEIGHT]?.asIntOrNull()
    val heightString = element.jsonValues[KEY_HEIGHT]?.asStringOrNull()
    if (heightValue != null) {
        modifier = modifier.height(heightValue.dp)
    } else if (heightString == "fill" || heightString == "fillMaxHeight" || heightString == "match_parent") {
        modifier = modifier.fillMaxHeight()
    }

    val aspectRatio = element.jsonValues[KEY_ASPECT_RATIO]?.asFloatOrNull()
        ?: parseAspectRatio(element.jsonValues[KEY_ASPECT_RATIO]?.asStringOrNull())
    if (aspectRatio != null && aspectRatio > 0f) {
        modifier = modifier.aspectRatio(aspectRatio)
    }

    val radius = element.jsonValues[KEY_CORNER_RADIUS]?.asIntOrNull()
        ?: element.jsonValues[KEY_RADIUS]?.asIntOrNull()
    if (radius != null && radius > 0) {
        modifier = modifier.clip(RoundedCornerShape(radius.dp))
    }

    val alpha = element.jsonValues[KEY_ALPHA]?.asFloatOrNull()
    if (alpha != null) {
        modifier = modifier.alpha(alpha.coerceIn(0f, 1f))
    }

    val contentScale = parseContentScale(element.jsonValues[KEY_CONTENT_SCALE]?.asStringOrNull())
    val alignment = parseAlignment(element.jsonValues[KEY_ALIGNMENT]?.asStringOrNull())

    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current
    if (element.actions.isNotEmpty()) {
        modifier = modifier.clickable(enabled = isEnabled) {
            element.actions.firstOrNull()?.let { actionPerformer.onAction(it) }
        }
    }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
    )
}

internal fun parseContentScale(scale: String?): ContentScale {
    return when (scale?.lowercase()?.trim()) {
        "crop" -> ContentScale.Crop
        "fillbounds", "fill_bounds", "stretch" -> ContentScale.FillBounds
        "fillwidth", "fill_width" -> ContentScale.FillWidth
        "fillheight", "fill_height" -> ContentScale.FillHeight
        "inside" -> ContentScale.Inside
        "none" -> ContentScale.None
        else -> ContentScale.Fit
    }
}

internal fun parseAlignment(align: String?): Alignment {
    return when (align?.lowercase()?.trim()) {
        "top", "topcenter", "top_center" -> Alignment.TopCenter
        "topstart", "top_start" -> Alignment.TopStart
        "topend", "top_end" -> Alignment.TopEnd
        "bottom", "bottomcenter", "bottom_center" -> Alignment.BottomCenter
        "bottomstart", "bottom_start" -> Alignment.BottomStart
        "bottomend", "bottom_end" -> Alignment.BottomEnd
        "centerstart", "center_start", "start" -> Alignment.CenterStart
        "centerend", "center_end", "end" -> Alignment.CenterEnd
        else -> Alignment.Center
    }
}

internal fun parseAspectRatio(ratio: String?): Float? {
    if (ratio == null) return null
    if (ratio.contains(":")) {
        val parts = ratio.split(":")
        if (parts.size == 2) {
            val w = parts[0].trim().toFloatOrNull() ?: return null
            val h = parts[1].trim().toFloatOrNull() ?: return null
            if (h > 0f) return w / h
        }
    } else if (ratio.contains("/")) {
        val parts = ratio.split("/")
        if (parts.size == 2) {
            val w = parts[0].trim().toFloatOrNull() ?: return null
            val h = parts[1].trim().toFloatOrNull() ?: return null
            if (h > 0f) return w / h
        }
    }
    return ratio.toFloatOrNull()
}
