package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    initialDataString: String,
    onJSONDataChanged: (String) -> Unit,
    onJSONUIChanged: (String) -> Unit,
) {
    var jsonContent by remember { mutableStateOf(initialContentString) }
    var jsonData by remember { mutableStateOf(initialContentString) }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                JSONTextEditor(
                    initialValue = initialContentString,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    jsonContent = it
                    onJSONUIChanged(it)
                }
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                JSONTextEditor(
                    title = "Data",
                    initialValue = initialDataString,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    jsonData = it
                    onJSONDataChanged(it)
                }
            }
        }

        JsonContent(
            contentString = jsonContent,
            dataString = jsonData,
            elementOverrides = { elementOverride(it) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}
