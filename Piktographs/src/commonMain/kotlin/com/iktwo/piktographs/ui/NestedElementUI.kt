package com.iktwo.piktographs.ui

import androidx.compose.runtime.Composable
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.piktographs.ElementUI
import com.iktwo.piktographs.LocalBooleanInputData
import com.iktwo.piktographs.LocalElementOverrides
import com.iktwo.piktographs.LocalInputHandler
import com.iktwo.piktographs.LocalTextInputData
import com.iktwo.piktographs.LocalValidityMap

@Composable
fun NestedElementUI(nestedElement: ProcessedElement) {
    ElementUI(
        element = nestedElement,
        elementOverrides = LocalElementOverrides.current,
        inputHandler = LocalInputHandler.current,
        textInputData = LocalTextInputData.current,
        booleanInputData = LocalBooleanInputData.current,
        validityMap = LocalValidityMap.current,
    )
}
