package com.iktwo.piktographs.elements

import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.elements.ProcessedValues
import com.iktwo.kodices.utils.asStringOrNull

class WebElement(
    id: String,
    jsonValues: ProcessedValues,
    val url: String,
    val jsOnLoad: String?,
    nestedElements: List<ProcessedElement> = emptyList(),
    actions: List<Action> = emptyList(),
) : ProcessedElement(type = type, nestedElements = nestedElements, id = id, actions = actions) {
    companion object : ElementDescriptor {
        override val type = "web"

        private const val URL = "url"
        private const val JS_ON_LOAD_KEY = "jsOnLoad"

        override val builder: ElementBuilder = { _, id, processedValues, nestedElements, actions ->
            val url = processedValues[URL]?.asStringOrNull()
            val jsOnLoad = processedValues[JS_ON_LOAD_KEY]?.asStringOrNull()

            if (url.isNullOrBlank()) {
                throw Exception("Unable to create a WebElement without a url")
            }

            WebElement(id, processedValues.toMutableMap(), url, jsOnLoad, nestedElements, actions)
        }
    }
}
