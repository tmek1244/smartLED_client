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
        val POWER_STATE = booleanPreferencesKey("powerState")
    }

    suspend fun saveColor(value: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.COLOR] = value
        }
    }

    suspend fun savePowerState(value: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.POWER_STATE] = value
        }
    }

    suspend fun getColor() = context.dataStore.data
        .map { preferences ->
            // Get our show completed value, defaulting to false if not set:
            val color = preferences[PreferencesKeys.COLOR] ?: 0
            color
        }

    suspend fun getPowerState() = context.dataStore.data
        .map { preferences ->
            val state = preferences[PreferencesKeys.POWER_STATE] ?: false
            state
        }
}
