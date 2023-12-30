package com.iktwo.kodices.dataprocessors

import com.iktwo.kodices.utils.Constants
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DataProcessorRegistryTest {
    @Test
    fun testGetBuilderFromJsonObject() {
        val builder =
            DataProcessorRegistry.fromJsonObject(
                buildJsonObject {
                    put(Constants.TYPE, JsonPrimitive(JSONDrillerProcessor.TYPE))
                },
            )

        assertNotNull(builder)
    }

    @Test
    fun testGetBuilderFromJsonObjectWithInvalidValue() {
        val builder =
            DataProcessorRegistry.fromJsonObject(
                buildJsonObject {
                    put(Constants.TYPE, JsonPrimitive("utoijswe"))
                },
            )

        assertNull(builder)
    }
}
