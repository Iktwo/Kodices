package com.iktwo.kodices.sampleapp.ui

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.kodices.sampleapp.LastSecond

@Composable
fun ElementOverride(element: ProcessedElement): Boolean {
    when {
        element.type == BUTTON_ELEMENT_TYPE -> {
            ButtonUI(element)
        }

        element is CountdownElement -> {
            CountdownUI(element, LastSecond.current)
        }

        else -> {
            return false
        }
    }

    return true
}
