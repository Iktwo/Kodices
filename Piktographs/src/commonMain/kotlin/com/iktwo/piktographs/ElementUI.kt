package com.iktwo.piktographs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_AREA
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.piktographs.ui.CheckboxUI
import com.iktwo.piktographs.ui.Constants
import com.iktwo.piktographs.ui.Constants.TOP_BAR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.IMAGE_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ImageUI
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
import com.iktwo.piktographs.ui.SEPARATOR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.SeperatorUI
import com.iktwo.piktographs.ui.TextAreaUI
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.UnknownElementUI

val LocalElementOverrides = compositionLocalOf<@Composable (ProcessedElement) -> Boolean> { error("No element overrides provided") }

val LocalInputHandler = compositionLocalOf<InputHandler> { error("No input handler provided") }

val LocalTextInputData = compositionLocalOf<SnapshotStateMap<String, String?>> { error("No text input data provided") }

val LocalBooleanInputData = compositionLocalOf<SnapshotStateMap<String, Boolean>> { error("No boolean input data provided") }

val LocalValidityMap = compositionLocalOf<SnapshotStateMap<String, Boolean>> { error("No validity map provided") }

val LocalElementEnabled = compositionLocalOf { true }

val LocalElementTextInput = compositionLocalOf { "" }

val LocalElementBooleanInput = compositionLocalOf { false }

val LocalElementValidity = compositionLocalOf { true }

@Composable
fun ElementUI(
    element: ProcessedElement,
) {
    val inputHandler = LocalInputHandler.current
    val textInputData = LocalTextInputData.current
    val validityMap = LocalValidityMap.current
    val booleanInputData = LocalBooleanInputData.current
    val elementOverrides = LocalElementOverrides.current

    val isEnabled by remember(element.requiresValidElements, validityMap) {
        derivedStateOf {
            if (element.requiresValidElements.isEmpty()) {
                true
            } else {
                element.requiresValidElements.all { validityMap[it] == true }
            }
        }
    }

    val componentContent = @Composable {
        CompositionLocalProvider(LocalElementEnabled provides isEnabled) {
            Box {
                when (element.type) {
                    ROW_ELEMENT_TYPE -> {
                        RowUI(element)
                    }

                    INPUT_ELEMENT_TEXT_INPUT if element is InputElement -> {
                        SideEffect {
                            val isElementValid = element.isValid(textInputData[element.id] ?: element.text ?: "")
                            if (validityMap[element.id] != isElementValid) {
                                validityMap[element.id] = isElementValid
                            }
                        }

                        TextInputUI(
                            element = element,
                            inputHandler = inputHandler,
                        )
                    }

                    INPUT_ELEMENT_TEXT_AREA if element is InputElement -> {
                        TextAreaUI(element, inputHandler)
                    }

                    INPUT_ELEMENT_CHECKBOX if element is InputElement -> {
                        CheckboxUI(element, inputHandler)
                    }

                    SEPARATOR_ELEMENT_TYPE -> {
                        SeperatorUI(element)
                    }

                    IMAGE_ELEMENT_TYPE -> {
                        ImageUI(element)
                    }

                    TOP_BAR_ELEMENT_TYPE -> {
                        // Skip this element, as it should be rendered in the Scaffold
                    }

                    else -> {
                        if (!elementOverrides(element)) {
                            UnknownElementUI(element)
                        }
                    }
                }
            }
        }
    }

    if (element is InputElement) {
        val currentTextInput = textInputData[element.id] ?: element.text ?: ""

        val currentBooleanInput = booleanInputData[element.id] ?: element.jsonValues[Constants.ACTIVE_KEY]?.asBooleanOrNull() ?: false

        val isValid = validityMap[element.id] ?: element.isValid

        CompositionLocalProvider(
            LocalElementTextInput provides currentTextInput,
            LocalElementBooleanInput provides currentBooleanInput,
            LocalElementValidity provides isValid,
        ) {
            componentContent()
        }
    } else {
        componentContent()
    }
}
