package com.iktwo.sampleapp

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iktwo.kodices.sampleapp.json
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
        onValueChange = {
            layoutText = it

            validJSON =
                try {
                    val element = Json.parseToJsonElement(it)
                    onJSONTextChanged(it)

                    val stringified = json.encodeToString(JsonElement.serializer(), element)

                    if (it != stringified) {
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
                Icon(Icons.Filled.Warning, "error", tint = MaterialTheme.colors.error)
            }
        },
    )
}
