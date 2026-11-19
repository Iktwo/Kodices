package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * The body every [ContentDialog] actual shows: a surface with an optional [title] above [content],
 * the same arrangement as [ContentBottomSheet].
 */
@Composable
internal fun DialogContent(
    title: String?,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            content()
        }
    }
}

@Preview
@Composable
internal fun DialogContentPreview() {
    MaterialTheme {
        DialogContent(title = "Dialog title") {
            Text(text = "Dialog body", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview
@Composable
internal fun DialogContentNoTitlePreview() {
    MaterialTheme {
        DialogContent(title = null) {
            Text(text = "Dialog body without a title", modifier = Modifier.padding(16.dp))
        }
    }
}
