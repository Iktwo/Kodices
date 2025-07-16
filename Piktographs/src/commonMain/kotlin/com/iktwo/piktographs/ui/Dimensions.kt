package com.iktwo.piktographs.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Dimensions(
    val padding: Dp = SPACING,
    val verticalSpacing: Dp = VERTICAL_SPACING,
    val horizontalSpacing: Dp = HORIZONTAL_SPACING,
    val separator: Dp = SEPARATOR_SIZE,
)

private val SPACING = 8.dp
private val VERTICAL_SPACING = 4.dp
private val HORIZONTAL_SPACING = 4.dp
private val SEPARATOR_SIZE = 8.dp

data class FontSizes(
    val primary: TextUnit = FONT_SIZE_PRIMARY,
    val secondary: TextUnit = FONT_SIZE_SECONDARY,
)

private val FONT_SIZE_PRIMARY = 18.sp
private val FONT_SIZE_SECONDARY = 12.sp

data class Colors(
    val mainTextColor: Color = MAIN_TEXT_COLOR,
    val secondaryTextColor: Color = SECONDARY_TEXT_COLOR,
    val separatorColor: Color = SEPARATOR_COLOR,
)

private val MAIN_TEXT_COLOR = Color.Black
private val SECONDARY_TEXT_COLOR = Color(0xFF131313)
private val SEPARATOR_COLOR = Color(0xFF282828)

data class Theme(
    val fonts: FontSizes = FontSizes(),
    val colors: Colors = Colors(),
    val dimensions: Dimensions = Dimensions(),
)

val DefaultTheme = compositionLocalOf { Theme() }
