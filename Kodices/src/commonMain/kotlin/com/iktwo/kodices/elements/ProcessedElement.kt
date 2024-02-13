package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.inputvalidation.Validation
import kotlinx.serialization.Serializable

@Serializable
open class ProcessedElement(
    override val type: String,
    override val nestedElements: List<ProcessedElement> = emptyList(),
    val id: String,
    val index: Int = 0,
    val text: String? = null,
    val textSecondary: String? = null,
    override val actions: List<Action> = emptyList(),
    val jsonValues: ProcessedValues = mutableMapOf(),
    open val style: String? = null,
    open val validation: Validation? = null,
    open val requiresValidElements: List<String> = emptyList(),
    override val enabled: Boolean = true,
    // TODO: add a generic way to represent conditionals to toggle enablement or visibility
) : Element {
    override fun toString(): String {
        return "ProcessedElement { type: $type, id: $id, text: $text, jsonValues: $jsonValues }"
    }

    open fun copy(
        id: String = this.id,
        index: Int = this.index,
        nestedElements: List<ProcessedElement> = this.nestedElements,
        text: String? = this.text,
        textSecondary: String? = this.textSecondary,
        actions: List<Action> = this.actions,
        jsonValues: ProcessedValues = this.jsonValues,
        style: String? = this.style,
        validation: Validation? = this.validation,
        requiresValidElements: List<String> = this.requiresValidElements,
        enabled: Boolean = this.enabled,
    ): ProcessedElement {
        return ProcessedElement(
            type = type,
            nestedElements = nestedElements,
            id = id,
            index = index,
            text = text,
            textSecondary = textSecondary,
            actions = actions,
            jsonValues = jsonValues,
            style = style,
            validation = validation,
            requiresValidElements = requiresValidElements,
            enabled = enabled
        )
    }
}
