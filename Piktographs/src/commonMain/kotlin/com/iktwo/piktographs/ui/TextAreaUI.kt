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

@Composable
fun TextAreaUI(
    element: InputElement,
    inputHandler: InputHandler,
    inputData: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        TextField(
            value = inputData ?: element.text ?: "",
            onValueChange = { newText ->
                inputHandler.onTextInput(element, newText)
            },
            placeholder = {
                element.textSecondary?.let { placeholder ->
                    Text(placeholder)
                }
            },
        )
    }
}
