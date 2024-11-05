package com.uniguard.ble_scanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uniguard.ble_scanner.ui.screens.SettingScreen
import com.uniguard.ble_scanner.ui.screens.BLEScannerScreen
import com.uniguard.ble_scanner.ui.theme.BleScannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            BleScannerTheme{
                NavHost(
                    navController = navController,
                    startDestination = "/main"
                ) {
                    composable("/main") {
                        BLEScannerScreen(
                            navController = navController
                        )
                    }
                    composable("/settings") {
                        SettingScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

