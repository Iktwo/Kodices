package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable

@Composable
expect fun ContentDialog(
    title: String? = null,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
)
