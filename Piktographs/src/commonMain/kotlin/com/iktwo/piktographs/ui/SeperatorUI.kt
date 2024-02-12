package com.iktwo.piktographs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asIntOrNull

const val SEPARATOR_ELEMENT_TYPE = "separator"

private const val SIZE_KEY = "size"

@Composable
fun SeperatorUI(element: ProcessedElement) {
    val size = element.jsonValues[SIZE_KEY]?.asIntOrNull()?.dp
        ?: DefaultTheme.current.dimensions.separator

    val style = SeparatorStyle.fromString(element.style ?: "")

    when (style) {
        SeparatorStyle.SPACE -> {
            Box(modifier = Modifier.height(size).width(size))
        }

        SeparatorStyle.LINE -> {
            Box(
                modifier = Modifier.height(size).fillMaxWidth()
                    .background(DefaultTheme.current.colors.separatorColor)
            )
        }
    }
}

enum class SeparatorStyle {
    SPACE,
    LINE,
    ;

    companion object {
        fun fromString(style: String): SeparatorStyle {
            return entries.firstOrNull { it.name.equals(style, ignoreCase = true) } ?: SPACE
        }
    }
}