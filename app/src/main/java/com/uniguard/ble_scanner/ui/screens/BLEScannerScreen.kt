package com.uniguard.ble_scanner.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.uniguard.ble_scanner.ui.composable.BLEPermissionCheck
import com.uniguard.ble_scanner.ui.viewmodels.BLEScannerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEScannerScreen(
    viewModel: BLEScannerViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    val intervalScan by viewModel.intervalScan.collectAsState()

    BLEPermissionCheck {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    title = {
                        Text(
                            text = "BLE Scanner",
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("/settings")
                        }) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->

            LaunchedEffect(intervalScan) {
                viewModel.startScanning()
            }

            val scannedDevices by viewModel.scannedDevices.collectAsState()

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LazyColumn {
                    items(scannedDevices) { deviceInfo ->
                        DeviceItem(address = deviceInfo.address, rssi = deviceInfo.rssi, deviceName = deviceInfo.name?:"Unknown")
                    }
                }
            }
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(address: String, rssi: Int, deviceName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(8.dp)
    ) {
        Text(text = "Name: $deviceName")
        Text(text = "Address: $address")
        Text(text = "Signal Strength: $rssi dBm")
    }
}

