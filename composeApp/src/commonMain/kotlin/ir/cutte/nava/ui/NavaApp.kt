package ir.cutte.nava.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.model.AppSettings
import ir.cutte.nava.model.DeliveryStatus
import ir.cutte.nava.model.DispatchResult
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.SmsPayload
import ir.cutte.nava.ui.theme.NavaTheme
import ir.cutte.nava.util.currentTimeMillis
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NavaScreen {
    Dashboard,
    Settings
}

@Composable
fun NavaApp(
    settingsRepository: SettingsRepository,
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isForegroundRunning: Boolean,
    batteryLevel: Int,
    isCharging: Boolean,
    deviceInfo: String,
    onRequestSmsPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onLaunchOemAutostart: () -> Unit,
    onToggleForegroundService: () -> Unit,
    onToggleHeartbeatScheduler: (Boolean) -> Unit,
    onDispatchTestPayload: suspend (String, String, String, SmsPayload) -> DispatchResult
) {
    val coroutineScope = rememberCoroutineScope()
    val appSettings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
    var currentScreen by remember { mutableStateOf(NavaScreen.Dashboard) }

    val uptimeText = remember(appSettings.serviceStartTimestamp, appSettings.isServiceEnabled) {
        if (!appSettings.isServiceEnabled || appSettings.serviceStartTimestamp == 0L) {
            "Offline"
        } else {
            val elapsedMillis = currentTimeMillis() - appSettings.serviceStartTimestamp
            val hours = (elapsedMillis / (1000 * 60 * 60)).coerceAtLeast(0)
            val minutes = ((elapsedMillis / (1000 * 60)) % 60).coerceAtLeast(0)
            "${hours}h ${minutes}m"
        }
    }

    NavaTheme {
        when (currentScreen) {
            NavaScreen.Dashboard -> {
                DashboardScreen(
                    isServiceEnabled = appSettings.isServiceEnabled,
                    probeStatus = appSettings.lastProbeStatus,
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    uptimeText = uptimeText,
                    totalDispatchedCount = appSettings.totalDispatchedCount,
                    hasSmsPermission = hasSmsPermission,
                    hasNotificationPermission = hasNotificationPermission,
                    isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                    isForegroundRunning = isForegroundRunning,
                    recentActivities = appSettings.recentActivities,
                    onToggleService = { enabled ->
                        coroutineScope.launch {
                            settingsRepository.setServiceEnabled(enabled)
                        }
                    },
                    onRequestSmsPermission = onRequestSmsPermission,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onRequestBatteryOptimization = onRequestBatteryOptimization,
                    onLaunchOemAutostart = onLaunchOemAutostart,
                    onToggleForegroundService = onToggleForegroundService,
                    onNavigateToSettings = { currentScreen = NavaScreen.Settings }
                )
            }
            NavaScreen.Settings -> {
                SettingsScreen(
                    primaryWorkerUrl = appSettings.primaryWorkerUrl,
                    secondaryWorkerUrl = appSettings.secondaryWorkerUrl,
                    authToken = appSettings.authToken,
                    whitelistSenders = appSettings.whitelistSenders,
                    keywords = appSettings.keywords,
                    isServiceEnabled = appSettings.isServiceEnabled,
                    isHeartbeatEnabled = appSettings.isHeartbeatEnabled,
                    onUpdatePrimaryWorkerUrl = { url ->
                        coroutineScope.launch {
                            settingsRepository.updatePrimaryWorkerUrl(url)
                        }
                    },
                    onUpdateSecondaryWorkerUrl = { url ->
                        coroutineScope.launch {
                            settingsRepository.updateSecondaryWorkerUrl(url)
                        }
                    },
                    onUpdateAuthToken = { token ->
                        coroutineScope.launch {
                            settingsRepository.updateAuthToken(token)
                        }
                    },
                    onAddWhitelist = { sender ->
                        coroutineScope.launch {
                            settingsRepository.addWhitelistSender(sender)
                        }
                    },
                    onRemoveWhitelist = { sender ->
                        coroutineScope.launch {
                            settingsRepository.removeWhitelistSender(sender)
                        }
                    },
                    onAddKeyword = { keyword ->
                        coroutineScope.launch {
                            settingsRepository.addKeyword(keyword)
                        }
                    },
                    onRemoveKeyword = { keyword ->
                        coroutineScope.launch {
                            settingsRepository.removeKeyword(keyword)
                        }
                    },
                    onToggleService = { enabled ->
                        coroutineScope.launch {
                            settingsRepository.setServiceEnabled(enabled)
                        }
                    },
                    onToggleHeartbeat = { enabled ->
                        coroutineScope.launch {
                            settingsRepository.setHeartbeatEnabled(enabled)
                            onToggleHeartbeatScheduler(enabled)
                        }
                    },
                    onDispatchTestPayload = {
                        val testPayload = SmsPayload(
                            sender = "TEST_SENDER",
                            body = "Test incoming SMS payload with failover verification",
                            timestamp = currentTimeMillis(),
                            simSlot = 0,
                            matchedKeyword = "DIAGNOSTIC",
                            deviceInfo = deviceInfo,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging
                        )
                        val result = onDispatchTestPayload(
                            appSettings.primaryWorkerUrl,
                            appSettings.secondaryWorkerUrl,
                            appSettings.authToken,
                            testPayload
                        )
                        val activity = ForwardingActivity(
                            id = "${currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
                            timestamp = testPayload.timestamp,
                            normalizedSender = "TEST_SENDER",
                            matchedKeyword = "DIAGNOSTIC",
                            httpStatusCode = result.statusCode,
                            isSuccess = result.isSuccess,
                            snippet = testPayload.body,
                            deliveryStatus = if (result.isSuccess) DeliveryStatus.DISPATCHED_INSTANT else DeliveryStatus.QUEUED_OFFLINE
                        )
                        settingsRepository.recordActivity(activity)
                        result
                    },
                    onNavigateBack = { currentScreen = NavaScreen.Dashboard }
                )
            }
        }
    }
}
