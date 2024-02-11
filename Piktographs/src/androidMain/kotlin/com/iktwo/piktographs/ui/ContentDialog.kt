package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
actual fun ContentDialog(
    title: String?,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onCloseRequest) {
        content()
    }
}
