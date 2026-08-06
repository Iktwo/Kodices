package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.sampleapp.DefaultActionPerformer
import com.iktwo.kodices.sampleapp.theme.SPACING
import com.iktwo.piktographs.LocalElementEnabled

const val BUTTON_ELEMENT_TYPE = "button"

@Composable
fun ButtonUI(element: ProcessedElement) {
    val actionPerformer = DefaultActionPerformer.current

    Box(modifier = Modifier.fillMaxWidth().padding(SPACING)) {
        Button(
            onClick = {
                actionPerformer.onAction(element.actions.first())
            },
            // LocalElementEnabled already folds in element.enabled plus requiresValidElements
            // gating, so a button with requiresValidElements only becomes clickable once those
            // inputs are valid.
            enabled = LocalElementEnabled.current && element.actions.isNotEmpty(),
        ) {
            Text(element.text ?: "")
        }
    }
}
