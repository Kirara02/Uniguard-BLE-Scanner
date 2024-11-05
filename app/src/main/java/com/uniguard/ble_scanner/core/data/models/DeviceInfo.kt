package com.uniguard.ble_scanner.core.data.models

data class DeviceInfo(
    val address: String,
    var rssi: Int,
    var name: String?,
    var lastSeen: Long
)
