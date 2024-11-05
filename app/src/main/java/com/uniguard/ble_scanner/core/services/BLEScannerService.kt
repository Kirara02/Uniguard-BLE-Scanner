package com.uniguard.ble_scanner.core.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class BLEScannerService : Service() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        Log.d("BLEScannerService", "onCreate: Service start")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, createNotification())
        } else {
            startForeground(1, createLegacyNotification())
        }

       startScanning()
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
            results.forEach { result ->
                sendScanResultToUI(result)
            }
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


    override fun onDestroy() {
        stopScanning()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? = null
}