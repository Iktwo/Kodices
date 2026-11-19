package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.iktwo.kodices.sampleapp.theme.AppTheme
import com.iktwo.kodices.sampleapp.theme.SPACING
import com.iktwo.piktographs.elements.WebElement

@Composable
actual fun WebUI(element: WebElement) {
    Text(
        "WebUI is not currently supported on Desktop",
        modifier = Modifier.fillMaxWidth().padding(SPACING),
        color = Color.Red,
    )
}

@Preview
@Composable
private fun WebUIPreview() {
    AppTheme(darkTheme = false) {
        Surface { WebUI(WebElement(id = "web", jsonValues = emptyMap(), url = "https://example.com", jsOnLoad = null)) }
    }
}
