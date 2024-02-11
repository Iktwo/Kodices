package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogWindow

@Composable
actual fun ContentDialog(
    title: String?,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    DialogWindow(title = title ?: "", onCloseRequest = onCloseRequest) {
        content()
    }
}
