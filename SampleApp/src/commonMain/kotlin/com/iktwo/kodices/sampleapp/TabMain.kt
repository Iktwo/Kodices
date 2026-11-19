@file:OptIn(ExperimentalTime::class)

package com.iktwo.kodices.sampleapp

import androidx.compose.runtime.compositionLocalOf
import com.iktwo.kodices.actions.Action
import com.iktwo.kodices.actions.ActionPerformer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val emptyActionPerformer = object : ActionPerformer {
    override fun onAction(action: Action) {
    }
}

val LocalLastSecond = compositionLocalOf { Clock.System.now() }
