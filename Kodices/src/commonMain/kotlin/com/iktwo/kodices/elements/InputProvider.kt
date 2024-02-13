package com.iktwo.kodices.elements

interface InputProvider {
    val isValid: Boolean
}

const val INPUT_ELEMENT_CHECKBOX = "checkbox"
const val INPUT_ELEMENT_TEXT_INPUT = "textInput"
const val INPUT_ELEMENT_TEXT_AREA = "textArea"

val DefaultInputElements = listOf(
    INPUT_ELEMENT_CHECKBOX,
    INPUT_ELEMENT_TEXT_INPUT,
    INPUT_ELEMENT_TEXT_AREA
)
