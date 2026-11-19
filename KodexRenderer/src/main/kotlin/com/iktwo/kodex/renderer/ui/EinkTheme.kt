package com.iktwo.kodex.renderer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iktwo.piktographs.ui.Colors
import com.iktwo.piktographs.ui.Dimensions
import com.iktwo.piktographs.ui.FontSizes
import com.iktwo.piktographs.ui.Theme

/**
 * Black on white and nothing in between.
 *
 * The panel turns Material's tonal palette (pastel containers, `onSurfaceVariant` greys, outline
 * greys) into mush, so every role is collapsed to pure black or pure white. Containers are white so
 * that tonal elevation cannot tint them.
 */
internal val EinkColorScheme: ColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,
    inversePrimary = Color.White,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color.White,
    onSecondaryContainer = Color.Black,
    tertiary = Color.Black,
    onTertiary = Color.White,
    tertiaryContainer = Color.White,
    onTertiaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color.White,
    onSurfaceVariant = Color.Black,
    surfaceTint = Color.White,
    inverseSurface = Color.Black,
    inverseOnSurface = Color.White,
    error = Color.Black,
    onError = Color.White,
    errorContainer = Color.White,
    onErrorContainer = Color.Black,
    outline = Color.Black,
    outlineVariant = Color.Black,
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceDim = Color.White,
)

/**
 * Material's type scale with the bundled font on every style, and no style lighter than Normal:
 * hairline strokes break up once the image is quantized to 16 grey levels.
 */
internal fun einkTypography(fontFamily: FontFamily): Typography {
    fun TextStyle.eink(): TextStyle {
        val weight = fontWeight ?: FontWeight.Normal
        return copy(
            fontFamily = fontFamily,
            fontWeight = if (weight < FontWeight.Normal) FontWeight.Normal else weight,
        )
    }

    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.eink(),
        displayMedium = base.displayMedium.eink(),
        displaySmall = base.displaySmall.eink(),
        headlineLarge = base.headlineLarge.eink(),
        headlineMedium = base.headlineMedium.eink(),
        headlineSmall = base.headlineSmall.eink(),
        titleLarge = base.titleLarge.eink().copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.eink().copy(fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.eink().copy(fontWeight = FontWeight.Bold),
        bodyLarge = base.bodyLarge.eink(),
        bodyMedium = base.bodyMedium.eink(),
        bodySmall = base.bodySmall.eink(),
        labelLarge = base.labelLarge.eink(),
        labelMedium = base.labelMedium.eink(),
        labelSmall = base.labelSmall.eink(),
    )
}

/**
 * The Piktographs [Theme] handed to `PageUI`.
 *
 * Text is larger than the Piktographs defaults because the panel is read from arm's length, and
 * separators are 2 dp, which is 4 px at the default render density: never below the 2 px floor.
 */
internal val EinkPageTheme: Theme = Theme(
    fonts = FontSizes(
        primary = 22.sp,
        secondary = 16.sp,
    ),
    colors = Colors(
        mainTextColor = Color.Black,
        secondaryTextColor = Color.Black,
        separatorColor = Color.Black,
        // A tone is a color and the panel has none: every tone is black, and what it meant has
        // to be read from the icon and the words next to it.
        infoColor = Color.Black,
        successColor = Color.Black,
        warningColor = Color.Black,
        errorColor = Color.Black,
    ),
    dimensions = Dimensions(
        padding = 16.dp,
        verticalSpacing = 6.dp,
        horizontalSpacing = 8.dp,
        separator = 2.dp,
    ),
)

/**
 * Wraps [content] in the e-ink Material theme on an opaque white page.
 *
 * The white background matters: anything left transparent would be composited by the panel's
 * firmware, not by us, and the output would stop being what the PNG shows.
 */
@Composable
internal fun EinkTheme(
    fontFamily: FontFamily = EinkFonts.notoSans,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EinkColorScheme,
        typography = einkTypography(fontFamily),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            content()
        }
    }
}
