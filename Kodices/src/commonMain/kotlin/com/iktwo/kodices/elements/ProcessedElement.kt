package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
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
    open val style: String? = null
) : Element {
    override fun toString(): String {
        return "ProcessedElement { type: $type, id: $id, text: $text, jsonValues: $jsonValues }"
    }
}
