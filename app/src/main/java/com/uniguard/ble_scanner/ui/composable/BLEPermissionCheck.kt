package com.uniguard.ble_scanner.ui.composable

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun BLEPermissionCheck(
    onPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }

    // Define required permissions based on Android version
    val permissions = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
    }

    // Launcher to request permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsGranted ->
        hasPermissions = permissionsGranted.all { it.value }
    }

    // Check permissions and request if necessary
    LaunchedEffect(Unit) {
        if (permissions.all {
                ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }) {
            hasPermissions = true
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    if (hasPermissions) {
        onPermissionsGranted()
    }
}
