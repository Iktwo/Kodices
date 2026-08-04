package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable

/**
 * Shows [content] in a platform dialog.
 *
 * @param title window title. Only the JVM/desktop actual has a window to put it on; the Android,
 * iOS and web actuals render a [androidx.compose.ui.window.Dialog] and ignore it. Render the title
 * inside [content] if it has to appear on every platform.
 * @param onCloseRequest invoked when the user dismisses the dialog.
 * @param content the dialog body.
 */
@Composable
public expect fun ContentDialog(
    title: String? = null,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
)
