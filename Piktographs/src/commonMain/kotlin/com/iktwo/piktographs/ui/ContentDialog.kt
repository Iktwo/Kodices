package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable

@Composable
expect fun ContentDialog(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
)
