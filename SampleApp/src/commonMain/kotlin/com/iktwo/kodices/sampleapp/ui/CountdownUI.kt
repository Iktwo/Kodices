package com.iktwo.kodices.sampleapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.iktwo.piktographs.elements.CountdownElement
import com.iktwo.piktographs.elements.CountdownStyle
import com.iktwo.kodices.sampleapp.theme.SPACING
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isDistantPast
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toInstant

@Composable
fun CountdownUI(
    countdownElement: CountdownElement,
    instant: Instant = Clock.System.now(),
) {
    val timeFrames = mutableListOf<String>()

    val timeDifference = instant.periodUntil(
        countdownElement.target.toInstant(TimeZone.currentSystemDefault()),
        TimeZone.currentSystemDefault(),
    )

    val isTargetInThePast = countdownElement.target.toInstant(TimeZone.currentSystemDefault()) < Clock.System.now()

    Column(modifier = Modifier.fillMaxWidth().padding(SPACING)) {
        countdownElement.text?.let {
            Text(
                text = it,
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(textDecoration = if (isTargetInThePast) TextDecoration.LineThrough else null),
            )
        }

        when (countdownElement.style) {
            CountdownStyle.SHORT -> {
                if (timeDifference.years != 0) {
                    timeFrames.add("${timeDifference.years} years")
                } else if (timeDifference.months != 0) {
                    timeFrames.add("${timeDifference.months} months")
                } else if (timeDifference.days != 0) {
                    timeFrames.add("${timeDifference.days} days")
                } else if (timeDifference.hours != 0) {
                    timeFrames.add("${timeDifference.hours} hours")
                } else if (timeDifference.minutes != 0) {
                    timeFrames.add("${timeDifference.minutes} minutes")
                } else if (timeDifference.seconds != 0) {
                    timeFrames.add("${timeDifference.seconds} seconds")
                }
            }

            CountdownStyle.DAYS_HOURS_MINUTES_SECONDS -> {
                if (timeDifference.years != 0) {
                    timeFrames.add("${timeDifference.years} years")
                }

                if (timeDifference.months != 0) {
                    timeFrames.add("${timeDifference.months} months")
                }

                if (timeDifference.days != 0) {
                    timeFrames.add("${timeDifference.days} days")
                }

                if (timeDifference.hours != 0) {
                    timeFrames.add("${timeDifference.hours} hours")
                }

                if (timeDifference.minutes != 0) {
                    timeFrames.add("${timeDifference.minutes} minutes")
                }

                if (timeDifference.seconds != 0) {
                    timeFrames.add("${timeDifference.seconds} seconds")
                }
            }
        }

        Text(
            text = timeFrames.joinToString(separator = ", "),
            color = Color.Black,
            fontSize = 18.sp,
            style = TextStyle(textDecoration = if (isTargetInThePast) TextDecoration.LineThrough else null),
        )
    }
}
