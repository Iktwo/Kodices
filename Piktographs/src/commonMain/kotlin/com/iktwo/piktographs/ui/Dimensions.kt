package com.iktwo.piktographs.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Dimensions(
    val padding: Dp = SPACING,
    val verticalSpacing: Dp = VERTICAL_SPACING,
    val horizontalSpacing: Dp = HORIZONTAL_SPACING
)

private val SPACING = 8.dp
private val VERTICAL_SPACING = 4.dp
private val HORIZONTAL_SPACING = 4.dp

data class FontSizes(
    val primary: TextUnit = FONT_SIZE_PRIMARY,
    val secondary: TextUnit = FONT_SIZE_SECONDARY
)

private val FONT_SIZE_PRIMARY = 18.sp
private val FONT_SIZE_SECONDARY = 12.sp

data class Theme(val fonts: FontSizes = FontSizes(), val dimensions: Dimensions = Dimensions())

val DefaultTheme = compositionLocalOf { Theme() }