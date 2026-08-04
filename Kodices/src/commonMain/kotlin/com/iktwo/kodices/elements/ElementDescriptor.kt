package com.iktwo.kodices.elements

import com.iktwo.kodices.actions.Action
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * typealias that defines a function to create a ProcessedElement.
 *
 * Note: the identifier parameter is deliberately not called `id`. Kotlin/Native exports this
 * function type to Objective-C as a block, and a parameter named `id` shadows the Objective-C `id`
 * keyword used later in the same signature, which makes the generated header fail to compile.
 *
 * @param type The type of the element.
 * @param elementId A unique identifier for the element.
 * @param processedValues A map of processed values associated with the element.
 * @param nestedElements A list of nested elements within this element.
 * @param actions A list of actions associated with the element.
 * @param json The JSON serializer/deserializer used for processing elements.
 * @return A ProcessedElement instance.
 */
public typealias ElementBuilder = (
    type: String,
    elementId: String,
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
public interface ElementDescriptor {
    public val type: String
    public val builder: ElementBuilder
}
