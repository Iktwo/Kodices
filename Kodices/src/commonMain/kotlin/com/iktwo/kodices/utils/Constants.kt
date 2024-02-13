package com.iktwo.kodices.utils

import kotlinx.serialization.json.Json

object Constants {
    const val ACTION = "action"
    const val ACTIONS = "actions"
    const val CONSTANTS = "constants"
    const val CONTENT = "content"
    const val DATA = "data"
    const val ELEMENT = "element"
    const val ELEMENTS = "elements"
    const val EXPAND_WITH_PROCESSOR = "expandWithProcessor"
    const val EXPAND_WITH_PROCESSORS = "expandWithProcessors"
    const val ID = "id"
    const val NESTED_ELEMENTS = "nestedElements"
    const val PROCESSORS = "processors"
    const val TEXT_KEY = "text"
    const val TEXT_SECONDARY_KEY = "textSecondary"
    const val TYPE = "type"
    const val STYLE = "style"
    const val VALIDATION_KEY = "validation"
    const val ENABLED_KEY = "enabled"
    const val REQUIRES_VALID_ELEMENTS_KEY = "requiresValidElements"

    val GENERIC_ELEMENT_KEYS = listOf(
        TEXT_KEY,
        TEXT_SECONDARY_KEY,
        STYLE,
        ENABLED_KEY,
        VALIDATION_KEY,
        REQUIRES_VALID_ELEMENTS_KEY
    )

    val jsonPrettyPrinter = Json { prettyPrint = true }
    val json = Json { prettyPrint = false }
}
