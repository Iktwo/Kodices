package com.iktwo.kodices.elements

import com.iktwo.kodices.sampleArrayData
import com.iktwo.kodices.sampleInterimElementSource
import com.iktwo.kodices.sampleInterimElementSourceWithAction
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterimElementTest {
    @Test
    fun testDeserializeInterimElement() {
        assertTrue(Json.decodeFromString(Element, sampleInterimElementSource.toString()) is InterimElement)
    }

    @Test
    fun testProcessInterimElement() {
        val element = Json.decodeFromString(Element, sampleInterimElementSource.toString())
        assertEquals("row", element.type)
        assertTrue(element.nestedElements.isEmpty())
        assertEquals("Melanerpes formicivorus", (element as InterimElement).process(0, data = sampleArrayData, json = Json { ignoreUnknownKeys = true }).first().text)
    }

    @Test
    fun testProcessInterimElementWithAction() {
        val element = Json.decodeFromString(Element, sampleInterimElementSourceWithAction.toString())
        assertEquals("row", element.type)
        assertTrue(element.nestedElements.isEmpty())
        assertEquals("sample", element.actions.first().type)
    }
}
