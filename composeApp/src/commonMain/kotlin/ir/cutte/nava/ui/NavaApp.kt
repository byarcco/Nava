package ir.cutte.nava.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.engine.IngestionOutcome
import ir.cutte.nava.model.AppSettings
import ir.cutte.nava.model.DeliveryStatus
import ir.cutte.nava.model.DispatchResult
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.SmsPayload
import ir.cutte.nava.ui.theme.NavaTheme
import ir.cutte.nava.util.currentTimeMillis
import ir.cutte.nava.util.formatActivityDuration
import ir.cutte.nava.util.toPersianDigits
import kotlinx.coroutines.delay
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
    onSimulateSms: suspend (String, String) -> IngestionOutcome,
    onDispatchTestPayload: suspend (String, String, String, SmsPayload) -> DispatchResult
) {
    val coroutineScope = rememberCoroutineScope()
    val appSettings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
    var currentScreen by remember { mutableStateOf(NavaScreen.Dashboard) }

    var currentTimestamp by remember { mutableStateOf(currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L)
            currentTimestamp = currentTimeMillis()
        }
    }

    val uptimeText = remember(appSettings.serviceStartTimestamp, appSettings.isServiceEnabled, currentTimestamp) {
        if (!appSettings.isServiceEnabled) {
            "غیرفعال"
        } else {
            val startTime = if (appSettings.serviceStartTimestamp > 0L) appSettings.serviceStartTimestamp else currentTimestamp
            val elapsedMillis = (currentTimestamp - startTime).coerceAtLeast(0L)
            formatActivityDuration(elapsedMillis)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavaTheme {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == NavaScreen.Settings) {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    } else {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    }
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    NavaScreen.Dashboard -> {
                        DashboardScreen(
                            isServiceEnabled = appSettings.isServiceEnabled,
                            probeStatus = appSettings.lastProbeStatus,
                            uptimeText = uptimeText,
                            totalDispatchedCount = appSettings.totalDispatchedCount,
                            hasSmsPermission = hasSmsPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                            isOemAutostartConfigured = appSettings.isOemAutostartConfigured,
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
                            onConfirmOemAutostart = {
                                coroutineScope.launch {
                                    settingsRepository.setOemAutostartConfigured(true)
                                }
                            },
                            onToggleForegroundService = onToggleForegroundService,
                            onSimulateSms = onSimulateSms,
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
                            deviceId = appSettings.deviceId.ifBlank { deviceInfo },
                            deviceName = appSettings.deviceName.ifBlank { deviceInfo },
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
                            onUpdateDeviceName = { name ->
                                coroutineScope.launch {
                                    settingsRepository.updateDeviceName(name)
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
                                val code = "123456"
                                val testPayload = SmsPayload(
                                    code = code,
                                    timestamp = currentTimeMillis() / 1000L,
                                    deviceId = appSettings.deviceId.ifBlank { deviceInfo },
                                    deviceName = appSettings.deviceName.ifBlank { deviceInfo },
                                    sender = "Raja.ir",
                                    body = "پیامک آزمایشی جهت ارزیابی ارتباط سامانه: $code",
                                    simSlot = 0,
                                    matchedKeyword = "آزمایش",
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
                                    timestamp = currentTimeMillis(),
                                    normalizedSender = "Raja.ir",
                                    matchedKeyword = "آزمایش",
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
    }
}
