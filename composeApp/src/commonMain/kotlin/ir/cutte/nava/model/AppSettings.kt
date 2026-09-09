package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val primaryWorkerUrl: String = "https://sms.cutte.ir",
    val secondaryWorkerUrl: String = "https://nava.imartrioss.workers.dev",
    val authToken: String = "85d8ecf6-5641-451a-a41d-20927eeccd28",
    val whitelistSenders: Set<String> = setOf("Raja.ir"),
    val keywords: Set<String> = setOf("کد ورود", "کد تایید", "کد تائید"),
    val isForwardAllEnabled: Boolean = false,
    val deviceId: String = "",
    val deviceName: String = "",
    val isServiceEnabled: Boolean = true,
    val isHeartbeatEnabled: Boolean = true,
    val isOemAutostartConfigured: Boolean = false,
    val lastProbeStatus: ProbeStatus = ProbeStatus.CONNECTED_PRIMARY,
    val lastProbeTimestamp: Long = 0L,
    val firstFailedProbeTimestamp: Long = 0L,
    val currentProbeIntervalMinutes: Long = 30L,
    val totalDispatchedCount: Long = 0L,
    val serviceStartTimestamp: Long = 0L,
    val recentActivities: List<ForwardingActivity> = emptyList()
)
