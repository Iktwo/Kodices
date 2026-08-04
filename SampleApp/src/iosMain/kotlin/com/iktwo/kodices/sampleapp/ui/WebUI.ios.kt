package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.piktographs.elements.WebElement

/**
 * iOS has no WKWebView-backed implementation yet. Renders an explicit placeholder rather than
 * nothing, so a `web` element does not silently vanish on this platform.
 */
@Composable
actual fun WebUI(element: WebElement) {
    Text(
        text = "Web content is not supported on iOS yet: ${element.url}",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    )
}
