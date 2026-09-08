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
    deviceInfo: String,
    onRequestSmsPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onLaunchOemAutostart: () -> Unit,
    onToggleForegroundService: () -> Unit,
    onDispatchTestPayload: suspend (String, SmsPayload) -> DispatchResult
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
                    workerUrl = appSettings.workerUrl,
                    whitelistSenders = appSettings.whitelistSenders,
                    keywords = appSettings.keywords,
                    isServiceEnabled = appSettings.isServiceEnabled,
                    onUpdateWorkerUrl = { url ->
                        coroutineScope.launch {
                            settingsRepository.updateWorkerUrl(url)
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
                    onDispatchTestPayload = {
                        val testPayload = SmsPayload(
                            sender = "TEST_SENDER",
                            body = "Test incoming SMS payload for worker endpoint verification",
                            timestamp = currentTimeMillis(),
                            simSlot = 0,
                            matchedKeyword = "DIAGNOSTIC",
                            deviceInfo = deviceInfo
                        )
                        val result = onDispatchTestPayload(appSettings.workerUrl, testPayload)
                        val activity = ForwardingActivity(
                            id = "${currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
                            timestamp = testPayload.timestamp,
                            normalizedSender = "TEST_SENDER",
                            matchedKeyword = "DIAGNOSTIC",
                            httpStatusCode = result.statusCode,
                            isSuccess = result.isSuccess,
                            snippet = testPayload.body
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
