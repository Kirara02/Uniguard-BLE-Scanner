package com.uniguard.ble_scanner.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.uniguard.ble_scanner.ui.composable.UTextField
import com.uniguard.ble_scanner.ui.viewmodels.PreferenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: PreferenceViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    // MutableState for temporary data storage
    var tempUrl by remember { mutableStateOf("") }
    var tempIntervalScan by remember { mutableStateOf("5") }
    var tempIdDevice by remember { mutableStateOf("") }

    val url by viewModel.url.collectAsState(initial = "")
    val intervalScan by viewModel.intervalScan.collectAsState(initial = 5)
    val idDevice by viewModel.idDevice.collectAsState(initial = "")

    // Initialize MutableState with current values
    LaunchedEffect(url, intervalScan, idDevice) {
        tempUrl = url ?: ""
        tempIntervalScan = (intervalScan ?: 5).toString()
        tempIdDevice = idDevice ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                title = {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            UTextField (
                value = tempUrl,
                onValueChange = { tempUrl = it },
                label = "URL",
                hint = "Input your URL here"
            )

            Spacer(modifier = Modifier.height(8.dp))

            UTextField(
                value = tempIntervalScan,
                onValueChange = { tempIntervalScan = it },
                label = "Interval Scan (seconds)",
                hint = "Enter scan interval in seconds"
            )

            Spacer(modifier = Modifier.height(8.dp))

            UTextField(
                value = tempIdDevice,
                onValueChange = { tempIdDevice = it },
                label = "ID Device",
                hint = "Input your device ID here"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        tempUrl.isBlank() -> {
                            // Show a message or a toast indicating the URL cannot be empty
                            Log.e("SettingScreen", "SettingScreen: url empty", )
                        }
                        !(tempUrl.startsWith("http://") || tempUrl.startsWith("https://")) -> {
                            // Show a message or a toast indicating the URL must start with http/https
                            Toast.makeText(context, "URL must start with http:// or https://", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Update the URL since it is valid
                            viewModel.updateUrl(tempUrl)
                        }
                    }

                    if(tempIntervalScan.isNotBlank()){
                        viewModel.updateIntervalScan(tempIntervalScan.toIntOrNull() ?: 5)
                    }
                    if(tempIdDevice.isNotBlank()){
                        viewModel.updateIdDevice(tempIdDevice)
                    }
                    navController.navigateUp() // Navigate back after saving
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}
