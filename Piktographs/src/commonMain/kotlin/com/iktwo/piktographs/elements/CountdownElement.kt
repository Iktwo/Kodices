package com.iktwo.piktographs.elements

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonElement

public enum class CountdownStyle {
    SHORT,
    DAYS_HOURS_MINUTES_SECONDS,
    ;

    public companion object {
        public fun fromString(value: String): CountdownStyle {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: DAYS_HOURS_MINUTES_SECONDS
        }
    }
}

public class CountdownElement(
    id: String,
    nestedElements: List<ProcessedElement> = emptyList(),
    jsonValues: Map<String, JsonElement?>,
    title: String? = null,
    actions: List<Action> = emptyList(),
    public val target: LocalDateTime,
    public val elementStyle: CountdownStyle,
) : ProcessedElement(
        type = type,
        nestedElements = nestedElements,
        id = id,
        text = title,
        actions = actions,
        jsonValues = jsonValues,
    ) {
    public companion object : ElementDescriptor {
        override val type: String = "countdown"

        private const val TARGET = "target"

        public const val STYLE: String = "style"

        override val builder: ElementBuilder = { _, id, processedValues, nestedElements, actions, _ ->
            val targetValue = processedValues[TARGET]?.asStringOrNull() ?: ""
            val styleValue = processedValues[STYLE]?.asStringOrNull() ?: ""
            val title = processedValues[Constants.TEXT_KEY]?.asStringOrNull()
            var target: LocalDateTime? = null

            try {
                target = LocalDateTime.parse(targetValue)
            } catch (e: IllegalArgumentException) {
                KodicesParser.logger.error("Unable to parse date: $targetValue")
                throw Exception("Unable to create CountdownElement")
            }

            CountdownElement(
                id = id,
                nestedElements = nestedElements,
                jsonValues = processedValues,
                actions = actions,
                target = target,
                title = title,
                elementStyle = CountdownStyle.fromString(styleValue),
            )
        }
    }
}
