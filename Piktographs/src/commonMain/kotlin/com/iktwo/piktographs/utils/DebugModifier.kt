package com.iktwo.piktographs.utils

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.iktwo.kodices.Kodices
import kotlin.random.Random

fun debugModifier(): Modifier {
    return if (Kodices.debug) Modifier.background(
        Color(
            red = Random.nextInt(255),
            green = Random.nextInt(255),
            blue = Random.nextInt(255),
            alpha = 100
        )
    ) else Modifier
}