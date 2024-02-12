package com.iktwo.kodices.elements

interface InputHandler {
    fun onTextInput(key: String, value: String)

    fun onBooleanInput(key: String, value: Boolean)
}