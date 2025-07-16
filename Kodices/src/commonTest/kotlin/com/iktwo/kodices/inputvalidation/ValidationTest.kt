package com.iktwo.kodices.inputvalidation

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationTest {
    @Test
    fun testDeserializeEmptyValidation() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{}",
        )

        assertNull(validation.nonBlank)
        assertNull(validation.length)
        assertNull(validation.regex)
    }

    @Test
    fun testDeserializeValidationWithOneValue() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"nonBlank\": true}",
        )

        assertTrue(validation.nonBlank ?: false)
    }

    @Test
    fun testDeserializeValidationWithMultipleValues() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"nonBlank\": true,\"length\": {\"min\": 10}}",
        )

        assertTrue(validation.nonBlank ?: false)
        assertEquals(10u, validation.length?.min)
    }

    @Test
    fun testValidateEmptyValidation() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{}",
        )

        assertTrue(validation.validate("test"))
        assertTrue(validation.validate(""))
        assertTrue(validation.validate(null))
    }

    @Test
    fun testValidateNonBlank() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"nonBlank\": true}",
        )

        assertTrue(validation.validate("test"))
        assertEquals(false, validation.validate(""))
    }

    @Test
    fun testValidateMinLength() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"length\": {\"min\": 3}}",
        )

        assertEquals(true, validation.validate("123"))
        assertEquals(false, validation.validate("12"))
        assertEquals(false, validation.validate(""))
        assertEquals(false, validation.validate(null))
        assertEquals(true, validation.validate("1234"))
    }

    @Test
    fun testValidateMaxLength() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"length\": {\"max\": 3}}",
        )

        assertEquals(true, validation.validate("123"))
        assertEquals(true, validation.validate("1"))
        assertEquals(true, validation.validate(""))
        assertEquals(false, validation.validate(null))
        assertEquals(false, validation.validate("1234"))
    }

    @Test
    fun testValidateMinMaxLength() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"length\": {\"min\": 2, \"max\": 3}}",
        )

        assertEquals(true, validation.validate("123"))
        assertEquals(true, validation.validate("12"))
        assertEquals(false, validation.validate("1"))
        assertEquals(false, validation.validate(""))
        assertEquals(false, validation.validate(null))
        assertEquals(false, validation.validate("1234"))
    }

    @Test
    fun testValidateEqualMinMaxLength() {
        val validation = Json.decodeFromString(
            Validation.serializer(),
            "{\"length\": {\"min\": 3, \"max\": 3}}",
        )

        assertEquals(true, validation.validate("123"))
        assertEquals(false, validation.validate("12"))
        assertEquals(false, validation.validate("1"))
        assertEquals(false, validation.validate(""))
        assertEquals(false, validation.validate(null))
        assertEquals(false, validation.validate("1234"))
    }
}
