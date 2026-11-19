@file:OptIn(ExperimentalTime::class)

package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.iktwo.kodices.sampleapp.theme.SPACING
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.CountdownStyle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun CountdownUI(
    countdownElement: CountdownElement,
    instant: Instant = Clock.System.now(),
) {
    val target = countdownElement.target.toInstant(TimeZone.currentSystemDefault())
    val timeDifference = instant.periodUntil(target, TimeZone.currentSystemDefault())

    // Against [instant], like the time left, so the two cannot disagree.
    val isTargetInThePast = target < instant

    Column(modifier = Modifier.fillMaxWidth().padding(SPACING)) {
        countdownElement.text?.let {
            Text(
                text = it,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(textDecoration = if (isTargetInThePast) TextDecoration.LineThrough else null),
            )
        }

        // Largest unit first. A target in the past counts up from it, so the amounts are negative.
        val amounts = listOf(
            timeDifference.years to "year",
            timeDifference.months to "month",
            timeDifference.days to "day",
            timeDifference.hours to "hour",
            timeDifference.minutes to "minute",
            timeDifference.seconds to "second",
        ).filter { (amount, _) -> amount != 0 }

        val shown = when (countdownElement.elementStyle) {
            CountdownStyle.SHORT -> amounts.take(1)
            CountdownStyle.DAYS_HOURS_MINUTES_SECONDS -> amounts
        }

        val label = shown.joinToString(separator = ", ") { (amount, unit) -> quantity(abs(amount), unit) }

        Text(
            text = when {
                label.isEmpty() -> "Now"
                isTargetInThePast -> "$label ago"
                else -> label
            },
            fontSize = 18.sp,
            style = TextStyle(textDecoration = if (isTargetInThePast) TextDecoration.LineThrough else null),
        )
    }
}

/** "1 month", "2 months". */
internal fun quantity(
    amount: Int,
    unit: String,
): String = if (amount == 1) "1 $unit" else "$amount ${unit}s"
