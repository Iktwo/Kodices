package com.iktwo.piktographs.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Countdown(
    val name: String? = null,
    val message: String? = null,
    val target: LocalDateTime,
)
