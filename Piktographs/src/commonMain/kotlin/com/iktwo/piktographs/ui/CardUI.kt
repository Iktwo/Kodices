package com.iktwo.piktographs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull
import com.iktwo.piktographs.LocalActionPerformer

public const val CARD_ELEMENT_TYPE: String = "card"
private const val KEY_VARIANT: String = "variant"

@Composable
public fun CardUI(element: ProcessedElement) {
    val actionPerformer = LocalActionPerformer.current
    val variant = element.jsonValues[KEY_VARIANT]?.asStringOrNull()?.lowercase() ?: "elevated"

    val modifier = Modifier
        .fillMaxWidth()
        .padding(DefaultTheme.current.dimensions.padding)
        .let { baseModifier ->
            if (element.actions.isNotEmpty()) {
                baseModifier.clickable {
                    element.actions.forEach { action ->
                        actionPerformer.onAction(action)
                    }
                }
            } else {
                baseModifier
            }
        }

    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DefaultTheme.current.dimensions.padding),
        ) {
            element.text?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            element.textSecondary?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = DefaultTheme.current.dimensions.verticalSpacing),
                )
            }

            element.nestedElements.forEach { child ->
                NestedElementUI(child)
            }
        }
    }

    when (variant) {
        "outlined" -> OutlinedCard(modifier = modifier) { cardContent() }
        "filled" -> Card(modifier = modifier) { cardContent() }
        else -> ElevatedCard(modifier = modifier) { cardContent() }
    }
}
