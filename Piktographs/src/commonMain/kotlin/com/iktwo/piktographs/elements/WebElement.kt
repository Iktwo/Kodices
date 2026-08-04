package com.iktwo.piktographs.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.serialization.json.JsonElement

public class WebElement(
    id: String,
    jsonValues: Map<String, JsonElement?>,
    public val url: String,
    public val jsOnLoad: String?,
    nestedElements: List<ProcessedElement> = emptyList(),
    actions: List<Action> = emptyList(),
) : ProcessedElement(type = type, nestedElements = nestedElements, id = id, actions = actions) {
    public companion object : ElementDescriptor {
        override val type: String = "web"

        private const val URL = "url"
        private const val JS_ON_LOAD_KEY = "jsOnLoad"

        override val builder: ElementBuilder = { _, id, processedValues, nestedElements, actions, _ ->
            val url = processedValues[URL]?.asStringOrNull()
            val jsOnLoad = processedValues[JS_ON_LOAD_KEY]?.asStringOrNull()

            if (url.isNullOrBlank()) {
                throw Exception("Unable to create a WebElement without a url")
            }

            WebElement(id, processedValues, url, jsOnLoad, nestedElements, actions)
        }
    }
}
