package com.iktwo.kodices.content

import com.iktwo.kodices.elements.ProcessedElement
import kotlinx.serialization.Serializable

/**
 * Class that holds a complete model that can be represented on the UI.
 */
@Serializable
data class Content(
    val elements: List<ProcessedElement>,
)
