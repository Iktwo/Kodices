package com.iktwo.kodices.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
open class InterimAction(override val type: String) : Action {
    fun process(data: JsonElement): ProcessedAction {
        // TODO(): Handle data
        return ProcessedAction(type)
    }
}
