package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.piktographs.LocalElementBooleanInput

@Composable
fun CheckboxUI(
    element: InputElement,
    inputHandler: InputHandler,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.horizontalSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = LocalElementBooleanInput.current,
                enabled = element.enabled,
                onCheckedChange = { newCheckedValue ->
                    inputHandler.onBooleanInput(element, newCheckedValue)
                },
            )

            element.text?.let { text ->
                Text(text)
            }
        }

        for (nestedElement in element.nestedElements) {
            NestedElementUI(nestedElement)
        }
    }
}
