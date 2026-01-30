package com.iktwo.piktographs.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Composable utility to test different themes
 *
 * Usage:
 *
 * ThemeSplitter(
 *  { content -> BitBoardTheme(darkTheme = true, content = content) },
 *  { content -> BitBoardTheme(darkTheme = false, content = content) },
 * ) {
 *  // Composable to render
 * }
 *
 */
@Suppress("unused")
@Composable
fun ThemeSplitter(
    vararg themes: @Composable (content: @Composable () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    if (themes.isEmpty()) {
        content()
        return
    }

    if (themes.size == 1) {
        themes[0] { content() }
        return
    }

    val isTwoWay = themes.size == 2
    val isThreeWay = themes.size == 3

    var parentSize by remember { mutableStateOf(Size.Zero) }
    var dividerX by remember { mutableFloatStateOf(0f) }
    var dividerY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                parentSize = Size(size.width.toFloat(), size.height.toFloat())

                if (dividerX == 0f && parentSize.width > 0) {
                    dividerX = parentSize.width / 2f
                    dividerY = parentSize.height / 2f
                }
            },
    ) {
        val getTheme = { index: Int ->
            themes.getOrElse(index) { themes.last() }
        }

        val themeTL = getTheme(0)

        themeTL {
            val bottomLimit = if (isTwoWay) parentSize.height else dividerY
            ClippedBox(Rect(0f, 0f, dividerX, bottomLimit), content)
        }

        val themeTR = getTheme(1)

        themeTR {
            val bottomLimit = if (isTwoWay) parentSize.height else dividerY
            ClippedBox(Rect(dividerX, 0f, parentSize.width, bottomLimit), content)
        }

        if (!isTwoWay) {
            val themeBL = getTheme(2)

            if (isThreeWay) {
                themeBL {
                    ClippedBox(Rect(0f, dividerY, parentSize.width, parentSize.height), content)
                }
            } else {
                themeBL {
                    ClippedBox(Rect(0f, dividerY, dividerX, parentSize.height), content)
                }

                val themeBR = getTheme(3)
                themeBR {
                    ClippedBox(
                        Rect(dividerX, dividerY, parentSize.width, parentSize.height),
                        content,
                    )
                }
            }
        }

        val lineColor = Color.White.copy(alpha = 0.5f)
        val density = LocalDensity.current

        val vertHeightPx = if (isThreeWay) dividerY else parentSize.height
        val vertHeightDp = with(density) { vertHeightPx.toDp() }

        Box(
            modifier = Modifier
                .offset { IntOffset(dividerX.roundToInt(), 0) }
                .width(2.dp)
                .height(vertHeightDp)
                .background(lineColor),
        )

        if (!isTwoWay) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, dividerY.roundToInt()) }
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(lineColor),
            )
        }

        val handleSizeDp = 48.dp
        val halfHandleSizePx = with(density) { handleSizeDp.toPx() / 2f }

        val currentHandleY = if (isTwoWay) parentSize.height / 2f else dividerY

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (dividerX - halfHandleSizePx).roundToInt(),
                        y = (currentHandleY - halfHandleSizePx).roundToInt(),
                    )
                }
                .size(handleSizeDp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(isTwoWay) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        dividerX = (dividerX + dragAmount.x).coerceIn(0f, parentSize.width)

                        if (!isTwoWay) {
                            dividerY = (dividerY + dragAmount.y).coerceIn(0f, parentSize.height)
                        }
                    }
                },
        )
    }
}

@Composable
private fun ClippedBox(
    rect: Rect,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(object : Shape {
                override fun createOutline(
                    size: Size,
                    layoutDirection: LayoutDirection,
                    density: Density,
                ): Outline {
                    return Outline.Rectangle(rect)
                }
            }),
    ) {
        content()
    }
}
