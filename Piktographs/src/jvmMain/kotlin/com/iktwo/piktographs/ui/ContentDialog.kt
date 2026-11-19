package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

// An in-window dialog, like every other platform, rather than a separate OS window: it works in a
// headless scene and in Compose UI tests, which cannot drive a second AWT window (Compose 1.12+
// only allows touching Swing from the event dispatch thread).
@Composable
public actual fun ContentDialog(
    title: String?,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onCloseRequest) {
        DialogContent(title = title, content = content)
    }
}
