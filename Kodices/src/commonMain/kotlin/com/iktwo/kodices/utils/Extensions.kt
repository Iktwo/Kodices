package com.iktwo.kodices.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.asJSONArrayOrNull(): JsonArray? {
    return try {
        jsonArray
    } catch (e: Exception) {
        null
    }
}

fun JsonElement.asJSONObjectOrNull(): JsonObject? {
    return try {
        jsonObject
    } catch (e: Exception) {
        null
    }
}

fun JsonElement.asStringOrNull(): String? {
    return if (this is JsonPrimitive && jsonPrimitive.isString) {
        content
    } else {
        null
    }
}

fun JsonElement.asIntOrNull(): Int? {
    return if (this is JsonPrimitive && jsonPrimitive.intOrNull != null) {
        intOrNull
    } else {
        null
    }
}

fun JsonElement.asString(): String {
    return if (this is JsonArray) {
        jsonArray.joinToString {
            it.asStringOrNull() ?: Constants.json.encodeToString(JsonElement.serializer(), it)
        }
    } else if (this is JsonPrimitive && this.isString) {
        content
    } else {
        Constants.json.encodeToString(JsonElement.serializer(), this)
    }
}

fun JsonObject.asMap(): Map<String, JsonElement?> {
    return entries.associate { it.key to (if (it.value == JsonNull) null else it.value) }
}
