package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * typealias that defines a function to create a ProcessedElement.
 *
 * @param type The type of the element.
 * @param id A unique identifier for the element.
 * @param processedValues A map of processed values associated with the element.
 * @param nestedElements A list of nested elements within this element.
 * @param actions A list of actions associated with the element.
 * @param json The JSON serializer/deserializer used for processing elements.
 * @return A ProcessedElement instance.
 */
typealias ElementBuilder = (
    type: String,
    id: String,
    processedValues: Map<String, JsonElement?>,
    nestedElements: List<ProcessedElement>,
    actions: List<Action>,
    json: Json,
) -> ProcessedElement

/**
 * An interface representing an element descriptor.
 *
 * @property type A string that identifies the type of element.
 * @property builder An ElementBuilder function used to create a ProcessedElement from this descriptor.
 */
interface ElementDescriptor {
    val type: String
    val builder: ElementBuilder
}
