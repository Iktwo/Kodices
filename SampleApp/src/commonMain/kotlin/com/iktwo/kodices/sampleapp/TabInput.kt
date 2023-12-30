package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.Composable
import com.iktwo.sampleapp.JSONTextEditor

@Composable
fun TabInput(
    initialContentString: String,
    onJSONTextChanged: (String) -> Unit,
) {
    JSONTextEditor(initialValue = initialContentString) { onJSONTextChanged(it) }
}
