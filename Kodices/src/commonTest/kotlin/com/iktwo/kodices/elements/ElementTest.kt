package com.iktwo.kodices.elements

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.dataprocessors.DataProcessorException
import com.iktwo.kodices.sampleInterimElement
import com.iktwo.kodices.sampleInvalidDataProcessorInElement
import com.iktwo.kodices.sampleProcessedElementWithNestedElements
import com.iktwo.kodices.sampleRowElement
import com.iktwo.kodices.sampleScalarDataProcessorInElement
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
        assertFailsWith<DataProcessorException> {
            json.decodeFromJsonElement<Element>(sampleInvalidDataProcessorInElement)
        }
    }

    @Test
    fun `Test unknown data processor`() {
        assertFailsWith<DataProcessorException> {
            json.decodeFromJsonElement<Element>(sampleUnknownDataProcessorInElement)
        }
    }

    @Test
    fun `Test data processor that is neither an object nor an array`() {
        assertFailsWith<DataProcessorException> {
            json.decodeFromJsonElement<Element>(sampleScalarDataProcessorInElement)
        }
    }

    @Test
    fun `Test that nested constants and processors survive a parent that declares neither`() {
        val content = kodicesParser.parseJSONToContent(
            """
            {
              "elements": [
                {
                  "type": "grid",
                  "text": "Parent text",
                  "columns": 2,
                  "nestedElements": [
                    { "type": "text", "constants": { "text": "From constants" } },
                    {
                      "type": "wrapper",
                      "nestedElements": [
                        { "type": "text", "processors": { "text": { "type": "path", "element": "greeting" } } }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            """{ "greeting": "From data" }""",
        )

        val grid = assertNotNull(content).elements.single()
        assertEquals("grid", grid.type)
        assertEquals("grid_0", grid.id)
        assertEquals("Parent text", grid.text, "the parent keeps the values it declares directly")
        assertEquals(2, grid.jsonValues["columns"]?.let { json.decodeFromJsonElement<Int>(it) })
        assertEquals("From constants", grid.nestedElements[0].text)
        assertEquals("From data", grid.nestedElements[1].nestedElements.single().text)
        assertEquals(
            grid.nestedElements.map { it.id }.distinct().size,
            grid.nestedElements.size,
            "generated ids stay unique",
        )
    }
}
