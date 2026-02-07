package com.iktwo.piktographs.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

fun Modifier.wipOverlay(
    text: String = "WIP",
    visible: Boolean = true,
    stripColor: Color = Color(0xFFFFD700),
    textColor: Color = Color.Black,
    overlayColor: Color = Color.Yellow.copy(alpha = 0.1f),
    blockInput: Boolean = true,
): Modifier = composed {
    if (!visible) return@composed this

    val textMeasurer = rememberTextMeasurer()

    val textStyle = remember {
        TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (blockInput) {
        this.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                }
            }
        }
    } else {
        this
    }
        .clipToBounds()
        .drawWithContent {
            drawContent()

            drawRect(color = overlayColor)

            val diagonal = sqrt(size.width * size.width + size.height * size.height)
            val stripHeight = 24.dp.toPx()
            val gap = 24.dp.toPx()

            val textLayoutResult = textMeasurer.measure(text, textStyle)
            val textWidth = textLayoutResult.size.width
            val textPadding = 20.dp.toPx()

            rotate(degrees = -45f) {
                var currentY = -diagonal

                while (currentY < diagonal) {
                    drawRect(
                        color = stripColor,
                        topLeft = Offset(x = -diagonal, y = currentY),
                        size = Size(width = diagonal * 2, height = stripHeight)
                    )

                    var currentX = -diagonal
                    while (currentX < diagonal * 2) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = currentX,
                                y = currentY + (stripHeight - textLayoutResult.size.height) / 2
                            )
                        )
                        currentX += textWidth + textPadding
                    }

                    currentY += stripHeight + gap
                }
            }
        }
}

fun Modifier.wipOverlayWithAnimationSupport(
    text: String = "WIP",
    visible: Boolean = true,
    stripColor: Color = Color(0xFFFFD700),
    textColor: Color = Color.Black,
    overlayColor: Color = Color.Yellow.copy(alpha = 0.1f),
    blockInput: Boolean = true,
): Modifier = composed {
    val transition = updateTransition(targetState = visible, label = "WIP Overlay")

    if (!visible && transition.currentState == transition.targetState) {
        return@composed this
    }

    val removalProgress by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 3000, easing = FastOutSlowInEasing)
        },
        label = "Slide"
    ) { isVisible ->
        if (isVisible) 0f else 1f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 3000, easing = LinearEasing)
        },
        label = "Alpha"
    ) { isVisible ->
        if (isVisible) 1f else 0f
    }

    val textMeasurer = rememberTextMeasurer()

    val textStyle = remember(alpha) {
        TextStyle(
            color = textColor.copy(alpha = textColor.alpha * alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (blockInput && visible) {
        this.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                }
            }
        }
    } else {
        this
    }
        .clipToBounds()
        .drawWithContent {
            drawContent()

            drawRect(color = overlayColor.copy(alpha = overlayColor.alpha * alpha))

            val diagonal = sqrt(size.width * size.width + size.height * size.height)
            val stripHeight = 24.dp.toPx()
            val gap = 24.dp.toPx()

            val textLayoutResult = textMeasurer.measure(text, textStyle)
            val textWidth = textLayoutResult.size.width
            val textPadding = 20.dp.toPx()

            val slideOffset = diagonal * 2 * removalProgress

            rotate(degrees = -45f) {
                var currentY = -diagonal + slideOffset

                while (currentY < diagonal + slideOffset) {
                    if (currentY > -diagonal - stripHeight && currentY < diagonal + stripHeight) {

                        drawRect(
                            color = stripColor.copy(alpha = stripColor.alpha * alpha),
                            topLeft = Offset(x = -diagonal, y = currentY),
                            size = Size(width = diagonal * 2, height = stripHeight)
                        )

                        var currentX = -diagonal
                        while (currentX < diagonal * 2) {
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(
                                    x = currentX,
                                    y = currentY + (stripHeight - textLayoutResult.size.height) / 2
                                )
                            )
                            currentX += textWidth + textPadding
                        }
                    }
                    currentY += stripHeight + gap
                }
            }
        }
}

@Preview
@Composable
fun PreviewWIPModifier() {
    Surface {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Text with modifier", modifier = Modifier.wipOverlay())

            Text(text = "Text without modifier")
        }
    }
}

@Preview
@Composable
fun PreviewWIPAnimationModifier() {
    var visible by remember { mutableStateOf(true) }

    Surface {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Text with modifier",
                modifier = Modifier.wipOverlayWithAnimationSupport(
                    visible = visible,
                )
            )

            Button(onClick = {
                visible = !visible
            }) {
                Text(("Animate"))
            }
        }
    }
}
