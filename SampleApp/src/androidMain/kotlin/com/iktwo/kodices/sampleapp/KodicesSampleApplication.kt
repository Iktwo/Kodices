package com.iktwo.kodices.sampleapp

import android.app.Application
import android.content.Context

class KodicesSampleApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: KodicesSampleApplication

        fun applicationContext(): Context {
            return instance.applicationContext
        }
    }
}
