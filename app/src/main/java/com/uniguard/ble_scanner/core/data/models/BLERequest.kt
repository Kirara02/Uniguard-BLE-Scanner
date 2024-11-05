package com.uniguard.ble_scanner.core.data.models

import com.google.gson.annotations.SerializedName

data class BLERequest(
    @SerializedName("id_device")
    val deviceId: String,
    val rfid : List<String>
)
