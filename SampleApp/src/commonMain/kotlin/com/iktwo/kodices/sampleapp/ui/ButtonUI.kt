package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.sampleapp.DefaultActionPerformer
import com.iktwo.kodices.sampleapp.theme.SPACING

const val BUTTON_ELEMENT_TYPE = "button"

@Composable
fun ButtonUI(element: ProcessedElement) {
    val actionPerformer = DefaultActionPerformer.current

    Box(modifier = Modifier.fillMaxWidth().padding(SPACING)) {
        Button(
            onClick = {
                actionPerformer.onAction(element.actions.first())
            },
            enabled = element.enabled && element.actions.isNotEmpty(),
        ) {
            Text(element.text ?: "")
        }
    }
}
