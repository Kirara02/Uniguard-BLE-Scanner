package com.uniguard.ble_scanner.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.uniguard.ble_scanner.ui.composable.BLEPermissionCheck
import com.uniguard.ble_scanner.ui.viewmodels.BLEScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEScannerScreen(
    viewModel: BLEScannerViewModel = hiltViewModel(),
    navController: NavController
) {

    BLEPermissionCheck {

        val uploadMessage by viewModel.uploadMessage.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(end = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLE Scanner",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = uploadMessage,
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
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

            LaunchedEffect(Unit) {
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

