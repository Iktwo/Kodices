package com.iktwo.kodices.sampleapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual fun getDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath { "$DATA_STORE_FILE.preferences_pb".toPath() }
}
