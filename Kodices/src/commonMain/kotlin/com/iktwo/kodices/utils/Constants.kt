package com.iktwo.kodices.utils

import kotlinx.serialization.json.Json

public object Constants {
    public const val ACTION: String = "action"
    public const val ACTIONS: String = "actions"
    public const val CONSTANTS: String = "constants"
    public const val CONTENT: String = "content"
    public const val DATA: String = "data"
    public const val ELEMENT: String = "element"
    public const val ELEMENTS: String = "elements"
    public const val EXPAND_WITH_PROCESSOR: String = "expandWithProcessor"
    public const val EXPAND_WITH_PROCESSORS: String = "expandWithProcessors"
    public const val ID: String = "id"
    public const val NESTED_ELEMENTS: String = "nestedElements"
    public const val PROCESSORS: String = "processors"
    public const val TEXT_KEY: String = "text"
    public const val TEXT_SECONDARY_KEY: String = "textSecondary"
    public const val TYPE: String = "type"
    public const val STYLE: String = "style"
    public const val VALIDATION_KEY: String = "validation"
    public const val ENABLED_KEY: String = "enabled"
    public const val VISIBLE_KEY: String = "visible"
    public const val REQUIRES_VALID_ELEMENTS_KEY: String = "requiresValidElements"

    public val GENERIC_ELEMENT_KEYS: List<String> = listOf(
        TEXT_KEY,
        TEXT_SECONDARY_KEY,
        STYLE,
        ENABLED_KEY,
        VISIBLE_KEY,
        VALIDATION_KEY,
        REQUIRES_VALID_ELEMENTS_KEY,
    )

    public val json: Json = Json { prettyPrint = false }
}
