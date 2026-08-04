package com.iktwo.kodices.actions

import kotlinx.serialization.json.JsonElement

public typealias ActionBuilder = (actionSource: JsonElement, data: JsonElement) -> Action

public interface ActionDescriptor {
    public val type: String
    public val builder: ActionBuilder
}
