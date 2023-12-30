package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.iktwo.piktographs.elements.WebElement
import com.iktwo.kodices.sampleapp.theme.SPACING

@Composable
actual fun WebUI(element: WebElement) {
    Text(
        "WebUI is not currently supported on Desktop",
        modifier = Modifier.fillMaxWidth().padding(SPACING),
        color = Color.Red,
    )
}
