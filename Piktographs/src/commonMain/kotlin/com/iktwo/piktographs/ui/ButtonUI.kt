package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer
import com.iktwo.piktographs.LocalElementEnabled

public const val BUTTON_ELEMENT_TYPE: String = "button"
private const val KEY_VARIANT: String = "variant"
private const val KEY_LOADING: String = "loading"

@Composable
public fun ButtonUI(element: ProcessedElement) {
    val actionPerformer = LocalActionPerformer.current
    val isEnabled = LocalElementEnabled.current && element.enabled
    val isLoading = element.jsonValues[KEY_LOADING]?.asBooleanOrNull() ?: false
    val variant = element.jsonValues[KEY_VARIANT]?.asStringOrNull()?.lowercase() ?: "filled"

    val onClick: () -> Unit = {
        if (!isLoading) {
            element.actions.forEach { action ->
                actionPerformer.onAction(action)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultTheme.current.dimensions.padding),
        contentAlignment = Alignment.Center,
    ) {
        val buttonContent: @Composable () -> Unit = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text = element.text ?: "")
            }
        }

        when (variant) {
            "outlined" -> {
                OutlinedButton(
                    onClick = onClick,
                    enabled = isEnabled && !isLoading,
                    content = { buttonContent() },
                )
            }

            "text" -> {
                TextButton(
                    onClick = onClick,
                    enabled = isEnabled && !isLoading,
                    content = { buttonContent() },
                )
            }

            "elevated" -> {
                ElevatedButton(
                    onClick = onClick,
                    enabled = isEnabled && !isLoading,
                    content = { buttonContent() },
                )
            }

            else -> {
                Button(
                    onClick = onClick,
                    enabled = isEnabled && !isLoading,
                    content = { buttonContent() },
                )
            }
        }
    }
}
