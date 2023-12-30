package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogWindow

@Composable
actual fun ContentDialog(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    DialogWindow(onCloseRequest = onCloseRequest) {
        content()
    }
}
