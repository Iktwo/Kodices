package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.sampleapp.theme.SPACING

const val ROW_ELEMENT_TYPE = "row"

@Composable
fun RowUI(element: ProcessedElement) {
    Column(modifier = Modifier.fillMaxWidth()) {
        element.text?.let {
            Text(it, modifier = Modifier.fillMaxWidth().padding(SPACING), color = Color.Black)
        }

        element.textSecondary?.let {
            Text(it, modifier = Modifier.fillMaxWidth().padding(SPACING), color = Color.Black)
        }
    }
}
