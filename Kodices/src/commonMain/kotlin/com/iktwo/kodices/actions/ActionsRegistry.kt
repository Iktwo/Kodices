package com.iktwo.kodices.actions

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonObject

/**
 * A process-global registry for [ActionBuilder] instances.
 */
@Deprecated(
    "Global mutable state shared by every parser. Build a KodicesRegistry and pass it to " +
        "KodicesParser instead; this is removed in 1.0.",
)
public object ActionsRegistry {
    private val actions: MutableMap<String, ActionBuilder> = mutableMapOf()

    public fun fromJsonObject(jsonObject: JsonObject): ActionBuilder? {
        val type = jsonObject[Constants.TYPE]?.asStringOrNull() ?: ""
        return actions[type]
    }

    public fun addAction(descriptor: ActionDescriptor) {
        actions[descriptor.type] = descriptor.builder
    }

    public fun addActions(descriptors: List<ActionDescriptor>) {
        actions.putAll(descriptors.map { Pair(it.type, it.builder) })
    }

    public fun getAction(type: String): ActionBuilder? {
        return actions[type]
    }
}
