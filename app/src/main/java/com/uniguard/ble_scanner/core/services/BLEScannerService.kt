package com.uniguard.ble_scanner.core.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.uniguard.ble_scanner.core.data.datasource.local.SettingsDataStore
import com.uniguard.ble_scanner.core.data.models.BLERequest
import com.uniguard.ble_scanner.core.data.models.DeviceInfo
import com.uniguard.ble_scanner.core.domain.usecases.ble.UploadsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class BLEScannerService : Service() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    @Inject
    lateinit var uploadsUseCase: UploadsUseCase

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private var intervalScan = 5
    private var scanResultReceiver: BroadcastReceiver? = null
    private val _scannedDevices = mutableListOf<DeviceInfo>()
    private var periodicUploadJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        Log.d("BLEScannerService", "onCreate: Service start")

        CoroutineScope(Dispatchers.IO).launch {
            intervalScan = settingsDataStore.intervalScan.first() ?: 5
            val isHitInBackground = settingsDataStore.isHitInBackground.first()
            if (isHitInBackground) {
                startPeriodicUpload()
            } else {
                stopPeriodicUpload()
            }
        }

        // Ensure service is started in the foreground quickly after calling startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, createNotification())
        } else {
            startForeground(1, createLegacyNotification())
        }

        startScanning()
        registerScanResultReceiver()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "BLEScannerServiceChannel"
        val channel = NotificationChannel(
            channelId,
            "BLE Scanner Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("BLE Scanning Active")
            .setContentText("The app is scanning for Bluetooth devices")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    @Suppress("deprecation")
    private fun createLegacyNotification(): Notification {
        val builder = Notification.Builder(this)
            .setContentTitle("BLE Scanning Active")
            .setContentText("The app is scanning for Bluetooth devices")
            .setSmallIcon(android.R.drawable.ic_dialog_info)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build()
        }
        return builder.notification
    }

    private fun startScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bleScanner?.startScan(scanCallback)
    }

    private fun stopScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bleScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            sendScanResultToUI(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result -> sendScanResultToUI(result) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEScannerService", "Scan failed with error code: $errorCode")
        }
    }

    private fun sendScanResultToUI(scanResult: ScanResult) {
        val intent = Intent("com.uniguard.ble_scanner.SCAN_RESULT")
        intent.putExtra("device_address", scanResult.device.address)
        intent.putExtra("rssi", scanResult.rssi)
        intent.putExtra("device_name", scanResult.device.name)
        sendBroadcast(intent)
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

    private fun updateDeviceList(deviceInfo: DeviceInfo) {
        val existingDeviceIndex = _scannedDevices.indexOfFirst { it.address == deviceInfo.address }

        if (existingDeviceIndex != -1) {
            _scannedDevices[existingDeviceIndex] = _scannedDevices[existingDeviceIndex].copy(
                rssi = deviceInfo.rssi,
                lastSeen = deviceInfo.lastSeen
            )
        } else {
            _scannedDevices.add(deviceInfo)
        }

        removeOldDevices()
    }

    private fun removeOldDevices() {
        val currentTime = System.currentTimeMillis()
        val validTimeFrame = 30 * 1000 // 30 seconds

        _scannedDevices.removeAll { currentTime - it.lastSeen > validTimeFrame }
    }

    private fun startBackgroundApiHit() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay((intervalScan * 1000).toLong())
                uploadBleApiCall()
            }
        }
    }

    private fun startPeriodicUpload() {
        periodicUploadJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay((intervalScan * 1000).toLong())
                uploadBleApiCall()
            }
        }
    }

    private fun stopPeriodicUpload() {
        periodicUploadJob?.cancel()
        periodicUploadJob = null
    }

    private suspend fun uploadBleApiCall() {
        Log.d("BLEScannerService", "API CALLED")
        val deviceId = settingsDataStore.idDevice.firstOrNull() ?: return
        val bleList = _scannedDevices.map { it.address }
        val requestData = BLERequest(deviceId, bleList)

        try {
            uploadsUseCase.execute(requestData)
                .catch { Log.e("ERROR", "uploadBleApiCall: ${it.message}") }
                .collect { Log.d("SUCCESS", "uploadBleApiCall: $it") }
        } catch (e: Exception) {
            Log.e("API EXCEPTION", "uploadBleApiCall: ${e.message}")
        }
    }

    override fun onDestroy() {
        scanResultReceiver?.let { receiver ->
            try {
                application.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.e("BLEScannerService", "Receiver not registered: ${e.message}")
            }
        }
        stopScanning()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? = null
}
