package com.iktwo.kodices.elements

import com.iktwo.kodices.inputvalidation.Validation
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asBooleanOrNull
import com.iktwo.kodices.utils.asJSONArrayOrNull
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

data class CommonElementProperties(
    val text: String?,
    val textSecondary: String?,
    val style: String?,
    val validation: Validation?,
    val requiresValidElements: List<String>,
    val enabled: Boolean
)

fun Map<String, JsonElement?>.toCommonElementProperties(json: Json): CommonElementProperties {
    val text = get(Constants.TEXT_KEY)?.asStringOrNull()
    val textSecondary = get(Constants.TEXT_SECONDARY_KEY)?.asStringOrNull()
    val style = get(Constants.STYLE)?.asStringOrNull()
    val enabled = get(Constants.ENABLED_KEY)?.asBooleanOrNull() ?: true

    val validation = get(Constants.VALIDATION_KEY)?.let {
        json.decodeFromJsonElement<Validation>(it)
    }

    val requiresValidElements = get(Constants.REQUIRES_VALID_ELEMENTS_KEY)?.asJSONArrayOrNull()?.mapNotNull { it.asStringOrNull() }

    return CommonElementProperties(
        text = text,
        textSecondary = textSecondary,
        style = style,
        validation = validation,
        requiresValidElements = requiresValidElements ?: emptyList(),
        enabled = enabled
    )
}
