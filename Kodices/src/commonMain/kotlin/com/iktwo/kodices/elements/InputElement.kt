package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.inputvalidation.Validation

class InputElement(
    id: String,
    nestedElements: List<ProcessedElement> = emptyList(),
    jsonValues: ProcessedValues,
    text: String? = null,
    textSecondary: String? = null,
    actions: List<Action> = emptyList(),
    override val type: String,
    override val style: String? = null,
    override val validation: Validation? = null,
    override val requiresValidElements: List<String> = emptyList(),
    override val enabled: Boolean = true
) : ProcessedElement(
    type = type,
    nestedElements = nestedElements,
    id = id,
    text = text,
    textSecondary = textSecondary,
    actions = actions,
    jsonValues = jsonValues,
    style = style,
    validation = validation,
    requiresValidElements = requiresValidElements,
    enabled = enabled
), InputProvider {
    override val isValid: Boolean
        get() = validation == null || validation.validate(text)

    override fun copy(
        id: String,
        index: Int,
        nestedElements: List<ProcessedElement>,
        text: String?,
        textSecondary: String?,
        actions: List<Action>,
        jsonValues: ProcessedValues,
        style: String?,
        validation: Validation?,
        requiresValidElements: List<String>,
        enabled: Boolean
    ): InputElement {
        return InputElement(
            id = id,
            nestedElements = nestedElements,
            jsonValues = jsonValues,
            actions = actions,
            type = type,
            text = text,
            textSecondary = textSecondary,
            style = style,
            validation = validation,
            requiresValidElements = requiresValidElements,
            enabled = enabled
        )
    }

    companion object {
        val builder: ElementBuilder = { type, id, processedValues, nestedElements, actions, json ->
            val commonElementProperties = processedValues.toCommonElementProperties(json)

            InputElement(
                id = id,
                nestedElements = nestedElements,
                jsonValues = processedValues.toMutableMap(),
                actions = actions,
                type = type,
                text = commonElementProperties.text,
                textSecondary = commonElementProperties.textSecondary,
                style = commonElementProperties.style,
                validation = commonElementProperties.validation,
                requiresValidElements = commonElementProperties.requiresValidElements
            )
        }
    }
}