package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iktwo.kodices.sampleapp.ui.elementOverride

@Composable
fun TabInput(
    initialContentString: String,
    onJSONTextChanged: (String) -> Unit,
) {
    var jsonContent by remember { mutableStateOf(initialContentString) }

    Row {
        JSONTextEditor(
            initialValue = initialContentString,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            jsonContent = it
            onJSONTextChanged(it)
        }

        JsonContent(
            contentString = jsonContent,
            dataString = "",
            elementOverrides = { elementOverride(it) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}
