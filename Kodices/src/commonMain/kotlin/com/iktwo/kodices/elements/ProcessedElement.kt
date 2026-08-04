package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.inputvalidation.Validation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public open class ProcessedElement(
    override val type: String,
    override val nestedElements: List<ProcessedElement> = emptyList(),
    public val id: String,
    public val index: Int = 0,
    public val text: String? = null,
    public val textSecondary: String? = null,
    override val actions: List<Action> = emptyList(),
    public val jsonValues: Map<String, JsonElement?> = emptyMap(),
    public open val style: String? = null,
    public open val validation: Validation? = null,
    /**
     * Used to define if this element will be enabled only if the list in here passes their validations.
     *
     * This is useful for buttons in forms.
     */
    public open val requiresValidElements: List<String> = emptyList(),
    override val enabled: Boolean = true,
    override val visible: Boolean = true,
) : Element {
    override fun toString(): String {
        return "ProcessedElement { type: $type, id: $id, text: $text, jsonValues: $jsonValues }"
    }

    public open fun copy(
        id: String = this.id,
        index: Int = this.index,
        nestedElements: List<ProcessedElement> = this.nestedElements,
        text: String? = this.text,
        textSecondary: String? = this.textSecondary,
        actions: List<Action> = this.actions,
        jsonValues: Map<String, JsonElement?> = this.jsonValues,
        style: String? = this.style,
        validation: Validation? = this.validation,
        requiresValidElements: List<String> = this.requiresValidElements,
        enabled: Boolean = this.enabled,
        visible: Boolean = this.visible,
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
            enabled = enabled,
            visible = visible,
        )
    }
}
