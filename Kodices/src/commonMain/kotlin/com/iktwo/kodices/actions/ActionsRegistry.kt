package com.iktwo.kodices.actions

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonObject

object ActionsRegistry {
    private val actions: MutableMap<String, ActionBuilder> = mutableMapOf()

    fun fromJsonObject(jsonObject: JsonObject): ActionBuilder? {
        val type = jsonObject[Constants.TYPE]?.asStringOrNull() ?: ""
        return actions[type]
    }

    fun addAction(descriptor: ActionDescriptor) {
        actions[descriptor.type] = descriptor.builder
    }

    fun addActions(descriptors: List<ActionDescriptor>) {
        actions.putAll(descriptors.map { Pair(it.type, it.builder) })
    }

    fun getAction(type: String): ActionBuilder? {
        return actions[type]
    }
}
