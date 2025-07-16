package com.iktwo.piktographs.elements

import com.iktwo.kodices.Kodices
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.elements.ElementBuilder
import com.iktwo.kodices.elements.ElementDescriptor
import com.iktwo.kodices.elements.ProcessedElement
import com.iktwo.kodices.elements.ProcessedValues
import com.iktwo.kodices.utils.Constants
import com.iktwo.kodices.utils.asStringOrNull
import kotlinx.datetime.LocalDateTime

enum class CountdownStyle {
    SHORT,
    DAYS_HOURS_MINUTES_SECONDS,
    ;

    companion object {
        fun fromString(value: String): CountdownStyle {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: DAYS_HOURS_MINUTES_SECONDS
        }
    }
}

class CountdownElement(
    id: String,
    nestedElements: List<ProcessedElement> = emptyList(),
    jsonValues: ProcessedValues,
    title: String? = null,
    actions: List<Action> = emptyList(),
    val target: LocalDateTime,
    val elementStyle: CountdownStyle,
) : ProcessedElement(
        type = type,
        nestedElements = nestedElements,
        id = id,
        text = title,
        actions = actions,
        jsonValues = jsonValues,
    ) {
    companion object : ElementDescriptor {
        override val type = "countdown"

        private const val TARGET = "target"

        const val STYLE = "style"

        override val builder: ElementBuilder = { _, id, processedValues, nestedElements, actions, _ ->
            val targetValue = processedValues[TARGET]?.asStringOrNull() ?: ""
            val styleValue = processedValues[STYLE]?.asStringOrNull() ?: ""
            val title = processedValues[Constants.TEXT_KEY]?.asStringOrNull()
            var target: LocalDateTime? = null

            try {
                target = LocalDateTime.parse(targetValue)
            } catch (e: IllegalArgumentException) {
                Kodices.logger.error("Unable to parse date: $targetValue")
                throw Exception("Unable to create CountdownElement")
            }

            CountdownElement(
                id = id,
                nestedElements = nestedElements,
                jsonValues = processedValues.toMutableMap(),
                actions = actions,
                target = target,
                title = title,
                elementStyle = CountdownStyle.fromString(styleValue),
            )
        }
    }
}
