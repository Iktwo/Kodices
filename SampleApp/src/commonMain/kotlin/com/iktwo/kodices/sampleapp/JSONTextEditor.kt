package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Composable
fun JSONTextEditor(
    initialValue: String = "{}",
    modifier: Modifier = Modifier.fillMaxWidth().fillMaxHeight(),
    onJSONTextChanged: (String) -> Unit,
) {
    var layoutText by remember { mutableStateOf(initialValue) }
    var validJSON by remember { mutableStateOf(true) }

    TextField(
        value = layoutText,
        modifier = modifier,
        onValueChange = { jsonString ->
            if (jsonString.isBlank()) {
                return@TextField
            }

            layoutText = jsonString

            validJSON =
                try {
                    val element = Json.parseToJsonElement(jsonString)
                    onJSONTextChanged(jsonString)

                    val stringified = json.encodeToString(JsonElement.serializer(), element)

                    if (jsonString != stringified) {
                        layoutText = stringified
                    }

                    true
                } catch (e: Exception) {
                    false
                }
        },
        label = { Text("layout") },
        maxLines = Int.MAX_VALUE,
        isError = !validJSON,
        trailingIcon = {
            if (!validJSON) {
                Icon(Icons.Filled.Warning, "error", tint = MaterialTheme.colorScheme.error)
            }
        },
    )
}
