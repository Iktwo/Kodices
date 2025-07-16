package com.iktwo.kodices.elements

interface InputHandler {
    fun onTextInput(
        element: ProcessedElement,
        value: String,
    )

    fun onBooleanInput(
        element: ProcessedElement,
        value: Boolean,
    )
}
