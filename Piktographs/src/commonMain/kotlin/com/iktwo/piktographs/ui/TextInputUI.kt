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
import com.iktwo.piktographs.LocalElementEnabled
import com.iktwo.piktographs.LocalElementTextInput
import com.iktwo.piktographs.LocalElementValidity

@Composable
fun TextInputUI(
    element: InputElement,
    inputHandler: InputHandler,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        TextField(
            singleLine = true,
            enabled = LocalElementEnabled.current,
            value = LocalElementTextInput.current,
            onValueChange = { newText ->
                inputHandler.onTextInput(element, newText)
            },
            placeholder = {
                element.textSecondary?.let { placeholder ->
                    Text(placeholder)
                }
            },
            isError = element.text != null && !LocalElementValidity.current,
        )
    }
}
