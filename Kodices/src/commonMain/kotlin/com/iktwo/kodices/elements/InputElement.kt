package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.utils.asStringOrNull

class InputElement(
    id: String,
    nestedElements: List<ProcessedElement> = emptyList(),
    jsonValues: ProcessedValues,
    text: String? = null,
    textSecondary: String? = null,
    actions: List<Action> = emptyList(),
    override val type: String,
    override val inputKey: String,
    override val style: String? = null
) : ProcessedElement(
    type = type,
    nestedElements = nestedElements,
    id = id,
    text = text,
    textSecondary = textSecondary,
    actions = actions,
    jsonValues = jsonValues,
    style = style
), InputProvider {
    companion object {
        private const val INPUT_KEY = "inputKey"

        val builder: ElementBuilder = { type, id, processedValues, nestedElements, actions ->
            val commonElementProperties = processedValues.toCommonElementProperties()

            val inputKey = processedValues[INPUT_KEY]?.asStringOrNull()

            InputElement(
                id = id,
                nestedElements = nestedElements,
                jsonValues = processedValues.toMutableMap(),
                actions = actions,
                type = type,
                text = commonElementProperties.text,
                textSecondary = commonElementProperties.textSecondary,
                inputKey = inputKey ?: type,
                style = commonElementProperties.style
            )
        }
    }
}