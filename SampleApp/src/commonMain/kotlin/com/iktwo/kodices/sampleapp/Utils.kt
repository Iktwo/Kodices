package com.iktwo.kodices.sampleapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

val dataStore = getDataStore()
expect fun getDataStore(): DataStore<Preferences>

const val dataStoreFile = "forms_cache_"
