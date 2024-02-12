package com.iktwo.piktographs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iktwo.kodices.elements.InputElement

@Composable
fun TextInputUI(element: InputElement) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DefaultTheme.current.dimensions.padding),
        verticalArrangement = Arrangement.spacedBy(DefaultTheme.current.dimensions.verticalSpacing)
    ) {
        TextField(value = "", onValueChange = {}, placeholder = {
            element.textSecondary?.let { placeholder ->
                Text(placeholder)
            }
        })
    }
}