package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.utils.asBooleanOrNull

private const val CHECKED_KEY = "checked"

@Composable
fun CheckboxUI(
    element: InputElement,
    inputHandler: InputHandler,
    inputData: Boolean?,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.horizontalSpacing)) {
            val checked = element.jsonValues[CHECKED_KEY]?.asBooleanOrNull()

            Checkbox(inputData ?: checked ?: false, onCheckedChange = { newCheckedValue ->
                inputHandler.onBooleanInput(element, newCheckedValue)
            })

            element.text?.let { text ->
                Text(text)
            }
        }
    }
}
