package com.iktwo.kodices.elements

import com.iktwo.kodices.sampleProcessedElementSource
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class ProcessedElementTest {
    @Test
    fun testDeserializeProcessedElement() {
        assertTrue(
            Json.decodeFromString(
                Element.Companion,
                sampleProcessedElementSource.toString(),
            ) is ProcessedElement,
        )
    }

    @Test
    fun testProcessedElementStructuralEquality() {
        val element1 = ProcessedElement(
            type = "row",
            id = "item_1",
            text = "Title",
            textSecondary = "Subtitle",
        )

        val element2 = ProcessedElement(
            type = "row",
            id = "item_1",
            text = "Title",
            textSecondary = "Subtitle",
        )

        val element3 = ProcessedElement(
            type = "row",
            id = "item_2",
            text = "Different",
        )

        kotlin.test.assertEquals(element1, element2)
        kotlin.test.assertEquals(element1.hashCode(), element2.hashCode())
        kotlin.test.assertNotEquals(element1, element3)
    }
}
