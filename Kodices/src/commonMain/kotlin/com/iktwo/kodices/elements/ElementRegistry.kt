package com.iktwo.kodices.elements

/**
 * A process-global registry for [ElementBuilder] instances.
 */
@Deprecated(
    "Global mutable state shared by every parser. Build a KodicesRegistry and pass it to " +
        "KodicesParser instead; this is removed in 1.0.",
)
public object ElementRegistry {
    private val elements: MutableMap<String, ElementBuilder> = mutableMapOf()

    /**
     * Adds a single element descriptor to the registry.
     *
     * @param descriptor The [ElementDescriptor] containing the type and builder for the element.
     */
    public fun addElement(descriptor: ElementDescriptor) {
        elements[descriptor.type] = descriptor.builder
    }

    /**
     * Adds multiple element descriptors to the registry.
     *
     * @param descriptors A list of [ElementDescriptor] instances to add.
     */
    public fun addElements(descriptors: List<ElementDescriptor>) {
        elements.putAll(descriptors.map { Pair(it.type, it.builder) })
    }

    /**
     * Retrieves an [ElementBuilder] instance based on the specified type.
     *
     * @param type The type of element to retrieve.
     * @return The [ElementBuilder] instance, or `null` if the type is not found.
     */
    public fun getElement(type: String): ElementBuilder? {
        return elements[type]
    }
}
