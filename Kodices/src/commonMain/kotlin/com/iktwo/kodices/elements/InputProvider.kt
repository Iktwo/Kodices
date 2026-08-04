package com.iktwo.kodices.elements

/**
 * Interface representing an input provider.
 *
 * @property isValid A Boolean indicating whether the input is valid.
 */
public interface InputProvider {
    public val isValid: Boolean
}

/**
 * Constant representing a checkbox input element.
 */
public const val INPUT_ELEMENT_CHECKBOX: String = "checkbox"

/**
 * Constant representing a text input element.
 */
public const val INPUT_ELEMENT_TEXT_INPUT: String = "textInput"

/**
 * Constant representing a text area input element.
 */
public const val INPUT_ELEMENT_TEXT_AREA: String = "textArea"

/**
 * List of default input element types.
 */
public val DefaultInputElements: List<String> = listOf(
    INPUT_ELEMENT_CHECKBOX,
    INPUT_ELEMENT_TEXT_INPUT,
    INPUT_ELEMENT_TEXT_AREA,
)
