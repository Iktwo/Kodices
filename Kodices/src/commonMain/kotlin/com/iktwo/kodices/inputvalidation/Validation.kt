package com.iktwo.kodices.inputvalidation

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
public data class Validation(
    val length: RangeValidation? = null,
    val regex: String? = null,
    val nonBlank: Boolean? = null,
) {
    @Transient
    private val compiledRegex: Regex? = regex?.let { pattern ->
        runCatching { Regex(pattern) }.getOrNull()
    }

    public fun validate(text: String?): Boolean {
        if (length == null && regex == null && nonBlank == null) {
            return true
        }

        if (text == null) {
            return false
        } else {
            length?.let { lengthValidation ->
                if (text.length.toUInt() < lengthValidation.min || text.length.toUInt() > lengthValidation.max) {
                    return false
                }
            }

            if (regex != null) {
                val matches = compiledRegex?.let {
                    runCatching { it.matches(text) }.getOrDefault(false)
                } ?: false
                if (!matches) {
                    return false
                }
            }

            nonBlank?.let { nonBlankValidation ->
                if (nonBlankValidation && text.isBlank()) {
                    return false
                }
            }

            return true
        }
    }
}

/**
 * Data class that represents a range for validation.
 */
@Serializable
public data class RangeValidation(
    val min: UInt = 0u,
    val max: UInt = UInt.MAX_VALUE,
)
