package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val ICON_ELEMENT_TYPE: String = "icon"

private const val NAME_KEY = "name"
private const val ICON_KEY = "icon"
private const val SIZE_KEY = "size"
private const val TINT_KEY = "tint"

@Composable
public fun IconUI(element: ProcessedElement) {
    val rawName = element.jsonValues[NAME_KEY]?.asStringOrNull()
        ?: element.jsonValues[ICON_KEY]?.asStringOrNull()
        ?: element.text
        ?: ""

    val imageVector = resolveImageVector(rawName) ?: Icons.Default.Info

    val size = element.jsonValues[SIZE_KEY]?.asIntOrNull()?.dp ?: 24.dp
    val tintString = element.jsonValues[TINT_KEY]?.asStringOrNull() ?: element.style
    val tint = parseColor(tintString) ?: LocalContentColor.current

    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current
    val hasAction = element.actions.isNotEmpty()

    val iconComposable = @Composable {
        Icon(
            imageVector = imageVector,
            contentDescription = element.text ?: rawName,
            modifier = Modifier.size(size),
            tint = tint,
        )
    }

    if (hasAction) {
        Box(
            modifier = Modifier
                .clickable(enabled = isEnabled) {
                    element.actions.firstOrNull()?.let { actionPerformer.onAction(it) }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            iconComposable()
        }
    } else {
        iconComposable()
    }
}

internal fun resolveImageVector(name: String): ImageVector? {
    val normalized = name.lowercase().replace("-", "_").trim()
    return when (normalized) {
        "add", "plus" -> Icons.Default.Add
        "check", "done" -> Icons.Default.Check
        "close", "clear", "cancel" -> Icons.Default.Close
        "arrow_back", "arrowback", "back" -> Icons.AutoMirrored.Filled.ArrowBack
        "arrow_forward", "arrowforward", "forward" -> Icons.AutoMirrored.Filled.ArrowForward
        "search" -> Icons.Default.Search
        "settings", "gear" -> Icons.Default.Settings
        "star" -> Icons.Default.Star
        "info" -> Icons.Default.Info
        "warning", "alert" -> Icons.Default.Warning
        "delete", "trash" -> Icons.Default.Delete
        "edit" -> Icons.Default.Edit
        "refresh" -> Icons.Default.Refresh
        "menu" -> Icons.Default.Menu
        "favorite", "heart" -> Icons.Default.Favorite
        "favorite_border", "heart_border" -> Icons.Default.FavoriteBorder
        "home" -> Icons.Default.Home
        "person", "account", "user" -> Icons.Default.Person
        "lock" -> Icons.Default.Lock
        "notifications", "bell" -> Icons.Default.Notifications
        "send" -> Icons.AutoMirrored.Filled.Send
        "shopping_cart", "cart" -> Icons.Default.ShoppingCart
        "thumb_up", "like" -> Icons.Default.ThumbUp
        "place", "location" -> Icons.Default.Place
        "play", "play_arrow" -> Icons.Default.PlayArrow
        "list" -> Icons.AutoMirrored.Filled.List
        else -> null
    }
}

private fun parseColor(colorString: String?): Color? {
    if (colorString == null) return null
    val clean = colorString.trim()
    return if (clean.startsWith("#")) {
        runCatching {
            val hex = clean.removePrefix("#")
            when (hex.length) {
                6 -> Color(hex.toLong(16) or 0x00000000FF000000)
                8 -> Color(hex.toLong(16))
                else -> null
            }
        }.getOrNull()
    } else {
        null
    }
}
