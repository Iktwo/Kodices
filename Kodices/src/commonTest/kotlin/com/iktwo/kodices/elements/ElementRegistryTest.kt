package com.iktwo.kodices.elements

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ElementRegistryTest {
    @BeforeTest
    fun setup() {
        ElementRegistry.addElement(
            object : ElementDescriptor {
                override val type = "valid"

                override val builder: ElementBuilder = { _, id, _, nestedElements, _, _ ->
                    ProcessedElement(type = type, nestedElements = nestedElements, id = id)
                }
            },
        )
    }

    @Test
    fun testUnknownElementReturnsNull() {
        assertNull(ElementRegistry.getElement("invalid"))
    }

    @Test
    fun testGetElementWithRegisteredElement() {
        val builder = ElementRegistry.getElement("valid")

        assertNotNull(builder)
    }
}
