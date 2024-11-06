package com.uniguard.ble_scanner.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferenceViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val url = settingsDataStore.url
    val intervalScan = settingsDataStore.intervalScan
    val idDevice = settingsDataStore.idDevice
    val isHitInBackground = settingsDataStore.isHitInBackground

    fun updateUrl(newUrl: String) {
        viewModelScope.launch {
            settingsDataStore.updateUrl(newUrl)
        }
    }

    fun updateIntervalScan(newInterval: Int) {
        viewModelScope.launch {
            settingsDataStore.updateIntervalScan(newInterval)
        }
    }

    fun updateIdDevice(newIdDevice: String) {
        viewModelScope.launch {
            settingsDataStore.updateIdDevice(newIdDevice)
        }
    }

    fun updateIsHitInBackground(newIsHitInBackground: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateIsHitInBackground(newIsHitInBackground)
        }
    }
}
