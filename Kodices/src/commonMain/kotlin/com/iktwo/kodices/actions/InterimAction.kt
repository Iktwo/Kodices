package com.iktwo.kodices.actions

import com.iktwo.kodices.kodicesContext
import com.iktwo.kodices.utils.Constants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
public open class InterimAction(
    override val type: String,
    private val jsonSource: JsonElement,
) : Action {
    @Deprecated(
        "Resolves against the deprecated global registry. Use process(data, json) so the action is " +
            "resolved against the parser's own registry.",
        ReplaceWith("process(data, com.iktwo.kodices.utils.Constants.json)"),
    )
    public fun process(data: JsonElement): Action = process(data, Constants.json)

    /**
     * Resolves this action against the registry carried by [json].
     *
     * Two overloads rather than a defaulted parameter: adding a default to the existing single-argument
     * function would change its signature and break binary compatibility.
     */
    public fun process(
        data: JsonElement,
        json: Json,
    ): Action =
        json.kodicesContext.registry
            .actionBuilder(type)
            ?.invoke(jsonSource, data)
            ?: SimpleAction(type)
}
