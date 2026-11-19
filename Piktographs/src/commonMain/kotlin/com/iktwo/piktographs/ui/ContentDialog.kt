package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable

/**
 * Shows [content] in a platform dialog.
 *
 * Every platform renders a [androidx.compose.ui.window.Dialog] inside the current window, on a
 * surface with [title] (if any) above [content], like [ContentBottomSheet].
 *
 * @param title shown at the top of the dialog.
 * @param onCloseRequest invoked when the user dismisses the dialog.
 * @param content the dialog body.
 */
@Composable
public expect fun ContentDialog(
    title: String? = null,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
)
