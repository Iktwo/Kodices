package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.asString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StringProcessorTest {
    @Test
    fun testStringProcessorDeserializationEmptyString() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"\"}",
        )
        assertNotNull(processor)
    }

    @Test
    fun testStringProcessorProcessEmptyString() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"\"}",
        )
        assertEquals("", processor.process(null).asString())
    }

    @Test
    fun testStringProcessorProcessTokenOnlyStringWithNullData() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"%\"}",
        )
        assertEquals("null", processor.process(null).asString())
    }

    @Test
    fun testStringProcessorProcessTokenOnlyString() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"%\"}",
        )
        assertEquals("8", processor.process(JsonPrimitive(8)).asString())
    }

    @Test
    fun testStringProcessorProcessString() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"Magic number: %\"}",
        )
        assertEquals("Magic number: 8", processor.process(JsonPrimitive(8)).asString())
    }

    @Test
    fun testStringProcessorMultiValueProcessString() {
        val processor = Json.decodeFromString(
            StringProcessor.Companion.serializer(),
            "{\"type\":\"${StringProcessor.TYPE}\",\"element\": \"Value: %1, Another value: %0\"}",
        )
        assertEquals(
            "Value: 8, Another value: 5",
            processor
                .process(
                    buildJsonArray {
                        add(JsonPrimitive(5))
                        add(JsonPrimitive(8))
                    },
                ).asString(),
        )
    }
}
