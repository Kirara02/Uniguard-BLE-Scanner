package com.uniguard.ble_scanner.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScanResultReceiver : BroadcastReceiver() {

    // Temporary callback holder
    var onScanResult: ((String?, Int, String, Long) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val deviceAddress = it.getStringExtra("device_address")
            val rssi = it.getIntExtra("rssi", -1)
            val name = it.getStringExtra("device_name") ?: "Unknown"
            val lastSeen = System.currentTimeMillis()

            // Call the callback if it's set
            onScanResult?.invoke(deviceAddress, rssi, name, lastSeen)
        }
    }
}
