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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import com.iktwo.kodices.elements.INPUT_ELEMENT_CHECKBOX
import com.iktwo.kodices.elements.INPUT_ELEMENT_SWITCH
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_AREA
import com.iktwo.kodices.elements.INPUT_ELEMENT_TEXT_INPUT
import com.iktwo.kodices.elements.INPUT_ELEMENT_TOGGLE
import com.iktwo.kodices.elements.InputElement
import com.iktwo.kodices.elements.InputHandler
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Logger
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.piktographs.ui.BADGE_ELEMENT_TYPE
import com.iktwo.piktographs.ui.BUTTON_ELEMENT_TYPE
import com.iktwo.piktographs.ui.BadgeUI
import com.iktwo.piktographs.ui.ButtonUI
import com.iktwo.piktographs.ui.CARD_ELEMENT_TYPE
import com.iktwo.piktographs.ui.CHART_ELEMENT_TYPE
import com.iktwo.piktographs.ui.CONTAINER_ELEMENT_TYPE
import com.iktwo.piktographs.ui.CONTAINER_TYPE_ALIASES
import com.iktwo.piktographs.ui.CardUI
import com.iktwo.piktographs.ui.ChartUI
import com.iktwo.piktographs.ui.CheckboxUI
import com.iktwo.piktographs.ui.Constants
import com.iktwo.piktographs.ui.Constants.TOP_BAR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ContainerUI
import com.iktwo.piktographs.ui.GRID_ELEMENT_TYPE
import com.iktwo.piktographs.ui.GridUI
import com.iktwo.piktographs.ui.ICON_ELEMENT_TYPE
import com.iktwo.piktographs.ui.IMAGE_ELEMENT_TYPE
import com.iktwo.piktographs.ui.IconUI
import com.iktwo.piktographs.ui.ImageUI
import com.iktwo.piktographs.ui.LIST_ITEM_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ListItemUI
import com.iktwo.piktographs.ui.PROGRESS_ELEMENT_TYPE
import com.iktwo.piktographs.ui.ProgressUI
import com.iktwo.piktographs.ui.ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.RowUI
import com.iktwo.piktographs.ui.SEPARATOR_ELEMENT_TYPE
import com.iktwo.piktographs.ui.STAT_ELEMENT_TYPE
import com.iktwo.piktographs.ui.SeparatorUI
import com.iktwo.piktographs.ui.StatUI
import com.iktwo.piktographs.ui.SwitchUI
import com.iktwo.piktographs.ui.TABLE_HEADER_ELEMENT_TYPE
import com.iktwo.piktographs.ui.TABLE_ROW_ELEMENT_TYPE
import com.iktwo.piktographs.ui.TABS_ELEMENT_TYPE
import com.iktwo.piktographs.ui.TEXT_ELEMENT_TYPE
import com.iktwo.piktographs.ui.TableHeaderUI
import com.iktwo.piktographs.ui.TableRowUI
import com.iktwo.piktographs.ui.TabsUI
import com.iktwo.piktographs.ui.TextAreaUI
import com.iktwo.piktographs.ui.TextInputUI
import com.iktwo.piktographs.ui.TextUI
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

/**
 * Where the renderer reports what it can only find out while drawing, such as an element written
 * with a deprecated name. Prints warnings and errors unless a host provides its own.
 */
public val LocalLogger: ProvidableCompositionLocal<Logger> = staticCompositionLocalOf { PrintLogger }

private object PrintLogger : Logger {
    override fun debug(message: String) = Unit

    override fun info(message: String) = Unit

    override fun warn(message: String) = println("W: $message")

    override fun error(message: String) = println("E: $message")
}

public val LocalElementEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

public val LocalElementTextInput: ProvidableCompositionLocal<String> = compositionLocalOf { "" }

public val LocalElementBooleanInput: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

public val LocalElementValidity: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

@Composable
public fun ElementUI(
    element: ProcessedElement,
    modifier: Modifier = Modifier,
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
            Box(modifier = modifier) {
                // Overrides are consulted first, so a host can restyle a built-in type as well as
                // render its own ones.
                if (!elementOverrides(element)) {
                    when (element.type) {
                        ROW_ELEMENT_TYPE -> {
                            RowUI(element)
                        }

                        CONTAINER_ELEMENT_TYPE, in CONTAINER_TYPE_ALIASES -> {
                            ContainerUI(element)
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

                        INPUT_ELEMENT_SWITCH if element is InputElement -> {
                            SwitchUI(element, inputHandler)
                        }

                        INPUT_ELEMENT_TOGGLE if element is InputElement -> {
                            SwitchUI(element, inputHandler)
                        }

                        ICON_ELEMENT_TYPE -> {
                            IconUI(element)
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
                            SeparatorUI(element)
                        }

                        IMAGE_ELEMENT_TYPE -> {
                            ImageUI(element)
                        }

                        TEXT_ELEMENT_TYPE -> {
                            TextUI(element)
                        }

                        STAT_ELEMENT_TYPE -> {
                            StatUI(element)
                        }

                        BADGE_ELEMENT_TYPE -> {
                            BadgeUI(element)
                        }

                        LIST_ITEM_ELEMENT_TYPE -> {
                            ListItemUI(element)
                        }

                        GRID_ELEMENT_TYPE -> {
                            GridUI(element)
                        }

                        CHART_ELEMENT_TYPE -> {
                            ChartUI(element)
                        }

                        TABLE_HEADER_ELEMENT_TYPE -> {
                            TableHeaderUI(element)
                        }

                        TABLE_ROW_ELEMENT_TYPE -> {
                            TableRowUI(element)
                        }

                        TABS_ELEMENT_TYPE -> {
                            TabsUI(element)
                        }

                        TOP_BAR_ELEMENT_TYPE -> {
                            // Skip this element, as it should be rendered in the Scaffold
                        }

                        else -> {
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
