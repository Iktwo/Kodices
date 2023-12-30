package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iktwo.kodices.Kodices
import com.iktwo.kodices.elements.ProcessedElement

@Composable
fun UnknownElementUI(processedElement: ProcessedElement) {
    if (Kodices.debug) {
        Text(
            "${processedElement.type} element is not supported",
            modifier = Modifier.fillMaxWidth().padding(
                8.dp,
            ),
            color = Color.Red,
            fontSize = 32.sp,
        )
    }
}
