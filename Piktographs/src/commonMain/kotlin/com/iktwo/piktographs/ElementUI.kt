package com.iktwo.piktographs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_AREA
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.piktographs.ui.BUTTON_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ButtonUI
import com.iktwo.piktographs.ui.CARD_ELEMENT_TYPE
import com.iktwo.piktographs.ui.CardUI
import com.iktwo.piktographs.ui.CheckboxUI
import com.iktwo.piktographs.ui.Constants
import com.iktwo.piktographs.ui.Constants.TOP_BAR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.IMAGE_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ImageUI
import com.iktwo.piktographs.ui.PROGRESS_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ProgressUI
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
import com.iktwo.piktographs.ui.SEPARATOR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.SeperatorUI
import com.iktwo.piktographs.ui.TextAreaUI
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.UnknownElementUI

public inline fun ActionPerformer(crossinline block: (Action) -> Unit): ActionPerformer =
    object : ActionPerformer {
        override fun onAction(action: Action) {
            block(action)
        }
    }

public val LocalElementOverrides: ProvidableCompositionLocal<@Composable (ProcessedElement) -> Boolean> =
    compositionLocalOf { error("No element overrides provided") }

public val LocalActionPerformer: ProvidableCompositionLocal<ActionPerformer> =
    compositionLocalOf { ActionPerformer { } }

public val LocalInputHandler: ProvidableCompositionLocal<InputHandler> =
    compositionLocalOf { error("No input handler provided") }

public val LocalTextInputData: ProvidableCompositionLocal<SnapshotStateMap<String, String?>> =
    compositionLocalOf { error("No text input data provided") }

public val LocalBooleanInputData: ProvidableCompositionLocal<SnapshotStateMap<String, Boolean>> =
    compositionLocalOf { error("No boolean input data provided") }

public val LocalValidityMap: ProvidableCompositionLocal<SnapshotStateMap<String, Boolean>> =
    compositionLocalOf { error("No validity map provided") }

public val LocalElementEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

public val LocalElementTextInput: ProvidableCompositionLocal<String> = compositionLocalOf { "" }

public val LocalElementBooleanInput: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

public val LocalElementValidity: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

@Composable
public fun ElementUI(
    element: ProcessedElement,
) {
    val inputHandler = LocalInputHandler.current
    val textInputData = LocalTextInputData.current
    val validityMap = LocalValidityMap.current
    val booleanInputData = LocalBooleanInputData.current
    val elementOverrides = LocalElementOverrides.current

    val isEnabled by remember(element.enabled, element.requiresValidElements, validityMap) {
        derivedStateOf {
            // element.enabled is the static flag from the JSON; requiresValidElements gates it on
            // the live validity of other inputs. Both feed LocalElementEnabled so every renderer
            // reads a single, complete answer instead of combining the two itself.
            element.enabled &&
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
                        SideEffect {
                            val isElementValid = element.isValid(textInputData[element.id] ?: element.text ?: "")
                            if (validityMap[element.id] != isElementValid) {
                                validityMap[element.id] = isElementValid
                            }
                        }

                        TextAreaUI(element, inputHandler)
                    }

                    INPUT_ELEMENT_CHECKBOX if element is InputElement -> {
                        CheckboxUI(element, inputHandler)
                    }

                    BUTTON_ELEMENT_TYPE -> {
                        ButtonUI(element)
                    }

                    CARD_ELEMENT_TYPE -> {
                        CardUI(element)
                    }

                    PROGRESS_ELEMENT_TYPE -> {
                        ProgressUI(element)
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
