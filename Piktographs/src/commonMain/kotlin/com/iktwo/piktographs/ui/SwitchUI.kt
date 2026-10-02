package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.piktographs.LocalElementBooleanInput
import com.iktwo.piktographs.LocalElementEnabled

@Composable
public fun SwitchUI(
    element: InputElement,
    inputHandler: InputHandler,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing / 2),
            ) {
                element.text?.let { text ->
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                element.textSecondary?.let { subtitle ->
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Switch(
                checked = LocalElementBooleanInput.current,
                enabled = LocalElementEnabled.current,
                onCheckedChange = { newCheckedValue ->
                    inputHandler.onBooleanInput(element, newCheckedValue)
                },
            )
        }

        for (nestedElement in element.nestedElements) {
            NestedElementUI(nestedElement)
        }
    }
}
