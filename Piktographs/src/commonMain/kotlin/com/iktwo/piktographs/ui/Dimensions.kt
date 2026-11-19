package com.iktwo.piktographs.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public data class Dimensions(
    val padding: Dp = SPACING,
    val verticalSpacing: Dp = VERTICAL_SPACING,
    val horizontalSpacing: Dp = HORIZONTAL_SPACING,
    val separator: Dp = SEPARATOR_SIZE,
)

private val SPACING = 8.dp
private val VERTICAL_SPACING = 4.dp
private val HORIZONTAL_SPACING = 4.dp
private val SEPARATOR_SIZE = 8.dp

public data class FontSizes(
    val primary: TextUnit = FONT_SIZE_PRIMARY,
    val secondary: TextUnit = FONT_SIZE_SECONDARY,
)

private val FONT_SIZE_PRIMARY = 18.sp
private val FONT_SIZE_SECONDARY = 12.sp

/**
 * Colors for Piktographs' own elements.
 *
 * [mainTextColor] and [secondaryTextColor] default to [Color.Unspecified], which means "follow the
 * Material theme" (`onSurface` / `onSurfaceVariant`), so text stays readable in dark themes unless a
 * host sets them explicitly.
 *
 * [infoColor], [successColor], [warningColor] and [errorColor] are what an element's `tone` is drawn
 * in. Left [Color.Unspecified], info and error follow the Material theme (`primary`, `error`) and
 * success and warning are a green and an amber picked for the theme's light or dark surface.
 */
public data class Colors(
    val mainTextColor: Color = MAIN_TEXT_COLOR,
    val secondaryTextColor: Color = SECONDARY_TEXT_COLOR,
    val separatorColor: Color = SEPARATOR_COLOR,
    val infoColor: Color = Color.Unspecified,
    val successColor: Color = Color.Unspecified,
    val warningColor: Color = Color.Unspecified,
    val errorColor: Color = Color.Unspecified,
)

private val MAIN_TEXT_COLOR = Color.Unspecified
private val SECONDARY_TEXT_COLOR = Color.Unspecified
private val SEPARATOR_COLOR = Color(0xFF282828)

public data class Theme(
    val fonts: FontSizes = FontSizes(),
    val colors: Colors = Colors(),
    val dimensions: Dimensions = Dimensions(),
)

/**
 * The [Theme] elements draw with. `PageUI` provides the one it is given; read it with
 * `LocalTheme.current`.
 */
public val LocalTheme: androidx.compose.runtime.ProvidableCompositionLocal<Theme> = compositionLocalOf { Theme() }

@Deprecated("Renamed to LocalTheme, the Compose naming for a CompositionLocal.", ReplaceWith("LocalTheme"))
public val DefaultTheme: androidx.compose.runtime.ProvidableCompositionLocal<Theme> get() = LocalTheme
