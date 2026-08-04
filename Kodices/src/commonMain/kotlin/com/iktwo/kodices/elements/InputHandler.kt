package com.iktwo.kodices.elements

public interface InputHandler {
    public fun onTextInput(
        element: ProcessedElement,
        value: String,
    )

    public fun onBooleanInput(
        element: ProcessedElement,
        value: Boolean,
    )
}
