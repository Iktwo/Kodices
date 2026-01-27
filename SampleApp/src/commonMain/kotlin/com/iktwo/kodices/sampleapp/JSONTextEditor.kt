package com.iktwo.kodices.sampleapp

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Composable
fun JSONTextEditor(
    initialValue: String = "{}",
    title: String = "UI",
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

            @Suppress("AssignedValueIsNeverRead")
            validJSON = try {
                Json.parseToJsonElement(jsonString)
                onJSONTextChanged(jsonString)
                true
            } catch (e: SerializationException) {
                false
            }
        },
        label = { Text(title) },
        maxLines = Int.MAX_VALUE,
        isError = !validJSON,
        trailingIcon = {
            if (!validJSON) {
                Icon(Icons.Filled.Warning, "error", tint = MaterialTheme.colorScheme.error)
            }
        },
    )

    Button(
        onClick = {
            try {
                val element = Json.parseToJsonElement(layoutText)
                @Suppress("AssignedValueIsNeverRead")
                layoutText = Json.encodeToString(JsonElement.serializer(), element)
                validJSON = true
            } catch (e: SerializationException) {
                validJSON = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Format JSON")
    }
}
