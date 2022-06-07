package com.example.smartLED

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class DataStoreManager(private val context: Context) {
    private object PreferencesKeys {
        val COLOR = intPreferencesKey("color")
    }

    suspend fun saveToDataStore(value: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.COLOR] = value
        }
    }

    val colorFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            // Get our show completed value, defaulting to false if not set:
            val color = preferences[PreferencesKeys.COLOR] ?: 0
            color
        }
}
