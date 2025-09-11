package com.iktwo.kodices.elements

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.sampleInterimElement
import com.iktwo.kodices.sampleInvalidDataProcessorInElement
import com.iktwo.kodices.sampleProcessedElementWithNestedElements
import com.iktwo.kodices.sampleRowElement
import com.iktwo.kodices.sampleUnknownDataProcessorInElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.Constants.json
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ElementTest {
    val kodicesParser = KodicesParser()

    @Test
    fun `Test that a ProcessedElement can be deserialized`() {
        val element = json.decodeFromJsonElement<Element>(sampleRowElement)
        assertNotNull(element)
        assertTrue(element is ProcessedElement)
        assertEquals("row", element.type)
        assertEquals("Test row subtitle", element.textSecondary)
        assertEquals("Test row", element.text)
    }

    @Test
    fun `Test that a ProcessedElement with nested elements can be deserialized`() {
        val element = json.decodeFromJsonElement<Element>(sampleProcessedElementWithNestedElements)
        assertNotNull(element)
        assertTrue(element is ProcessedElement)
        assertEquals("row", element.type)
        assertEquals("Top row", element.text)
        assertEquals("Test row", element.nestedElements.first().text)
        assertEquals("Top row", element.text)
        assertEquals(
            "Two level nested row",
            element.nestedElements
                .first()
                .nestedElements
                .first()
                .text,
        )
        assertEquals(
            "some_value",
            element.nestedElements
                .first()
                .nestedElements
                .first()
                .jsonValues["another_key"]
                ?.asStringOrNull(),
        )
    }

    @Test
    fun `Test that an InterimElement can be deserialized`() {
        val element = json.decodeFromJsonElement<Element>(sampleInterimElement)
        assertNotNull(element)
        assertTrue(element is InterimElement)
        assertEquals("row", element.type)
        assertEquals(3, element.processors?.get(Constants.TEXT_SECONDARY_KEY)?.size ?: 0)
    }

    @Test
    fun `Test invalid data processor`() {
        assertFailsWith<Exception> {
            json.decodeFromJsonElement<Element>(sampleInvalidDataProcessorInElement)
        }
    }

    @Test
    fun `Test unknown data processor`() {
        assertFailsWith<Exception> {
            json.decodeFromJsonElement<Element>(sampleUnknownDataProcessorInElement)
        }
    }
}
