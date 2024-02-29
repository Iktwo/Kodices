package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler

@Composable
fun TextInputUI(element: InputElement, inputHandler: InputHandler) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing)
    ) {
        TextField(
            singleLine = true,
            value = element.text ?: "",
            onValueChange = { newText ->
                inputHandler.onTextInput(element.copy(text = newText), newText)
            },
            placeholder = {
                element.textSecondary?.let { placeholder ->
                    Text(placeholder)
                }
            },
            isError = element.text != null && !element.isValid
        )
    }
}
