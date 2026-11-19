package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.utils.asIntOrNull
import com.iktwo.piktographs.LocalElementEnabled
import com.iktwo.piktographs.LocalElementTextInput
import com.iktwo.piktographs.LocalElementValidity

@Composable
public fun TextAreaUI(
    element: InputElement,
    inputHandler: InputHandler,
) {
    // TextField requires 1 <= minLines <= maxLines and throws otherwise, so clamp server values
    // instead of trusting them (e.g. "maxLines": 2 alone would conflict with the default minLines).
    val maxLines = (element.jsonValues["maxLines"]?.asIntOrNull() ?: Int.MAX_VALUE).coerceAtLeast(1)
    val minLines = (element.jsonValues["minLines"]?.asIntOrNull() ?: 3).coerceIn(1, maxLines)

    Column(
        modifier = Modifier.fillMaxWidth().padding(LocalTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(LocalTheme.current.dimensions.verticalSpacing),
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = LocalElementEnabled.current,
            value = LocalElementTextInput.current,
            minLines = minLines,
            maxLines = maxLines,
            onValueChange = { newText ->
                inputHandler.onTextInput(element, newText)
            },
            placeholder = {
                element.textSecondary?.let { placeholder ->
                    Text(placeholder)
                }
            },
            isError = !LocalElementValidity.current && (LocalElementTextInput.current.isNotEmpty() || element.text != null),
        )
    }
}
