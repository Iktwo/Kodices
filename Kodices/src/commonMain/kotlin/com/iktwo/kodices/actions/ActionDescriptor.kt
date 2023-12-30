package com.iktwo.kodices.actions

import kotlinx.serialization.json.JsonElement

typealias ActionBuilder = (actionSource: JsonElement, data: JsonElement) -> Action

interface ActionDescriptor {
    val type: String
    val builder: ActionBuilder
}
