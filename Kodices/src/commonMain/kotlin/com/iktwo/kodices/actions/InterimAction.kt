package com.iktwo.kodices.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
open class InterimAction(override val type: String, private val jsonSource: JsonElement) : Action {
    fun process(data: JsonElement): Action {
        return ActionsRegistry.getAction(type)?.invoke(jsonSource, data) ?: SimpleAction(type)
    }
}
