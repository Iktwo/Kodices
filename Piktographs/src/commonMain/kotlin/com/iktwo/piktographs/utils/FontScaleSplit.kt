package com.iktwo.piktographs.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Suppress("unused")
@Composable
fun FontScaleSplit(
    scaleA: Float = 1f,
    scaleB: Float = 2f,
    content: @Composable () -> Unit,
) {
    val currentDensity = LocalDensity.current

    val densityA = Density(currentDensity.density, fontScale = scaleA)
    val densityB = Density(currentDensity.density, fontScale = scaleB)

    ThemeSplitter(
        themes = arrayOf(
            { innerContent ->
                CompositionLocalProvider(LocalDensity provides densityA) {
                    innerContent()
                }
            },
            { innerContent ->
                CompositionLocalProvider(LocalDensity provides densityB) {
                    innerContent()
                }
            }
        )
    ) {
        content()
    }
}
