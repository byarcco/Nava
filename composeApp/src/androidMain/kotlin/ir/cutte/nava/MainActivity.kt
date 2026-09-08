package ir.cutte.nava

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.service.NavaForegroundService
import ir.cutte.nava.ui.NavaApp
import ir.cutte.nava.util.OemIntentNavigator
import ir.cutte.nava.util.PermissionHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext.navaDataStore)
        val httpDispatcher = HttpDispatcher()
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val deviceInfo = "$manufacturer ${Build.MODEL}"

        setContent {
            var hasSmsPermission by remember {
                mutableStateOf(PermissionHelper.hasSmsPermissions(this@MainActivity))
            }
            var hasNotificationPermission by remember {
                mutableStateOf(PermissionHelper.hasNotificationPermission(this@MainActivity))
            }
            var isBatteryOptimizationIgnored by remember {
                mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(this@MainActivity))
            }
            var isForegroundRunning by remember {
                mutableStateOf(PermissionHelper.hasForegroundServicePermission(this@MainActivity))
            }

            val smsPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val receiveGranted = results[Manifest.permission.RECEIVE_SMS] ?: false
                val readGranted = results[Manifest.permission.READ_SMS] ?: false
                hasSmsPermission = receiveGranted && readGranted
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotificationPermission = isGranted
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasSmsPermission = PermissionHelper.hasSmsPermissions(this@MainActivity)
                        hasNotificationPermission = PermissionHelper.hasNotificationPermission(this@MainActivity)
                        isBatteryOptimizationIgnored = PermissionHelper.isBatteryOptimizationIgnored(this@MainActivity)
                        isForegroundRunning = PermissionHelper.hasForegroundServicePermission(this@MainActivity)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            NavaApp(
                settingsRepository = settingsRepository,
                hasSmsPermission = hasSmsPermission,
                hasNotificationPermission = hasNotificationPermission,
                isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                isForegroundRunning = isForegroundRunning,
                deviceInfo = deviceInfo,
                onRequestSmsPermission = {
                    smsPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                        )
                    )
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onRequestBatteryOptimization = {
                    OemIntentNavigator.launchBatteryOptimizationSettings(this@MainActivity)
                },
                onLaunchOemAutostart = {
                    OemIntentNavigator.launchAutostartSettings(this@MainActivity)
                },
                onToggleForegroundService = {
                    if (PermissionHelper.hasForegroundServicePermission(this@MainActivity)) {
                        NavaForegroundService.start(this@MainActivity)
                        isForegroundRunning = true
                    }
                },
                onDispatchTestPayload = { endpointUrl, payload ->
                    httpDispatcher.dispatch(endpointUrl, payload)
                }
            )
        }
    }
}
