package com.uniguard.ble_scanner.core.data.datasource.local

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Application.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(
    application: Application
) {

    companion object {
        private val URL_KEY = stringPreferencesKey("url")
        private val INTERVAL_SCAN_KEY = intPreferencesKey("scan_interval")
        private val ID_DEVICE_KEY = stringPreferencesKey("id_device")
    }

    private val dataStore = application.dataStore

    val url: Flow<String?> = dataStore.data.map { preferences ->
        preferences[URL_KEY]
    }

    val intervalScan: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[INTERVAL_SCAN_KEY]
    }

    val idDevice: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ID_DEVICE_KEY]
    }

    suspend fun updateUrl(newUrl: String) {
        dataStore.edit { preferences ->
            preferences[URL_KEY] = newUrl
        }
    }

    suspend fun updateIntervalScan(newInterval: Int) {
        dataStore.edit { preferences ->
            preferences[INTERVAL_SCAN_KEY] = newInterval
        }
    }

    suspend fun updateIdDevice(newIdDevice: String) {
        dataStore.edit { preferences ->
            preferences[ID_DEVICE_KEY] = newIdDevice
        }
    }
}