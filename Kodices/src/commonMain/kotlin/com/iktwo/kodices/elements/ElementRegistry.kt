package com.iktwo.kodices.elements

object ElementRegistry {
    private val elements: MutableMap<String, ElementBuilder> = mutableMapOf()

    fun addElement(descriptor: ElementDescriptor) {
        elements[descriptor.type] = descriptor.builder
    }

    fun addElements(descriptors: List<ElementDescriptor>) {
        elements.putAll(descriptors.map { Pair(it.type, it.builder) })
    }

    fun getElement(type: String): ElementBuilder? {
        return elements[type]
    }
}
