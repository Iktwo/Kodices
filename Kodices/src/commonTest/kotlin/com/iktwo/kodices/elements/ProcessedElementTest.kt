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
}
