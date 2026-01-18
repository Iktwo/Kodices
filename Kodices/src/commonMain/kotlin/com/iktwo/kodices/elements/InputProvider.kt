package com.iktwo.kodices.elements

/**
 * Interface representing an input provider.
 *
 * @property isValid A Boolean indicating whether the input is valid.
 */
interface InputProvider {
    val isValid: Boolean
}

/**
 * Constant representing a checkbox input element.
 */
const val INPUT_ELEMENT_CHECKBOX = "checkbox"

/**
 * Constant representing a text input element.
 */
const val INPUT_ELEMENT_TEXT_INPUT = "textInput"

/**
 * Constant representing a text area input element.
 */
const val INPUT_ELEMENT_TEXT_AREA = "textArea"

/**
 * List of default input element types.
 */
val DefaultInputElements = listOf(
    INPUT_ELEMENT_CHECKBOX,
    INPUT_ELEMENT_TEXT_INPUT,
    INPUT_ELEMENT_TEXT_AREA,
)
