package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import kotlinx.serialization.json.JsonElement

/**
 * typealias that defines a function that creates a ProcessedElement
 */
typealias ElementBuilder = (
    type: String,
    id: String,
    processedValues: Map<String, JsonElement?>,
    nestedElements: List<ProcessedElement>,
    actions: List<Action>,
) -> ProcessedElement

interface ElementDescriptor {
    val type: String
    val builder: ElementBuilder
}
