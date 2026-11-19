package com.iktwo.kodex.renderer.render

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * The data a screen's processors run against.
 *
 * Time comes from an injected [Clock] and is truncated to the minute, so renders inside the same
 * minute see identical data (and produce identical bytes), and tests can pin it.
 */
internal class ScreenData(
    private val clock: Clock,
    private val zoneId: ZoneId,
    private val locale: Locale,
) {
    /** Values exposed to screens under `now`, e.g. `{"type":"path","elements":["now","date"]}`. */
    fun now(): JsonObject {
        val now = ZonedDateTime.now(clock.withZone(zoneId)).truncatedTo(ChronoUnit.MINUTES)

        fun format(pattern: String) = DateTimeFormatter.ofPattern(pattern, locale).format(now)

        return buildJsonObject {
            put("iso", now.toOffsetDateTime().toString())
            put("date", format("EEEE, MMMM d, yyyy"))
            put("dateShort", format("MMM d"))
            put("weekday", format("EEEE"))
            put("time", format("HH:mm"))
            put("time12", format("h:mm a"))
            put("year", format("yyyy"))
        }
    }

    /**
     * Merges `now` into the screen's own data. Screen data wins on a key clash, so a screen can pin
     * `now` for a demo.
     */
    fun merge(screenData: JsonElement?): JsonObject {
        val base = mutableMapOf<String, JsonElement>(NOW_KEY to now())
        when (screenData) {
            is JsonObject -> base.putAll(screenData)
            null -> Unit
            else -> base[SCREEN_DATA_KEY] = screenData
        }
        return JsonObject(base)
    }

    /** A short stable key for the minute the data describes, used for cache keys. */
    fun minuteKey(): String = (now()["iso"] as JsonPrimitive).content

    companion object {
        const val NOW_KEY = "now"

        /** Where non-object screen data (e.g. a bare array) is exposed. */
        const val SCREEN_DATA_KEY = "data"
    }
}
