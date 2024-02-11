package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.Composable

@Composable
fun TabInput(
    initialContentString: String,
    onJSONTextChanged: (String) -> Unit,
) {
    JSONTextEditor(initialValue = initialContentString) { onJSONTextChanged(it) }
}
