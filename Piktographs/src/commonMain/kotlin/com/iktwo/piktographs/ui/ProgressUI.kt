package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

public const val PROGRESS_ELEMENT_TYPE: String = "progress"
private const val KEY_VARIANT: String = "variant"
private const val KEY_PROGRESS: String = "progress"

@Composable
public fun ProgressUI(element: ProcessedElement) {
    val variant = element.jsonValues[KEY_VARIANT]?.asStringOrNull()?.lowercase() ?: "linear"
    val jsonPrimitive = element.jsonValues[KEY_PROGRESS] as? JsonPrimitive
    val progressValue = jsonPrimitive?.doubleOrNull?.toFloat() ?: jsonPrimitive?.content?.toFloatOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultTheme.current.dimensions.padding),
    ) {
        element.text?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = DefaultTheme.current.dimensions.verticalSpacing),
            )
        }

        when (variant) {
            "circular" -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (progressValue != null) {
                        CircularProgressIndicator(progress = { progressValue.coerceIn(0f, 1f) })
                    } else {
                        CircularProgressIndicator()
                    }

                    element.textSecondary?.let { subtitle ->
                        Spacer(modifier = Modifier.width(DefaultTheme.current.dimensions.horizontalSpacing))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                if (progressValue != null) {
                    LinearProgressIndicator(
                        progress = { progressValue.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                element.textSecondary?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = DefaultTheme.current.dimensions.verticalSpacing),
                    )
                }
            }
        }
    }
}
