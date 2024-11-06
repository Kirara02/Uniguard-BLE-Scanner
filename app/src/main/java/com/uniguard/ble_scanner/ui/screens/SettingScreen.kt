package com.uniguard.ble_scanner.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.uniguard.ble_scanner.ui.composable.UTextField
import com.uniguard.ble_scanner.ui.viewmodels.BLEScannerViewModel
import com.uniguard.ble_scanner.ui.viewmodels.PreferenceViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: PreferenceViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    // State for dialog visibility
    var showDialog by remember { mutableStateOf(false) }

    // MutableState for temporary data storage
    var tempUrl by remember { mutableStateOf("") }
    var tempIntervalScan by remember { mutableStateOf("5") }
    var tempIdDevice by remember { mutableStateOf("") }
    var tempIsHitInBackground by remember { mutableStateOf(false) }

    val url by viewModel.url.collectAsState(initial = "")
    val intervalScan by viewModel.intervalScan.collectAsState(initial = 5)
    val idDevice by viewModel.idDevice.collectAsState(initial = "")
    val isHitInBackground by viewModel.isHitInBackground.collectAsState(initial = false)

    // Initialize MutableState with current values
    LaunchedEffect(url, intervalScan, idDevice) {
        tempUrl = url ?: ""
        tempIntervalScan = (intervalScan ?: 5).toString()
        tempIdDevice = idDevice ?: ""
        tempIsHitInBackground = isHitInBackground
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

        // Show dialog when the value changes
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Do nothing, we want the user to make a decision
                },
                title = { Text("Restart Required") },
                text = { Text("Changes to Preferences will require a restart of the app. Would you like to restart now?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Restarting...", Toast.LENGTH_SHORT).show()
                            restartApp(context)
                        }
                    ) {
                        Text("Restart")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false // Dismiss the dialog
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Upload in Background")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = tempIsHitInBackground,
                    onCheckedChange = { tempIsHitInBackground = it }
                )
            }

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
                    viewModel.updateIsHitInBackground(tempIsHitInBackground)
                    // Check if 'Run in Background' or 'Interval' changed
                    if (tempIsHitInBackground != isHitInBackground || tempIntervalScan != intervalScan.toString()) {
                        showDialog = true
                    } else {
                        navController.navigate("/main")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}

// Function to restart the application
private fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  or Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(
        AlarmManager.RTC,
        SystemClock.elapsedRealtime() + 100,
        pendingIntent
    )

    // Exit the application after scheduling restart
    exitProcess(0)
}