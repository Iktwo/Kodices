package com.iktwo.kodices.elements

import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonElement

data class CommonElementProperties(
    val text: String?,
    val textSecondary: String?,
    val style: String?
)

fun Map<String, JsonElement?>.toCommonElementProperties(): CommonElementProperties {
    val text = get(Constants.TEXT_KEY)?.asStringOrNull()
    val textSecondary = get(Constants.TEXT_SECONDARY_KEY)?.asStringOrNull()
    val style = get(Constants.STYLE)?.asStringOrNull()

    return CommonElementProperties(text = text, textSecondary = textSecondary, style = style)
}
