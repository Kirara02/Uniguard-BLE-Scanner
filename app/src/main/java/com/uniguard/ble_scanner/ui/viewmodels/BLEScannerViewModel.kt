//package com.uniguard.ble_scanner.ui.viewmodels
//
//import android.annotation.SuppressLint
//import android.app.Application
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothManager
//import android.bluetooth.le.ScanCallback
//import android.bluetooth.le.ScanResult
//import android.content.Context
//import android.os.Build
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
//import com.uniguard.ble_scanner.core.data.models.BLERequest
//import com.uniguard.ble_scanner.core.data.models.DeviceInfo
//import com.uniguard.ble_scanner.core.domain.usecases.ble.UploadsUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//
//@HiltViewModel
//class BLEScannerViewModel @Inject constructor(
//    private val uploadsUseCase: UploadsUseCase,
//    private val settingsDataStore: SettingsDataStore,
//    application: Application,
//) : ViewModel() {
//
//
//    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
//    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner
//
//    private val _scannedDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
//    val scannedDevices: StateFlow<List<DeviceInfo>> get() = _scannedDevices
//
//    private val _intervalScan = MutableStateFlow(5) // Default value
//    val intervalScan: StateFlow<Int> get() = _intervalScan
//
//    private val idDevice = settingsDataStore.idDevice
//
//    init {
//        viewModelScope.launch {
//            settingsDataStore.intervalScan.collect { interval ->
//                _intervalScan.value = interval ?: 5
//            }
//        }
//        viewModelScope.launch {
//            idDevice.collect { id ->
//                Log.d("ID_DEVICE", "ID: $id ")
//                if (id == null || id == "") {
//                    val deviceName = Build.MODEL
//                    val defaultIdDevice = "id_$deviceName"
//
//                    updateIdDevice(defaultIdDevice)
//                }
//            }
//        }
//        startPeriodicUpload()
//
//    }
//
//    private fun startPeriodicUpload(){
//        viewModelScope.launch {
//            while (true){
//                delay(_intervalScan.value*1000.toLong())
//                uploadBleApiCall()
//            }
//        }
//    }
//
//    private suspend fun uploadBleApiCall(){
//        val deviceId = idDevice.firstOrNull() ?: return
//        val bleList = _scannedDevices.value.map { it.device.address }
//        val requestData = BLERequest(deviceId, bleList)
//
//        try {
//            uploadsUseCase.execute(requestData)
//                .catch {
//                    Log.e("ERROR", "uploadBleApiCall: ${it.message}")
//                }.collect{
//                    Log.d("SUCCESS", "uploadBleApiCall: $it")
//                }
//        }catch (e: Exception){
//            Log.e("API EXCEPTION", "uploadBleApiCall: ${e.message}", )
//        }
//    }
//
//    private fun updateIdDevice(newIdDevice: String) {
//        viewModelScope.launch {
//            settingsDataStore.updateIdDevice(newIdDevice)
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    fun startScanning() {
//        stopScanning()
//        bleScanner?.startScan(scanCallback)
//    }
//
//    @SuppressLint("MissingPermission")
//    fun stopScanning() {
//        bleScanner?.stopScan(scanCallback)
//    }
//
//    // Create a ScanCallback to handle scan results
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            updateDeviceList(result)
//        }
//
//        override fun onBatchScanResults(results: List<ScanResult>) {
//            super.onBatchScanResults(results)
//            results.forEach { updateDeviceList(it) }
//        }
//
//        private fun updateDeviceList(result: ScanResult) {
//            val device = result.device
//            val rssi = result.rssi
//
//            val currentDevices = _scannedDevices.value.toMutableList()
//            val existingDeviceIndex = currentDevices.indexOfFirst { it.device.address == device.address }
//
//            if (existingDeviceIndex != -1) {
//                // Update RSSI for existing device
//                currentDevices[existingDeviceIndex] = currentDevices[existingDeviceIndex].copy(rssi = rssi)
//            } else {
//                // Add new device
//                currentDevices.add(DeviceInfo(device, rssi))
//            }
//
//            _scannedDevices.value = currentDevices
//        }
//    }
//
//    // Function to determine if the device is a BLE device
//    @SuppressLint("MissingPermission")
//    private fun isBLEDevice(device: BluetoothDevice): Boolean {
//        return device.type == BluetoothDevice.DEVICE_TYPE_LE || device.name?.startsWith("BLE") == true
//    }
//
//    override fun onCleared() {
//        stopScanning()
//        super.onCleared()
//    }
//}

package com.uniguard.ble_scanner.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import com.uniguard.ble_scanner.core.data.models.BLERequest
import com.uniguard.ble_scanner.core.data.models.DeviceInfo
import com.uniguard.ble_scanner.core.domain.usecases.ble.UploadsUseCase
import com.uniguard.ble_scanner.core.services.BLEScannerService
import com.uniguard.ble_scanner.core.services.ScanResultReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BLEScannerViewModel @Inject constructor(
    private val uploadsUseCase: UploadsUseCase,
    private val settingsDataStore: SettingsDataStore,
    private val application: Application,
) : ViewModel() {

    private val _scannedDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val scannedDevices: StateFlow<List<DeviceInfo>> get() = _scannedDevices

    private val _intervalScan = MutableStateFlow(5)
    val intervalScan: StateFlow<Int> get() = _intervalScan

    private val idDevice = settingsDataStore.idDevice

    private var scanResultReceiver: BroadcastReceiver? = null

    init {
        viewModelScope.launch {
            settingsDataStore.intervalScan.collect { interval ->
                _intervalScan.value = interval ?: 5
            }
        }
        viewModelScope.launch {
            idDevice.collect { id ->
                Log.d("ID_DEVICE", "ID: $id ")
                if (id.isNullOrEmpty()) {
                    val deviceName = Build.MODEL
                    val defaultIdDevice = "id_$deviceName"
                    updateIdDevice(defaultIdDevice)
                }
            }
        }

        registerScanResultReceiver()
    }


    private fun registerScanResultReceiver() {
        val filter = IntentFilter("com.uniguard.ble_scanner.SCAN_RESULT")
        scanResultReceiver = ScanResultReceiver { deviceAddress, rssi, name, lastSeen ->
            deviceAddress?.let { address ->
                updateDeviceList(DeviceInfo(address, rssi, name, lastSeen))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(scanResultReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            application.registerReceiver(scanResultReceiver, filter)
        }
    }

    private fun updateIdDevice(newIdDevice: String) {
        viewModelScope.launch {
            settingsDataStore.updateIdDevice(newIdDevice)
        }
    }

    private fun startPeriodicUpload() {
        viewModelScope.launch {
            while (true) {
                delay(_intervalScan.value * 1000L)
                uploadBleApiCall()
            }
        }
    }

    private suspend fun uploadBleApiCall() {
        val deviceId = idDevice.firstOrNull() ?: return
        val bleList = _scannedDevices.value.map { it.address }
        val requestData = BLERequest(deviceId, bleList)

        try {
            uploadsUseCase.execute(requestData)
                .catch { Log.e("ERROR", "uploadBleApiCall: ${it.message}") }
                .collect { Log.d("SUCCESS", "uploadBleApiCall: $it") }
        } catch (e: Exception) {
            Log.e("API EXCEPTION", "uploadBleApiCall: ${e.message}")
        }
    }

    fun startScanning() {
        val intent = Intent(application, BLEScannerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }

        startPeriodicUpload()
    }

    private fun stopScanning(context: Context) {
        val intent = Intent(context, BLEScannerService::class.java)
        context.stopService(intent)
    }

    private fun updateDeviceList(deviceInfo: DeviceInfo) {
        val currentDevices = _scannedDevices.value.toMutableList()
        val existingDeviceIndex = currentDevices.indexOfFirst { it.address == deviceInfo.address }

        if (existingDeviceIndex != -1) {
            currentDevices[existingDeviceIndex] = currentDevices[existingDeviceIndex].copy(
                rssi = deviceInfo.rssi,
                lastSeen = deviceInfo.lastSeen
            )
        } else {
            currentDevices.add(deviceInfo.copy(lastSeen = System.currentTimeMillis()))
        }

        _scannedDevices.value = currentDevices
        removeOldDevices()
    }


    private fun removeOldDevices() {
        val currentTime = System.currentTimeMillis()
        val validTimeFrame = 30000
        _scannedDevices.value = _scannedDevices.value.filter {
            currentTime - it.lastSeen <= validTimeFrame
        }.toMutableList()
    }

    private fun unregisterScanResultReceiver() {
        scanResultReceiver?.let {
            application.unregisterReceiver(it)
            scanResultReceiver = null
        }
    }

    override fun onCleared() {
        stopScanning(application)
        unregisterScanResultReceiver()
        super.onCleared()
    }
}
