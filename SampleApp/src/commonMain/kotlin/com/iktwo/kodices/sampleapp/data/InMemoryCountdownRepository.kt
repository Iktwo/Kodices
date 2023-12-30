package com.iktwo.kodices.sampleapp.data

import com.iktwo.kodices.sampleapp.data.CountdownRepository
import com.iktwo.piktographs.data.Countdown

class InMemoryCountdownRepository : CountdownRepository {
    private val countdownList: MutableList<Countdown> = mutableListOf()

    override suspend fun get(): List<Countdown> {
        return countdownList
    }

    override suspend fun add(element: Countdown) {
        countdownList.add(element)
    }

    override fun isEmpty(): Boolean {
        return countdownList.isEmpty()
    }
}
