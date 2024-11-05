package com.uniguard.ble_scanner.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScanResultReceiver(private val onScanResult: (String?, Int, String, Long) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val deviceAddress = it.getStringExtra("device_address")
            val rssi = it.getIntExtra("rssi", -1)
            val name = it.getStringExtra("device_name") ?: "Unknown"
            val lastSeen = System.currentTimeMillis()

            Log.d("ScanResultReceiver", "Received scan result: $deviceAddress, RSSI: $rssi, Name: $name")
            onScanResult(deviceAddress, rssi, name, lastSeen)
        }
    }
}