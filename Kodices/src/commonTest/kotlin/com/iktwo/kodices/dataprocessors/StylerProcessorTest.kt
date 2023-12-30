package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.asString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StylerProcessorTest {
    @Test
    fun testStylerProcessorDeserializationEmptyString() {
        val processor = Json.decodeFromString(
            StylerProcessor.Companion.serializer(),
            "{\"type\":\"${StylerProcessor.TYPE}\",\"element\": \"\"}",
        )
        assertNotNull(processor)
        assertEquals("", processor.element)
    }

    @Test
    fun testStylerProcessorUppercase() {
        val processor = Json.decodeFromString(
            StylerProcessor.Companion.serializer(),
            "{\"type\":\"${StylerProcessor.TYPE}\",\"element\": \"uppercase\"}",
        )
        assertNotNull(processor)
        assertEquals("SOMETHING", processor.process(JsonPrimitive("SomethinG"))?.asString())
    }

    @Test
    fun testStylerProcessorLowercase() {
        val processor = Json.decodeFromString(
            StylerProcessor.Companion.serializer(),
            "{\"type\":\"${StylerProcessor.TYPE}\",\"element\": \"lowercase\"}",
        )
        assertNotNull(processor)
        assertEquals("something", processor.process(JsonPrimitive("SomethinG"))?.asString())
    }

    @Test
    fun testStylerProcessorUnknown() {
        val processor = Json.decodeFromString(
            StylerProcessor.Companion.serializer(),
            "{\"type\":\"${StylerProcessor.TYPE}\",\"element\": \"lskjlwiuo\"}",
        )
        assertNotNull(processor)
        assertEquals("SomethinG", processor.process(JsonPrimitive("SomethinG"))?.asString())
    }
}
