package com.uniguard.ble_scanner.core.data.datasource.local

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uniguard.ble_scanner.core.services.BLEScannerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Application.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(
    private val application: Application
) {

    companion object {
        private val URL_KEY = stringPreferencesKey("url")
        private val INTERVAL_SCAN_KEY = intPreferencesKey("scan_interval")
        private val ID_DEVICE_KEY = stringPreferencesKey("id_device")
        private val IS_HIT_IN_BACKGROUND = booleanPreferencesKey("is_hit_in_background")
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

    val isHitInBackground: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_HIT_IN_BACKGROUND] ?: false
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

    suspend fun updateIsHitInBackground(newIsHitInBackground: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_HIT_IN_BACKGROUND] = newIsHitInBackground
        }
        stopScanning(application)
        startScanning()
    }

    private fun startScanning() {
        val intent = Intent(application, BLEScannerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    private fun stopScanning(context: Context) {
        val intent = Intent(context, BLEScannerService::class.java)
        application.stopService(intent)
    }
}