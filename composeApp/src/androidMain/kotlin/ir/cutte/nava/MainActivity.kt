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
import androidx.lifecycle.lifecycleScope
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.engine.SmsIngestionProcessor
import ir.cutte.nava.service.NavaForegroundService
import ir.cutte.nava.ui.NavaApp
import ir.cutte.nava.util.BatteryUtil
import ir.cutte.nava.util.DeviceIdentifierHelper
import ir.cutte.nava.util.NetworkUtil
import ir.cutte.nava.util.OemIntentNavigator
import androidx.activity.SystemBarStyle
import android.graphics.Color as AndroidColor
import ir.cutte.nava.util.PermissionHelper
import ir.cutte.nava.worker.HeartbeatScheduler
import ir.cutte.nava.worker.SmsDispatchWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.parseColor("#F8FAFC"),
                AndroidColor.parseColor("#F8FAFC")
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.parseColor("#F8FAFC"),
                AndroidColor.parseColor("#F8FAFC")
            )
        )
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext.navaDataStore)
        val httpDispatcher = HttpDispatcher()
        val hardwareId = DeviceIdentifierHelper.getHardwareDeviceId(this)
        val defaultName = DeviceIdentifierHelper.getDefaultDeviceName()
        val deviceInfo = defaultName

        lifecycleScope.launch {
            settingsRepository.ensureDeviceIdentification(hardwareId, defaultName)
        }

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
            var batteryStatus by remember {
                mutableStateOf(BatteryUtil.getBatteryStatus(this@MainActivity))
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
                        batteryStatus = BatteryUtil.getBatteryStatus(this@MainActivity)
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
                batteryLevel = batteryStatus.batteryLevel,
                isCharging = batteryStatus.isCharging,
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
                onToggleHeartbeatScheduler = { enabled ->
                    if (enabled) {
                        HeartbeatScheduler.schedule(this@MainActivity)
                    } else {
                        HeartbeatScheduler.cancel(this@MainActivity)
                    }
                },
                onSimulateSms = { sender, body ->
                    val settings = settingsRepository.getSnapshot()
                    val isOnline = NetworkUtil.isOnline(this@MainActivity)
                    val processor = SmsIngestionProcessor(settingsRepository, httpDispatcher)
                    processor.processIncoming(
                        settings = settings,
                        rawSender = sender,
                        body = body,
                        simSlot = 0,
                        batteryLevel = batteryStatus.batteryLevel,
                        isCharging = batteryStatus.isCharging,
                        deviceInfo = deviceInfo,
                        isOnline = isOnline,
                        forceDispatch = true,
                        onEnqueueWorker = { activityId, payload ->
                            SmsDispatchWorker.enqueue(
                                context = applicationContext,
                                activityId = activityId,
                                primaryUrl = settings.primaryWorkerUrl,
                                secondaryUrl = settings.secondaryWorkerUrl,
                                authToken = settings.authToken,
                                payload = payload
                            )
                        }
                    )
                },
                onDispatchTestPayload = { primaryUrl, secondaryUrl, authToken, payload ->
                    httpDispatcher.dispatchWithFailover(
                        primaryUrl = primaryUrl,
                        secondaryUrl = secondaryUrl,
                        authToken = authToken,
                        payload = payload
                    )
                }
            )
        }
    }
}
