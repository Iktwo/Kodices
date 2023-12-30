package com.iktwo.kodices.sampleapp.data

import com.iktwo.piktographs.data.Countdown

interface CountdownRepository {
    suspend fun get(): List<Countdown>

    suspend fun add(element: Countdown)

    fun isEmpty(): Boolean
}
