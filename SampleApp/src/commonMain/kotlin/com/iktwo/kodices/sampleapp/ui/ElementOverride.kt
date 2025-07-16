@file:OptIn(ExperimentalTime::class)

package com.iktwo.kodices.sampleapp.ui

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.sampleapp.LastSecond
import com.iktwo.piktographs.elements.CountdownElement
import kotlin.time.ExperimentalTime

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
