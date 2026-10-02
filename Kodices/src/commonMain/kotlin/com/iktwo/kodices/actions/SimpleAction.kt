package com.iktwo.kodices.actions

import kotlinx.serialization.json.JsonElement

public open class SimpleAction(
    override val type: String,
    public val jsonValues: Map<String, JsonElement?> = emptyMap(),
) : Action {
    public constructor(type: String) : this(type, emptyMap())

    override fun toString(): String = "SimpleAction(type=$type, jsonValues=$jsonValues)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleAction) return false
        return type == other.type && jsonValues == other.jsonValues
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + jsonValues.hashCode()
        return result
    }
}
